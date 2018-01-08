package ChatServer;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class Server
{
    public static final String ACCOUNT_PATH = "accounts.dat";
    
    public String Name;
    public int Port;
    public ServerStatus Status;
    public AccountManager Accounts;

    private ServerForm _form;
    private ServerSocket _socket;
    private Map<String,ClientThread> _clientMap;
    private Map<String, Map<String,ClientThread>> _roomMap;

    public Server(int port, String name, ServerForm form) 
    {
        _form = form;
        _socket = null;
        _clientMap = new HashMap<>();
        _roomMap = new HashMap<>();
        
        Name = name;
        Port = port;
        setStatus(ServerStatus.Default);
        Accounts = new AccountManager(ACCOUNT_PATH);
    }
    
    public void init() {        
        _roomMap.put("Default", new HashMap<>());
        
        try {
            _socket = new ServerSocket(Port);
            log(String.format("Created ServerSocket at %s on port %d", getHost(), getPort()) );
            setStatus(ServerStatus.Initialized);
        } catch(IOException ex)
        {
            log("Error while creating ServerSocket: " + ex.getMessage());
            setStatus(ServerStatus.Error);
        }
    }

    public void start() 
    {
        if(Status != ServerStatus.Initialized) {
            log("Server has to be initialized.");
            return;
        } else setStatus(ServerStatus.Running);
        
        while(Status == ServerStatus.Running) {
            try {
                Socket clientSocket = _socket.accept();
                log("Client tries to connect...");
                loginClient(clientSocket);
            } catch(IOException | ClassNotFoundException ex ) {
                if(ex instanceof SocketException) continue;
                log("Error while client tried to connect: " + ex.getMessage());
            }
        }
    }
    
    public synchronized void stop() {
        log("Stopping Server...");
        try {
            for(ClientThread client : _clientMap.values() ) {
                client.close(false);
            }
            _clientMap.clear();
            _roomMap.clear();
            
            _socket.close();
            setStatus(ServerStatus.Stopped);
            log("Server was stopped.");
        } catch (IOException | ClassNotFoundException ex) {
            log("Exception while trying to close server: " + ex.getMessage());
        }
        //Thread.currentThread().interrupt();
    }
    
    private void loginClient(Socket clientSocket) throws IOException, ClassNotFoundException {
        ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
                
        Message credentialMsg = (Message)in.readObject();
        String name = credentialMsg.Text[0];
        String password = credentialMsg.Text[1];
        log(credentialMsg);
        
        Message replyMsg;
        if(_clientMap.containsKey(name)) {
            replyMsg = new Message(MessageType.LOGIN_REPLY, "server", "newClient", "CLIENT_ALREADY_CONNECTED");
            log(replyMsg);
            out.writeObject(replyMsg);
            out.flush();
            
            out.close();
            in.close();
            clientSocket.close();
            log("Client connection attempt refused because user with same name is already connected.");
            
            return;
        }
        else {
            if(Accounts.accountExists(name)) {
                if(!Accounts.authenticate(name, password)) {
                    replyMsg = new Message(MessageType.LOGIN_REPLY, "server", "newClient", "WRONG_CREDENTIALS");
                    log(replyMsg);
                    out.writeObject(replyMsg);
                    out.flush();
                
                    out.close();
                    in.close();
                    clientSocket.close();
                    log("Client connection attempt refused because of wrong credentials.");
                    
                    return;
                }
            } else {
                Accounts.createAccount(name, password);
                log("Client registered new account with given credentials.");
            }
        }
        
        replyMsg = new Message(MessageType.LOGIN_REPLY, "server", "newClient", "ACCEPTED");
        log(replyMsg);
        out.writeObject(replyMsg);
        out.flush();
                
        ClientThread thread = new ClientThread(this, clientSocket, name, out, in);
        _clientMap.put(name, thread);
        _roomMap.get("Default").put(name, thread);
        thread.start();
                
        log("Client " + name + " succesfully connected.");
    }
        
    public synchronized void logoutClient(String clientName, String roomName) {
        if(!_clientMap.containsKey(clientName)) return;
        _clientMap.remove(clientName);
        
        _roomMap.get(roomName).remove(clientName);
        log("Client " + clientName + " disconnected.");
    }    
        
    public synchronized void forwardPrivate(Message msg) throws IOException {
        ClientThread client = _clientMap.get(msg.Receiver);
        client.sendMessage(msg);
    }
    
    /**
    Forwards the given Message to all Clients in the Room addressed by Receiver except the Sender.
    **/
    public synchronized void forwardRoom(Message msg) throws IOException {
        String clientSender = msg.Sender;
        msg.Sender = msg.Receiver;
        
        for(ClientThread client : _roomMap.get(msg.Receiver).values()) {
            if(!client.ClientName.equals(clientSender)) {
                msg.Receiver = client.ClientName;
                client.sendMessage(msg);
            }
        }
    }
    
    /**
    Returns a String array containing the names of all clients in the given Room.
    Returns a String array containing the names of all clients if the given Room is null or empty.
    Returns null if the given Room doesn't exists.
    **/
    public synchronized String[] getClientNames(String roomName) {
        String[] result = null;
        
        if(roomName == null || roomName.equals("")) {
            result = new String[_clientMap.size()];
            int i = 0;
        
            for(String name : _clientMap.keySet()) {
                result[i++] = name;
            }
        } else if(_roomMap.containsKey(roomName)) {
            result = new String[_roomMap.get(roomName).size()];
            int i = 0;
        
            for(String name : _roomMap.get(roomName).keySet()) {
                result[i++] = name;
            }
        }
        
        return result;
    }
    
    /**
     * Renames the room roomName to newName. 
     * @param roomName
     * @param newName
     * @return True, if rename is successful. False if room doesn't exist or if trying to change the Default room.
     */
    public synchronized boolean renameRoom(String roomName, String newName) {
        if(roomName.equals("Default") || !_roomMap.containsKey(roomName)) return false;
        
        Map<String, ClientThread> room = _roomMap.get(roomName);
        _roomMap.remove(roomName);
        _roomMap.put(newName, room);
        
        Message msg = new Message(MessageType.INTERNAL, "server", null, "ROOM_RENAMED", newName);
        
        for(ClientThread client : room.values())  {
            msg.Receiver = client.ClientName;
            client.sendMessage(msg);
        }
        
        return true;
    }
    
    /**
     * Adds a new room to the server.
     * @param roomName
     * @return True, if new room was added successfuly. False if trying to create room named "Default" or room does already exists.
     */
    public synchronized boolean addRoom(String roomName) {
        if(roomName.equals("Default") || _roomMap.containsKey(roomName)) return false;
        
        _roomMap.put(roomName, new HashMap<>());
        
        Message msg = new Message(MessageType.INTERNAL, "server", null, "ROOM_ADDED", roomName);
        for(ClientThread client : _clientMap.values()) {
            msg.Receiver = client.ClientName;
            client.sendMessage(msg);
        }
        
        return true;
    }
    
    /**
     * Removes a room from the server.
     * @param roomName
     * @return True, if new room was removed successfuly. False if trying to remove room named "Default" or room doesn't exist.
     */
    public synchronized boolean removeRoom(String roomName) {
        if(roomName.equals("Default") || !_roomMap.containsKey(roomName)) return false;
        
        Map<String, ClientThread> room = _roomMap.get(roomName);
        for(ClientThread client : room.values()) {
            changeRoom(client.ClientName, "Default");
        }
        
        Message msg = new Message(MessageType.INTERNAL, "server", null, "ROOM_REMOVED", roomName);
        for(ClientThread client : _clientMap.values()) {
            msg.Receiver = client.ClientName;
            client.sendMessage(msg);
        }
        
        return true;
    }
    
    public synchronized void changeRoom(String clientName, String newRoom) {
        ClientThread client = _clientMap.get(clientName);
        
        if(!_roomMap.containsKey(newRoom) || client.RoomName.equals(newRoom)) {
            Message msg = new Message(MessageType.INTERNAL, "server", client.ClientName, "ROOM_CHANGED", "false");
            client.sendMessage(msg);
            return;
        }
             
        _roomMap.get(client.RoomName).remove(client.ClientName);
        _roomMap.get(newRoom).put(client.ClientName, client);    
        
        Message msg = new Message(MessageType.INTERNAL, "server", clientName, "ROOM_CHANGED", "true", "Default");
        client.sendMessage(msg);
    }
    
    public int getPort() 
    {
        return _socket.getLocalPort();
    }

    public String getHost() 
    {
        return _socket.getInetAddress().getHostAddress();
    }
    
    public final void setStatus(ServerStatus status) {
        Status = status;
        _form.updateStatus(status);
    }
    
    public void log(Object o) {
        if(o instanceof String) _form.logString((String)o);
        else if (o instanceof Message) _form.logMessage((Message)o);
        else _form.logString(o.toString());
    }
    
}
