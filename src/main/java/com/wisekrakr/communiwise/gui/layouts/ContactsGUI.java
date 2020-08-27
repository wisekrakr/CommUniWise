package com.wisekrakr.communiwise.gui.layouts;

import com.wisekrakr.communiwise.gui.EventManager;
import com.wisekrakr.communiwise.gui.ext.AbstractGUI;
import com.wisekrakr.communiwise.operations.apis.AccountAPI;
import com.wisekrakr.communiwise.user.ContactManager;
import com.wisekrakr.communiwise.user.phonebook.PhoneBookEntry;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;

public class ContactsGUI extends AbstractGUI {
    private JTextField username;
    private JTextField domain;
    private JTextField extension;
    private JButton addContact;
    private JPanel controlPanel;
    private JPanel contactPanel;

    private final GridBagConstraints gbc = new GridBagConstraints();

    private int labelCount = 0;
    private int gridx = 0;
    private int gridy = 0;

    private final EventManager eventManager;
    private final AccountAPI account;

    private final HashMap<String, ContactLabel> contactListLabels = new HashMap<>();

    public ContactsGUI(EventManager eventManager,AccountAPI account) {
        this.eventManager = eventManager;
        this.account = account;

        prepareGUI();
    }

    @Override
    public void prepareGUI() {
        setUndecorated(true);
        GridLayout gridLayout = new GridLayout();
        setLayout(gridLayout);

        addFrameDragAbility();

        JPanel mainPanel = new JPanel(new GridLayout(2, 1));
        mainPanel.setBorder(new TitledBorder("Contact List"));
        mainPanel.setMaximumSize(new Dimension(200, 300));

        contactPanel = new JPanel(new GridLayout(0, 1));
        controlPanel = new JPanel(new GridLayout(4, 2));

        add(mainPanel, BorderLayout.CENTER);

        JScrollPane scrollPane = new JScrollPane(contactPanel);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(controlPanel, BorderLayout.CENTER);

        controlPanel.add(new JLabel("Name: "), BorderLayout.WEST);
        controlPanel.add((username = new JTextField(3)), BorderLayout.CENTER);

        controlPanel.add(new JLabel("Domain: "), BorderLayout.WEST);
        controlPanel.add((domain = new JTextField(3)), BorderLayout.CENTER);

        controlPanel.add(new JLabel("Extension/Phone Number: "), BorderLayout.WEST);
        controlPanel.add((extension = new JTextField(3)), BorderLayout.CENTER);

        addContact = new JButton("Add Contact");
        controlPanel.add(addContact, BorderLayout.SOUTH);

    }

    @Override
    public void showWindow() {

        //run when first opening this frame
        contactListUpdate.run();
        addContact.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if (account.phoneBookHandler(ContactManager.UserOption.ADD_CONTACT, username.getText(), domain.getText(), Integer.parseInt(extension.getText()))){
                    eventManager.onAlert(ContactsGUI.this,
                            "Successfully added new contact: " + username.getText(),
                            JOptionPane.INFORMATION_MESSAGE);
                }else{
                    eventManager.onAlert(ContactsGUI.this,
                            "Failed adding contact: " + username.getText(),
                            JOptionPane.INFORMATION_MESSAGE);
                }

                //run again every time we add a new contact to the list
                contactListUpdate.run();
            }
        });

        JButton saveAndClose = new JButton("Save and Close");
        controlPanel.add(saveAndClose, BorderLayout.SOUTH);

        saveAndClose.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if(account.phoneBookHandler(ContactManager.UserOption.SAVE,null,null,0)){
                    eventManager.onAlert(ContactsGUI.this,
                            "Successfully saved contact list",
                            JOptionPane.INFORMATION_MESSAGE);
                }else{
                    eventManager.onAlert(ContactsGUI.this,
                            "Failed saving contact list",
                            JOptionPane.INFORMATION_MESSAGE);
                }

                hideWindow();
            }
        });

        pack();
        setVisible(true);

    }

    public Runnable contactListUpdate = new Runnable() {

        @Override
        public void run() {

            gbc.weightx = 1;
            gbc.weighty = 1;
            gbc.gridx = gridx;
            gbc.gridy = gridy;

            for (PhoneBookEntry contact : account.getContactManager().getPhoneBook().getEntries()) {

                if(!contactListLabels.containsKey(contact.getUsername())){

                    ContactLabel con = new ContactLabel(contact.getUsername(),
                            "<html>" +
                            ++labelCount + "    " + "<strong>" + contact.getUsername().toUpperCase() + "</strong>" +
                            ": " + contact.getExtension() + "@" + contact.getDomain() + "</html>");

                    contactListLabels.put(contact.getUsername(), con);

                    contactPanel.add(con, gbc);
                }
            }

            gridy++;
            if (gridy >= 0) {
                gridy = 0;
                gridx++;
            }

            contactPanel.revalidate();
            pack();
        }
    };

    protected class ContactLabel extends JPanel implements MouseListener {
        private final String contact;
        private final String text;

        private boolean isHighlighted;
        private final Border blackBorder = BorderFactory.createLineBorder(Color.BLACK);
        private final Border redBorder = BorderFactory.createLineBorder(Color.RED,2);

        private JButton edit, remove, call;
        private final JPanel buttonPanel;

        ContactLabel(String contact, String text){
            this.contact = contact;
            this.text = text;
            addMouseListener(this);
            setBorder(blackBorder);
            setFocusable(true);

            JLabel labelText = new JLabel(text);
            add(labelText);

            buttonPanel = new JPanel();
            add(buttonPanel);

        }

        @Override
        public Dimension getPreferredSize(){
            return new Dimension(150, 70);
        }

        private void showOptions(){
            call = new JButton("Call ");
            call.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {

                }
            });
            buttonPanel.add(call);

            edit = new JButton("Edit " );
            edit.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {

                }
            });
            add(edit);

            remove = new JButton("Remove " );
            remove.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if(account.phoneBookHandler(ContactManager.UserOption.DELETE_CONTACT, contact,null,0)){
                        eventManager.onAlert(ContactsGUI.this,
                                "Successfully removed contact: " + contact,
                                JOptionPane.INFORMATION_MESSAGE);
                    }else{
                        eventManager.onAlert(ContactsGUI.this,
                                "Failed removing contact list",
                                JOptionPane.INFORMATION_MESSAGE);
                    }

                    contactListUpdate.run();
                }
            });
            add(remove);
        }

        private void hideOptions(){
            if(isHighlighted){
                call.setVisible(false);
                edit.setVisible(false);
                remove.setVisible(false);
            }

        }

        @Override public void mouseClicked(MouseEvent e){
            if(isHighlighted) {
                setBorder(blackBorder);

                hideOptions();
            }else{
                setBorder(redBorder);

                showOptions();

            }
            isHighlighted=!isHighlighted;
        }

        @Override public void mousePressed(MouseEvent e){}
        @Override public void mouseReleased(MouseEvent e){}
        @Override public void mouseEntered(MouseEvent e){}
        @Override public void mouseExited(MouseEvent e){
        }
    }

}
