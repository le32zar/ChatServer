/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ChatServer;

import ChatServer.Dialogs.*;
import java.text.*;
import java.util.Date;
import java.io.*;
import java.util.*;
import javax.swing.*;

/**
 *
 * @author Sleepersword
 */
public class ServerForm extends javax.swing.JFrame {

    private Server serverInstance;
    private PrintWriter logWriter;
    private DefaultListModel userListModel;
    private DefaultListModel roomListModel;
    
    private int Port = 1501;
    private String ServerName = "ChatServer";  
    
    public ServerForm() {
        // Initialize Swing Components
        userListModel = new DefaultListModel();
        roomListModel = new DefaultListModel();
        initComponents();
        
        updateRunningComponents(false);
        // Finished
        
        try {
            String logFile = String.format("Log_ChatServer_%s_%s.txt", getDate(), getTime(true));
            logWriter = new PrintWriter(new FileWriter(logFile, false));
        } catch (IOException ex) {
            logString("Error while trying to create Log file: " + ex.getMessage());
        }
        
        serverInstance = new Server(Port, ServerName, this); 
        lblServerName.setText("Server Name: " + ServerName);      
        
    }
    
    public static String getTime(boolean includeSeconds) {
        DateFormat dateFormat = new SimpleDateFormat("HH-mm" + ((includeSeconds)?"-ss":"") );
        Date date = new Date();
        return dateFormat.format(date);
    }
    
    public static String getDate() {
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-YYYY");
        Date date = new Date();
        return dateFormat.format(date);
    }
    
    public final void logMessage(Message msg) {
        if(!msg.Sender.equals("server") && !msg.Receiver.equals("server")) {
            logString(String.format("%s --> %s [%s: %s]", msg.Sender, msg.Receiver ,msg.Type, msg.Text[0]));
        } else if(msg.Sender.equals("server")) {
            logString(String.format("--> %s [%s: %s]", msg.Receiver ,msg.Type, msg.Text[0]));
        } else {
            logString(String.format("<-- %s [%s: %s]", msg.Sender, msg.Type, msg.Text[0]));
        }
    }
    
    public final void logString(String text) {
        String logText = String.format("[%s]  %s\n", getTime(false), text);
        textAreaLog.append(logText);
        logWriter.print(logText);
    }
    
    public void showMessageDialog(String title, String text, int jOpenPane) {
        JOptionPane.showMessageDialog(null,text,title, jOpenPane);
    }
    
    public void updateStatus(ServerStatus status) {
        lblServerStatus.setText("Serverstatus: " + status.name());
    }
    
    public void updateUserAndRooms(Map<String, String[]> roomUserMap) {
        userListModel.clear();
        roomListModel.clear();
        
        roomUserMap.keySet().forEach((roomName) -> {
            String[] userNames = roomUserMap.get(roomName);
            roomListModel.addElement(String.format("%s (%d User)", roomName, userNames.length));
            
            for(String userName : userNames) {
                userListModel.addElement(String.format("[%s] %s", roomName, userName));
            }
        });
    }
    
    public final void updateRunningComponents(boolean enabled)  {        
        buttonUser.setEnabled(enabled);
        buttonRoom.setEnabled(enabled);
    }
    
    private String getRoomFromList() {
        return listRooms.getSelectedValue().split(" \\([0-9]* User\\)$")[0];
    }
    
