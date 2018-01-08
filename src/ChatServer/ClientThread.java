package ChatServer;

import java.net.*;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

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
                switch(msg.Type) {
                    case ROOM:
                        Server.log(msg);
                        Server.forwardRoom(msg);
                        break;
                    case PRIVATE:
                        Server.log(msg);
                        Server.forwardPrivate(msg);
                        break;
                    case INTERNAL:
                        handleInternal(msg.Text);
                        break;
                    case RECEIVED:
                        Server.log("[Client: " + ClientName + "] Received " + msg.Text[0]);
                        break;
                }
            }
        } catch (IOException | ClassNotFoundException ex) {
            //Server.log("Error while handling client " + ClientName + ". Associated thread is closed.");
            closeInternal(true);
        }
    }
    
    private void handleInternal(String[] msg) {
        switch(msg[0]) {
            case "CLOSE_CONNECTION":
                closeInternal(true);
                break;
            case "CHANGE_ROOM":
                
                break;
        }
    }
    
    public void close(boolean logout) throws IOException, ClassNotFoundException {
        Message msg = new Message(MessageType.INTERNAL, "server", ClientName, "CONNECTION_CLOSED");
        _out.writeObject(msg);        
        _in.readObject();
        
        closeInternal(logout);
    }
    
    private void closeInternal(boolean logout) {
        IsActive = false;
        
        try {
            _socket.close();
            _in.close();
            _out.close();
        } catch (IOException ex) {
            Server.log("[Client: " + ClientName + "]Exception while closing client thread: " + ex.getMessage());
        }
        
        if(logout) Server.logoutClient(ClientName, RoomName);
        this.interrupt();
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

}
