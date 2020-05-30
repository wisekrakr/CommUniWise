import javax.sip.*;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.address.URI;
import javax.sip.header.*;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;
import javax.swing.*;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Random;
import java.util.logging.Logger;

public class Main extends JFrame implements SipListener {

    // Objects used to communicate to the JAIN SIP API.
    SipFactory sipFactory;          // Used to access the SIP API.
    SipStack sipStack;              // The SIP stack.
    SipProvider sipProvider;        // Used to send SIP messages.
    MessageFactory messageFactory;  // Used to create SIP message factory.
    HeaderFactory headerFactory;    // Used to create SIP headers.
    AddressFactory addressFactory;  // Used to create SIP URIs.
    ListeningPoint listeningPoint;  // SIP listening IP address/port.
    Properties properties;          // Other properties.

    // Objects keeping local configuration.
    String ip;                      // The local IP address.
    int port = 6060;                // The local port.
    String protocol = "udp";        // The local protocol (UDP).
    int tag = (new Random()).nextInt(); // The local tag.
    Address contactAddress;         // The contact address.
    ContactHeader contactHeader;    // The contact header.

    public Main() {
        initComponents();
    }

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
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (UnsupportedLookAndFeelException ex) {
            Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Main().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton buttonBye;
    private JButton buttonInvite;
    private JButton buttonRegisterStatefull;
    private JButton buttonRegisterStateless;
    private JScrollPane scrollPane;
    private JTextArea textArea;
    private JTextField textField;
    // End of variables declaration//GEN-END:variables


    private void initComponents() {

        scrollPane = new JScrollPane();
        textArea = new JTextArea();
        buttonRegisterStateless = new JButton();
        buttonRegisterStatefull = new JButton();
        buttonInvite = new JButton();
        buttonBye = new JButton();
        textField = new JTextField();

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle("SIPPI [DEV]");
        setLocationByPlatform(true);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                onOpen(evt);
            }
        });

        textArea.setEditable(false);
        textArea.setColumns(20);
        textArea.setRows(5);
        scrollPane.setViewportView(textArea);

        buttonRegisterStateless.setText("Reg (SL)");
//        buttonRegisterStateless.setEnabled(false);
        buttonRegisterStateless.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                onRegisterStateless(evt);
            }
        });

        buttonRegisterStatefull.setText("Reg (SF)");
//        buttonRegisterStatefull.setEnabled(false);
        buttonRegisterStatefull.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                onRegisterStatefull(evt);
            }
        });

        buttonInvite.setText("Invite");
//        buttonInvite.setEnabled(false);
        buttonInvite.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                onInvite(evt);
            }
        });

        buttonBye.setText("Bye");
