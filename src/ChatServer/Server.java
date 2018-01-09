package ChatServer;

import java.io.*;
import java.net.*;
import java.util.*;

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
        setServerStatus(ServerStatus.Default);
        Accounts = new AccountManager(ACCOUNT_PATH);
    }
    
    public void init() {   
        // Add Default room
        _roomMap.put("Default", new HashMap<>());
        
        try {
            _socket = new ServerSocket(Port);
            log(String.format("Created ServerSocket at %s on port %d", getHost(), getPort()) );
            setServerStatus(ServerStatus.Initialized);
        } catch(IOException ex)
        {
            log("Error while creating ServerSocket: " + ex.getMessage());
            setServerStatus(ServerStatus.Error);
        }
        
        updateForm();
    }

    public void start() 
    {
        if(Status != ServerStatus.Initialized) {
            log("Server has to be initialized.");
            return;
        } else setServerStatus(ServerStatus.Running);
        
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
                System.out.println("TEST_FOR");
            }
            _clientMap.clear();
            _roomMap.clear();
            
            _socket.close();
            setServerStatus(ServerStatus.Stopped);
            updateForm();
            log("Server was stopped.");
        } catch (IOException ex) {
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
            // already connected
            replyMsg = new Message(MessageType.LOGIN_REPLY, "server", "newClient", "ALREADY_CONNECTED");
            log(replyMsg);
            out.writeObject(replyMsg);
            out.flush();
            
            out.close();
            in.close();
            clientSocket.close();
            log("Client connection attempt refused because user with same name is already connected.");
        }
        else {
            if(Accounts.accountExists(name)) {
                if(!Accounts.authenticate(name, password)) {
                    // wrong credentials
                    replyMsg = new Message(MessageType.LOGIN_REPLY, "server", "newClient", "WRONG_CREDENTIALS");
                    log(replyMsg);
                    out.writeObject(replyMsg);
                    out.flush();
                
                    out.close();
                    in.close();
                    clientSocket.close();
                    log("Client connection attempt refused because of wrong credentials.");
                } else {
                    // logged in
                    replyMsg = new Message(MessageType.LOGIN_REPLY, "server", "newClient", "ACCEPTED");
                    log(replyMsg);
                    out.writeObject(replyMsg);
                    out.flush();
                
                    ClientThread thread = new ClientThread(this, clientSocket, name, out, in);
                    _clientMap.put(name, thread);
                    _roomMap.get("Default").put(name, thread);
                    thread.start();
        
                    Message msg = new Message(MessageType.INTERNAL, "server", null, "CLIENT_CONNECTED", name);
                    forwardPublic(msg, name);
                
                    updateForm();
                    log("Client " + name + " succesfully connected.");
                }
            } else {
                // new registered
                Accounts.createAccount(name, password);
                log("Client registered new account with given credentials.");
                replyMsg = new Message(MessageType.LOGIN_REPLY, "server", "newClient", "ACCEPTED_NEW");
                log(replyMsg);
                out.writeObject(replyMsg);
                out.flush();
                
                ClientThread thread = new ClientThread(this, clientSocket, name, out, in);
                _clientMap.put(name, thread);
                _roomMap.get("Default").put(name, thread);
                thread.start();
        
                Message msg = new Message(MessageType.INTERNAL, "server", null, "CLIENT_CONNECTED", name);
                forwardPublic(msg, name);
                
                updateForm();
                log("Client " + name + " succesfully connected.");
            }
        }
        
        
    }
    
    public synchronized void logoutClient(String clientName, String roomName) {
        if(!_clientMap.containsKey(clientName)) return;
        _clientMap.remove(clientName);
        _roomMap.get(roomName).remove(clientName);    
        
        Message msg = new Message(MessageType.INTERNAL, "server", null, "CLIENT_DISCONNECTED", clientName, roomName);
        forwardPublic(msg, null);
        
        updateForm();
        log("Client " + clientName + " disconnected.");
    }    
        
    public synchronized void forwardPrivate(Message msg) {
        ClientThread client = _clientMap.get(msg.Receiver);
        client.sendMessage(msg);
    }
    
    /**
     * Forwards the given Message to all Clients on the Server.
     * @param msg
     * @throws IOException 
     */
    public synchronized void forwardPublic(Message msg, String except) {
        for(ClientThread client : _clientMap.values()) {
            if(except != null && client.ClientName.equals(except)) continue;
            msg.Receiver = client.ClientName;
            client.sendMessage(msg);
        }
    }
    
    /**
    Forwards the given Message to all Clients in the Room addressed by Receiver except the Sender.
    **/
    public synchronized void forwardRoom(Message msg) {
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
     * Returns if a room with the given name exists.
     * @param roomName
     * @return True if the room exists.
     */
    public synchronized boolean roomExists(String roomName) {
        return _roomMap.containsKey(roomName);
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
        
        Message msg = new Message(MessageType.INTERNAL, "server", null, "ROOM_RENAMEND", roomName, newName);
        forwardPublic(msg, null);
                        
        updateForm();
        log(String.format("Room \"%s\" was renamed to \"%s\".", roomName, newName));
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
        forwardPublic(msg, null);
                        
        updateForm();
        log(String.format("Room \"%s\" was added.", roomName));
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
        for(String clientName : room.keySet()) {
            changeRoom(clientName, "Default");
        }
        _roomMap.remove(roomName);
        
        Message msg = new Message(MessageType.INTERNAL, "server", null, "ROOM_REMOVED", roomName);
        forwardPublic(msg, null);
                        
        updateForm();
        log(String.format("Room \"%s\" was removed. All users moved to room \"Default\".", roomName));
        return true;
    }
    
    public synchronized void changeRoom(String clientName, String newRoom) {
        ClientThread client = _clientMap.get(clientName);        
        String oldRoom = client.RoomName;
        
        if(!_roomMap.containsKey(newRoom) || oldRoom.equals(newRoom)) {
            // Inform Client that room change wasn't successful 
            Message msg = new Message(MessageType.INTERNAL, "server", client.ClientName, "REPLY_CHANGE_ROOM", "false");
            client.sendMessage(msg);
            return;
        }
             
        _roomMap.get(oldRoom).remove(client.ClientName);
        _roomMap.get(newRoom).put(client.ClientName, client);
        client.RoomName = newRoom;
        
        // Inform client about successful room change
        Message msgReply = new Message(MessageType.INTERNAL, "server", clientName, "REPLY_CHANGE_ROOM", "true", newRoom);
        client.sendMessage(msgReply);
        
        // Inform other clients about room change
        Message msgInfo = new Message(MessageType.INTERNAL, "server", null, "CLIENT_ROOM_CHANGED", clientName, oldRoom, newRoom);
        forwardPublic(msgInfo, clientName);
        
        updateForm();
        log(String.format("Client \"%s\" moved from room \"%s\" to \"%s\".", clientName, oldRoom, newRoom));
    }
    
    public synchronized void sendRoomClientMap(String clientName) {
        ClientThread client = _clientMap.get(clientName);
        
        Message replyMsg = new Message(MessageType.INTERNAL, "server", clientName,"REPLY_STATUSLIST");
        
        client.sendMessage(replyMsg);
        client.sendObject(getRoomClientMap());
    }
    
    public int getPort() 
    {
        return _socket.getLocalPort();
    }

    public String getHost() 
    {
        return _socket.getInetAddress().getHostAddress();
    }
        
    public final void setServerStatus(ServerStatus status) {
        Status = status;
        _form.updateStatus(status);
    }
    
    private HashMap<String, String[]> getRoomClientMap() {
        HashMap<String, String[]> roomUserMap = new HashMap<>();
        
        _roomMap.forEach( (roomName, clientMap) ->  {
            Set<String> userSet = clientMap.keySet();
            String[] userNames = userSet.toArray(new String[userSet.size()]);
            roomUserMap.put(roomName, userNames);
        });
        
        return roomUserMap;
    }
    
    private void updateForm() {
        _form.updateUserAndRooms(getRoomClientMap());
    }
    
    public void log(Object o) {
        if(o instanceof String) _form.logString((String)o);
        else if (o instanceof Message) _form.logMessage((Message)o);
        else _form.logString(o.toString());
    }
    
}
