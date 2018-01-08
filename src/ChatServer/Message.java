package ChatServer;

import java.io.*;
import java.time.*;

public class Message implements Serializable
{   
    public MessageType Type;
    public String Sender;
    public String Receiver;
    
    public LocalDateTime TimeStamp;
    public String[] Text;
    
    public Message(MessageType type, String sender, String receiver, String... text) {
        Type = type;
        Sender = sender;
        Receiver = receiver;
        Text = text;
        TimeStamp = LocalDateTime.now(ZoneId.of("Europe/Berlin"));
    }
    
    public Message(MessageType type, String sender, String receiver, String text) {
        Type = type;
        Sender = sender;
        Receiver = receiver;
        Text = new String[] {text};
        TimeStamp = LocalDateTime.now(ZoneId.of("Europe/Berlin"));
    }
    
    @Override
    public String toString() {
        return String.format("[%s from %s to %s] %s", Type.toString(), Sender, Receiver, Text[0]);
    }
    
}