//        buttonBye.setEnabled(false);
        buttonBye.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                onBye(evt);
            }
        });

        textField.setText("sip:asterisk.interzone");

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(scrollPane)
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(buttonRegisterStateless, GroupLayout.PREFERRED_SIZE, 90, GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(buttonRegisterStatefull, GroupLayout.PREFERRED_SIZE, 90, GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(buttonInvite, GroupLayout.PREFERRED_SIZE, 90, GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(buttonBye, GroupLayout.PREFERRED_SIZE, 90, GroupLayout.PREFERRED_SIZE)
                                                .addGap(0, 2, Short.MAX_VALUE))
                                        .addComponent(textField))
                                .addContainerGap())
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(textField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 230, Short.MAX_VALUE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(buttonRegisterStateless)
                                        .addComponent(buttonInvite)
                                        .addComponent(buttonBye)
                                        .addComponent(buttonRegisterStatefull))
                                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void onOpen(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_onOpen
        // A method called when you open your application.

        try {
            // Get the local IP address.
            this.ip = InetAddress.getLocalHost().getHostAddress();
            // Create the SIP factory and set the path name.
            this.sipFactory = SipFactory.getInstance();
            this.sipFactory.setPathName("gov.nist");
            // Create and set the SIP stack properties.
            this.properties = new Properties();
            this.properties.setProperty("javax.sip.STACK_NAME", "stack");
            // Create the SIP stack.
            this.sipStack = this.sipFactory.createSipStack(this.properties);
            // Create the SIP message factory.
            this.messageFactory = this.sipFactory.createMessageFactory();
            // Create the SIP header factory.
            this.headerFactory = this.sipFactory.createHeaderFactory();
            // Create the SIP address factory.
            this.addressFactory = this.sipFactory.createAddressFactory();
            // Create the SIP listening point and bind it to the local IP address, port and protocol.
            this.listeningPoint = this.sipStack.createListeningPoint(this.ip, this.port, this.protocol);
            // Create the SIP provider.
            this.sipProvider = this.sipStack.createSipProvider(this.listeningPoint);
            // Add our application as a SIP listener.
            this.sipProvider.addSipListener(this);
            // Create the contact address used for all SIP messages.
            this.contactAddress = this.addressFactory.createAddress("sip:" + this.ip + ":" + this.port);
            // Create the contact header used for all SIP messages.
            this.contactHeader = this.headerFactory.createContactHeader(contactAddress);

            // Display the local IP address and port in the text area.
            this.textArea.append("Local address: " + this.ip + ":" + this.port + "\n");
        }
        catch(Exception e) {
            // If an error occurs, display an error message box and exit.
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(-1);
        }
    }//GEN-LAST:event_onOpen

    private void onRegisterStateless(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_onRegisterStateless
        // A method called when you click on the "Reg (SL)" button.
        try {
            // Get the destination address from the text field.
            Address addressTo = this.addressFactory.createAddress(this.textField.getText());
            // Create the request URI for the SIP message.
            URI requestURI = addressTo.getURI();

            // Create the SIP message headers.

            // The "Via" headers.
            ArrayList<ViaHeader> viaHeaders = new ArrayList<ViaHeader>();
            ViaHeader viaHeader = this.headerFactory.createViaHeader(this.ip, this.port, "udp", null);
            viaHeaders.add(viaHeader);
            // The "Max-Forwards" header.
            MaxForwardsHeader maxForwardsHeader = this.headerFactory.createMaxForwardsHeader(70);
            // The "sipphone.Call-Id" header.
            CallIdHeader callIdHeader = this.sipProvider.getNewCallId();
            // The "CSeq" header.
            CSeqHeader cSeqHeader = this.headerFactory.createCSeqHeader(1L,"REGISTER");
            // The "From" header.
            FromHeader fromHeader = this.headerFactory.createFromHeader(this.contactAddress, String.valueOf(this.tag));
            // The "To" header.
            ToHeader toHeader = this.headerFactory.createToHeader(addressTo, null);

            // Create the REGISTER request.
            Request request = this.messageFactory.createRequest(
                    requestURI,
                    "REGISTER",
                    callIdHeader,
                    cSeqHeader,
                    fromHeader,
                    toHeader,
                    viaHeaders,
                    maxForwardsHeader);
            // Add the "Contact" header to the request.
            request.addHeader(contactHeader);

            // Send the request statelessly through the SIP provider.
            this.sipProvider.sendRequest(request);

            // Display the message in the text area.
            this.textArea.append(
                    "Request sent:\n" + request.toString() + "\n\n");
        }
        catch(Exception e) {
            // If an error occurred, display the error.
            this.textArea.append("Request sent failed: " + e.getMessage() + "\n");
        }
    }//GEN-LAST:event_onRegisterStateless

    private void onRegisterStatefull(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_onRegisterStatefull
        // A method called when you click on the "Reg (SF)" button.
        // A method called when you click on the "Reg (SL)" button.
        try {
            // Get the destination address from the text field.
            Address addressTo = this.addressFactory.createAddress(this.textField.getText());
            // Create the request URI for the SIP message.
            URI requestURI = addressTo.getURI();

            // Create the SIP message headers.

            // The "Via" headers.
            ArrayList<ViaHeader> viaHeaders = new ArrayList<ViaHeader>();
            ViaHeader viaHeader = this.headerFactory.createViaHeader(this.ip, this.port, "udp", null);
            viaHeaders.add(viaHeader);
            // The "Max-Forwards" header.
            MaxForwardsHeader maxForwardsHeader = this.headerFactory.createMaxForwardsHeader(70);
            // The "sipphone.Call-Id" header.
            CallIdHeader callIdHeader = this.sipProvider.getNewCallId();
            // The "CSeq" header.
            CSeqHeader cSeqHeader = this.headerFactory.createCSeqHeader(1L,"REGISTER");
            // The "From" header.
            FromHeader fromHeader = this.headerFactory.createFromHeader(this.contactAddress, String.valueOf(this.tag));
            // The "To" header.
            ToHeader toHeader = this.headerFactory.createToHeader(addressTo, null);

            // Create the REGISTER request.
            Request request = this.messageFactory.createRequest(
                    requestURI,
                    "REGISTER",
                    callIdHeader,
                    cSeqHeader,
                    fromHeader,
                    toHeader,
                    viaHeaders,
                    maxForwardsHeader);
            // Add the "Contact" header to the request.
            request.addHeader(contactHeader);

            // Send the request statelessly through the SIP provider.
            // this.sipProvider.sendRequest(request);

            // Create a new SIP client transaction.
            ClientTransaction transaction = this.sipProvider.getNewClientTransaction(request);
            // Send the request statefully, through the client transaction.
            transaction.sendRequest();

            // Display the message in the text area.
            this.textArea.append(
                    "Request sent:\n" + request.toString() + "\n\n");
        }
        catch(Exception e) {
            // If an error occurred, display the error.
            this.textArea.append("Request sent failed: " + e.getMessage() + "\n");
        }
    }//GEN-LAST:event_onRegisterStatefull

    private void onInvite(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_onInvite
        // A method called when you click on the "Invite" button.


    }//GEN-LAST:event_onInvite

    private void onBye(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_onBye
        // A method called when you click on the "Bye" button.
    }//GEN-LAST:event_onBye

    @Override
    public void processRequest(RequestEvent requestEvent) {

    }

    @Override
    public void processResponse(ResponseEvent responseEvent) {
        // Get the response.
        Response response = responseEvent.getResponse();
        // Display the response message in the text area.
        this.textArea.append("\nReceived response: " + response.toString());
    }

    @Override
    public void processTimeout(TimeoutEvent timeoutEvent) {

    }

    @Override
    public void processIOException(IOExceptionEvent ioExceptionEvent) {

    }

    @Override
    public void processTransactionTerminated(TransactionTerminatedEvent transactionTerminatedEvent) {

    }

    @Override
    public void processDialogTerminated(DialogTerminatedEvent dialogTerminatedEvent) {

    }
}
