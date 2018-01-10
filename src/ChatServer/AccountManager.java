package ChatServer;

import java.util.*;
import java.io.*;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AccountManager 
{
    private HashMap<String, String> _accountBase;
    private List<String> _bannedAccounts;
    
    private File _accountFile;
    
    public AccountManager(String accountPath) {
        _accountFile = new File(accountPath);
        _accountBase = new HashMap<>();
        _bannedAccounts = new ArrayList<>();
        
        if(_accountFile.isFile()) readFile();
    }
    
    public synchronized boolean authenticate(String name, String pwd) {
        if(!accountExists(name) || accountIsBanned(name)) return false;
        
        return _accountBase.get(name).equals(pwd);
    }
    
    public synchronized boolean createAccount(String name, String pwd) {
        if(accountExists(name) || name.equals("newClient") || name.equals("server")) return false;
        
        _accountBase.put(name, pwd);
        writeFile();
        
        return true;
    }
    
    public synchronized boolean deleteAccount(String name) {
        if(!accountExists(name)) return false;
        
        _accountBase.remove(name);
        writeFile();
        
        return true;
    }
    
    public synchronized boolean accountExists(String name) {
        return _accountBase.containsKey(name);
    }
    
    public synchronized void banAccount(String name) {
        if(!accountExists(name)) return;
        
        _bannedAccounts.add(name);
    }
    
    public synchronized void unbanAccount(String name) {
        if(!accountIsBanned(name)) return;
        
        _bannedAccounts.remove(name);
    }
    
    public synchronized boolean accountIsBanned(String name) {
        return _bannedAccounts.contains(name);
    }
    
    @Override
    public synchronized String toString() {
        String result = "User\tPassword\tBanned\n";
        
        for(String name : _accountBase.keySet()) {
            result += String.format(" •%s\t  %s\t  %s\n", name, _accountBase.get(name), accountIsBanned(name));
        }
        
        return result;
    }
    
    private void writeFile() {
       try(ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(_accountFile, false))) {
           out.writeObject(_accountBase);
           out.writeObject(_bannedAccounts);
       } catch (IOException ex) {
            Logger.getLogger(AccountManager.class.getName()).log(Level.SEVERE, null, ex);
       }
    }
    
    private void readFile() {
        try(ObjectInputStream in = new ObjectInputStream(new FileInputStream(_accountFile))) {
           _accountBase = (HashMap<String,String>)in.readObject();
           _bannedAccounts = (ArrayList<String>)in.readObject();
       } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(AccountManager.class.getName()).log(Level.SEVERE, null, ex);
       }
    }    
}