    private String getUserFromList() {        
        return listUsers.getSelectedValue().split("^\\[.*\\] ")[1];
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelLog = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        textAreaLog = new javax.swing.JTextArea();
        lblServerStatus = new javax.swing.JLabel();
        lblServerName = new javax.swing.JLabel();
        panelStatus = new javax.swing.JPanel();
        tabbedPanelStatus = new javax.swing.JTabbedPane();
        jScrollPane2 = new javax.swing.JScrollPane();
        listUsers = new javax.swing.JList<>();
        jScrollPane3 = new javax.swing.JScrollPane();
        listRooms = new javax.swing.JList<>();
        menuMain = new javax.swing.JMenuBar();
        buttonServer = new javax.swing.JMenu();
        subButtonStart = new javax.swing.JMenuItem();
        subButtonStop = new javax.swing.JMenuItem();
        buttonUser = new javax.swing.JMenu();
        subButtonUserMessage = new javax.swing.JMenuItem();
        subButtonUserKick = new javax.swing.JMenuItem();
        subButtonUserBan = new javax.swing.JMenuItem();
        buttonRoom = new javax.swing.JMenu();
        subButtonRoomAdd = new javax.swing.JMenuItem();
        subButtonRoomRename = new javax.swing.JMenuItem();
        subButtonRoomRemove = new javax.swing.JMenuItem();
        buttonOptions = new javax.swing.JMenu();
        subButtonShowAccounts = new javax.swing.JMenuItem();
        subButtonServerOptions = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("ChatServer 0.1");
        setResizable(false);
        setSize(new java.awt.Dimension(950, 600));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        panelLog.setBorder(javax.swing.BorderFactory.createTitledBorder("Serverlog"));

        textAreaLog.setEditable(false);
        textAreaLog.setColumns(20);
        textAreaLog.setRows(5);
        jScrollPane1.setViewportView(textAreaLog);

        lblServerStatus.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblServerStatus.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblServerStatus.setText("jLabel2");

        lblServerName.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblServerName.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblServerName.setText("jLabel2");

        javax.swing.GroupLayout panelLogLayout = new javax.swing.GroupLayout(panelLog);
        panelLog.setLayout(panelLogLayout);
        panelLogLayout.setHorizontalGroup(
            panelLogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelLogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelLogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 410, Short.MAX_VALUE)
                    .addGroup(panelLogLayout.createSequentialGroup()
                        .addComponent(lblServerStatus)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(lblServerName)))
                .addContainerGap())
        );
        panelLogLayout.setVerticalGroup(
            panelLogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelLogLayout.createSequentialGroup()
                .addGroup(panelLogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblServerStatus)
                    .addComponent(lblServerName))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 502, Short.MAX_VALUE)
                .addContainerGap())
        );

        panelStatus.setBorder(javax.swing.BorderFactory.createTitledBorder("Status"));

        listUsers.setModel(userListModel);
        listUsers.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane2.setViewportView(listUsers);

        tabbedPanelStatus.addTab("Users", jScrollPane2);

        listRooms.setModel(roomListModel);
        listRooms.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane3.setViewportView(listRooms);

        tabbedPanelStatus.addTab("Rooms", jScrollPane3);

        javax.swing.GroupLayout panelStatusLayout = new javax.swing.GroupLayout(panelStatus);
        panelStatus.setLayout(panelStatusLayout);
        panelStatusLayout.setHorizontalGroup(
            panelStatusLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelStatusLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tabbedPanelStatus, javax.swing.GroupLayout.DEFAULT_SIZE, 446, Short.MAX_VALUE)
                .addContainerGap())
        );
        panelStatusLayout.setVerticalGroup(
            panelStatusLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelStatusLayout.createSequentialGroup()
                .addComponent(tabbedPanelStatus, javax.swing.GroupLayout.DEFAULT_SIZE, 269, Short.MAX_VALUE)
                .addContainerGap())
        );

        buttonServer.setText("Server");

        subButtonStart.setText("Start");
        subButtonStart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                subButtonStartActionPerformed(evt);
            }
        });
        buttonServer.add(subButtonStart);

        subButtonStop.setText("Stop");
        subButtonStop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                subButtonStopActionPerformed(evt);
            }
        });
        buttonServer.add(subButtonStop);

        menuMain.add(buttonServer);

        buttonUser.setText("User");

        subButtonUserMessage.setText("Send Message");
        subButtonUserMessage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                subButtonUserMessageActionPerformed(evt);
            }
        });
        buttonUser.add(subButtonUserMessage);

        subButtonUserKick.setText("Kick");
        subButtonUserKick.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                subButtonUserKickActionPerformed(evt);
            }
        });
        buttonUser.add(subButtonUserKick);

        subButtonUserBan.setText("Ban");
        subButtonUserBan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                subButtonUserBanActionPerformed(evt);
            }
        });
        buttonUser.add(subButtonUserBan);

        menuMain.add(buttonUser);

        buttonRoom.setText("Room");

        subButtonRoomAdd.setText("Add");
        subButtonRoomAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                subButtonRoomAddActionPerformed(evt);
            }
        });
        buttonRoom.add(subButtonRoomAdd);

        subButtonRoomRename.setText("Rename");
        subButtonRoomRename.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                subButtonRoomRenameActionPerformed(evt);
            }
        });
        buttonRoom.add(subButtonRoomRename);

        subButtonRoomRemove.setText("Remove");
        subButtonRoomRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                subButtonRoomRemoveActionPerformed(evt);
            }
        });
        buttonRoom.add(subButtonRoomRemove);

        menuMain.add(buttonRoom);

        buttonOptions.setText("Options");

        subButtonShowAccounts.setText("Show Accounts...");
        subButtonShowAccounts.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                subButtonShowAccountsActionPerformed(evt);
            }
        });
        buttonOptions.add(subButtonShowAccounts);

        subButtonServerOptions.setText("Server Options");
        subButtonServerOptions.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                subButtonServerOptionsActionPerformed(evt);
            }
        });
        buttonOptions.add(subButtonServerOptions);

        menuMain.add(buttonOptions);

        setJMenuBar(menuMain);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panelLog, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(panelStatus, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(panelStatus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 254, Short.MAX_VALUE))
                    .addComponent(panelLog, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void subButtonStartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_subButtonStartActionPerformed
        if(serverInstance.Status == ServerStatus.Running) {
            logString("Server is already running.");
            return;
        }
        
        Runnable serverRun = () -> {
            serverInstance.init();
            serverInstance.start();            
        };
        new Thread(serverRun).start();
        
        updateRunningComponents(true);
    }//GEN-LAST:event_subButtonStartActionPerformed

    private void subButtonStopActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_subButtonStopActionPerformed
        if(serverInstance.Status != ServerStatus.Default && serverInstance.Status != ServerStatus.Stopped) {
            serverInstance.stop(); 
        }
        
        updateRunningComponents(false);
    }//GEN-LAST:event_subButtonStopActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        if(serverInstance.Status != ServerStatus.Default && serverInstance.Status != ServerStatus.Stopped) {
            serverInstance.stop();
        }
        
        logWriter.close();
    }//GEN-LAST:event_formWindowClosing

    private void subButtonShowAccountsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_subButtonShowAccountsActionPerformed
        String title = "Registered Accounts";
        JTextArea tempArea = new JTextArea(serverInstance.Accounts.toString());    
        tempArea.setEditable(false);
        
        JOptionPane.showMessageDialog(null,tempArea,title, JOptionPane.PLAIN_MESSAGE);
    }//GEN-LAST:event_subButtonShowAccountsActionPerformed

    private void subButtonRoomAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_subButtonRoomAddActionPerformed
        AddRoomDialog dialog = new AddRoomDialog(this, true);
        dialog.setVisible(true);
        if(dialog.Canceled) return;
        
        while(serverInstance.roomExists(dialog.RoomName)) {
            JOptionPane.showMessageDialog(null,"The given room already exists.","Room already exists", JOptionPane.ERROR_MESSAGE);
            
            dialog.Canceled = true;
            dialog.setVisible(true);
            if(dialog.Canceled) return;
        }
        
        serverInstance.addRoom(dialog.RoomName);
    }//GEN-LAST:event_subButtonRoomAddActionPerformed

    private void subButtonRoomRenameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_subButtonRoomRenameActionPerformed
        if(listRooms.getSelectedIndex() == -1) {
            showMessageDialog("Error","No room selected.", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String selectedRoom = getRoomFromList();
        if(selectedRoom.equals("Default")) {
            showMessageDialog("Error","Can't rename Default room.", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        RenameRoomDialog dialog = new RenameRoomDialog(this, true, selectedRoom);
        dialog.setVisible(true);
        if(dialog.Canceled) return;
        
        while(serverInstance.roomExists(dialog.NewRoomName)) {
            JOptionPane.showMessageDialog(null,"The given room already exists.","Room already exists", JOptionPane.ERROR_MESSAGE);
            
            dialog.Canceled = true;
            dialog.setVisible(true);
            if(dialog.Canceled) return;
        }
        
        serverInstance.renameRoom(selectedRoom, dialog.NewRoomName);
    }//GEN-LAST:event_subButtonRoomRenameActionPerformed

    private void subButtonRoomRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_subButtonRoomRemoveActionPerformed
        if(listRooms.getSelectedIndex() == -1) {
            showMessageDialog("Error","No room selected.", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String selectedRoom = getRoomFromList();
        if(selectedRoom.equals("Default")) {
            showMessageDialog("Error","Can't remove Default room.", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if(JOptionPane.showConfirmDialog(null,"Do you really want to delete room " + selectedRoom,"Remove", JOptionPane.YES_NO_OPTION) ==  JOptionPane.YES_OPTION) {
            serverInstance.removeRoom(selectedRoom);
        }
    }//GEN-LAST:event_subButtonRoomRemoveActionPerformed

    private void subButtonUserMessageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_subButtonUserMessageActionPerformed
        if(listUsers.getSelectedIndex() == -1) {
            showMessageDialog("Error","No user selected.", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String selectedUser = getUserFromList();
        
        SendMessageDialog dialog = new SendMessageDialog(this, true);
        dialog.setVisible(true);
        
        if(dialog.Canceled) return;
        
        serverInstance.sendServerMessage(selectedUser, dialog.MessageText);
    }//GEN-LAST:event_subButtonUserMessageActionPerformed

    private void subButtonUserKickActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_subButtonUserKickActionPerformed
        if(listUsers.getSelectedIndex() == -1) {
            showMessageDialog("Error","No user selected.", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String selectedUser = getUserFromList();
        serverInstance.kickClient(selectedUser);
    }//GEN-LAST:event_subButtonUserKickActionPerformed

    private void subButtonUserBanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_subButtonUserBanActionPerformed
        if(listUsers.getSelectedIndex() == -1) {
            showMessageDialog("Error","No user selected.", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String selectedUser = getUserFromList();
        
        if(JOptionPane.showConfirmDialog(null,"Do you really want to ban user " + selectedUser,"Ban", JOptionPane.YES_NO_OPTION) ==  JOptionPane.YES_OPTION) {
            serverInstance.banClient(selectedUser);
        }
    }//GEN-LAST:event_subButtonUserBanActionPerformed

    private void subButtonServerOptionsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_subButtonServerOptionsActionPerformed
        SettingsDialog dialog = new SettingsDialog(this, true, Port, ServerName);
        dialog.setVisible(true);  
        
        if(!dialog.ServerName.equals(ServerName)) {
            logString("Server name changed from " + ServerName + " to " + dialog.ServerName + ".");
            ServerName = dialog.ServerName;
            lblServerName.setText("Server Name: " + ServerName);
        }
                
        if(dialog.Port != Port) {
            boolean wasRunning = (ServerStatus.Running == serverInstance.Status);
            //Stop server
            subButtonStopActionPerformed(null);
            
            //Log and change port
            logString("Port changed from " + Port + " to " + dialog.Port + ".");
            Port = dialog.Port;
            
            serverInstance = new Server(Port, ServerName, this);
            
            //Restart server
            if(wasRunning) subButtonStartActionPerformed(null);
        } 
    }//GEN-LAST:event_subButtonServerOptionsActionPerformed

    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(ServerForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ServerForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ServerForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ServerForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new ServerForm().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenu buttonOptions;
    private javax.swing.JMenu buttonRoom;
    private javax.swing.JMenu buttonServer;
    private javax.swing.JMenu buttonUser;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JLabel lblServerName;
    private javax.swing.JLabel lblServerStatus;
    private javax.swing.JList<String> listRooms;
    private javax.swing.JList<String> listUsers;
    private javax.swing.JMenuBar menuMain;
    private javax.swing.JPanel panelLog;
    private javax.swing.JPanel panelStatus;
    private javax.swing.JMenuItem subButtonRoomAdd;
    private javax.swing.JMenuItem subButtonRoomRemove;
    private javax.swing.JMenuItem subButtonRoomRename;
    private javax.swing.JMenuItem subButtonServerOptions;
    private javax.swing.JMenuItem subButtonShowAccounts;
    private javax.swing.JMenuItem subButtonStart;
    private javax.swing.JMenuItem subButtonStop;
    private javax.swing.JMenuItem subButtonUserBan;
    private javax.swing.JMenuItem subButtonUserKick;
    private javax.swing.JMenuItem subButtonUserMessage;
    private javax.swing.JTabbedPane tabbedPanelStatus;
    private javax.swing.JTextArea textAreaLog;
    // End of variables declaration//GEN-END:variables
}
