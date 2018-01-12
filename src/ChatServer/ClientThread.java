package ChatServer;

import java.net.*;
import java.io.*;

public class ClientThread extends Thread 
{    
    private Socket _socket;
    private ObjectInputStream _in;
    private ObjectOutputStream _out;
 
    public Server Server;
    public boolean IsActive;
    public String ClientName;
    public String RoomName;

    public ClientThread(Server server, Socket socket, String clientName, ObjectOutputStream out, ObjectInputStream in) {
        _socket = socket;
        _out = out;
        _in = in;

        Server = server;
        IsActive = true;
        ClientName = clientName;
        RoomName = "Default";
    }

    @Override
    public void run() {	
        try {        
            while(IsActive) {
                Message msg = (Message)_in.readObject();
                Server.log(msg);
                switch(msg.Type) {
                    case ROOM:
                        Server.forwardRoom(msg);
                        break;
                    case PRIVATE:
                        Server.forwardPrivate(msg);
                        break;
                    case INTERNAL:
                        handleInternal(msg);
                        break;
                }
            }
        } catch (IOException | ClassNotFoundException ex) {
            //Server.log("Error while handling client " + ClientName + ". Associated thread is closed.");
            closeInternal(true);
        }
    }
    
    private void handleInternal(Message msg) {
        switch(msg.Text[0]) {
            case "CLOSE_CONNECTION":
                closeInternal(true);
                break;
            case "REQUEST_CHANGE_ROOM":
                Server.changeRoom(msg.Sender, msg.Text[1]);
                break;
            case "REQUEST_STATUSLIST":
                Server.sendRoomClientMap(ClientName);
                break;
        }
    }
    
    public void close(boolean logout) {
        try {        
            Message msg = new Message(MessageType.INTERNAL, "server", ClientName, "CLOSE_CONNECTION");
            sendMessage(msg);
            _in.readObject();
        } catch (ClassNotFoundException | IOException ex) {
            //Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
        } 
        
        closeInternal(logout);
    }
    
    private void closeInternal(boolean logout) {
        IsActive = false;
        
        try {
            _socket.close();
            _in.close();
            _out.close();
        } catch (IOException ex) {
            Server.log("[Client: " + ClientName + "]Exception while trying to close client thread: " + ex.getMessage());
        }
        
        if(logout) Server.logoutClient(ClientName, RoomName);
    }
    
    public void sendMessage(Message msg){
        Server.log(msg);
        
        try {
            _out.writeObject(msg);
            _out.flush();
        } catch (IOException ex) {
            Server.log("[Client: " + ClientName + "]Exception while trying to send Message: " + ex.getMessage());
        }
    }
    
    public void sendObject(Serializable obj) {
        try {
            _out.writeObject(obj);
            _out.flush();
        } catch (IOException ex) {
            Server.log("[Client: " + ClientName + "]Exception while trying to send Object: " + ex.getMessage());
        }
    }

}
