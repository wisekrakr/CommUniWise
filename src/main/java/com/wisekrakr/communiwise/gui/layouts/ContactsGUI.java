package com.wisekrakr.communiwise.gui.layouts;

import com.wisekrakr.communiwise.gui.ext.AbstractScreen;
import com.wisekrakr.communiwise.gui.layouts.utils.FrameDragListener;
import com.wisekrakr.communiwise.operations.apis.AccountAPI;
import com.wisekrakr.communiwise.user.phonebook.PhoneBookEntry;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ContactsGUI extends AbstractScreen {
    private JTextField username;
    private JTextField domain;
    private JTextField extension;

    private JPanel contactPanel;

    private final GridBagConstraints gbc = new GridBagConstraints();

    private int labelCount = 0;
    private int gridx = 0;
    private int gridy = 0;

    private final AccountAPI account;

    public ContactsGUI(AccountAPI account) {
        this.account = account;
    }

    @Override
    public void showWindow() {
        setUndecorated(true);
        GridLayout gridLayout = new GridLayout();
        setLayout(gridLayout);
//        setMaximumSize(new Dimension(200, 300));


        FrameDragListener frameDragListener = new FrameDragListener(this);
        this.addMouseListener(frameDragListener);
        this.addMouseMotionListener(frameDragListener);

        JPanel mainPanel = new JPanel(new GridLayout(2, 1));
        mainPanel.setBorder(new TitledBorder("Contact List"));
        mainPanel.setMaximumSize(new Dimension(200, 300));

        contactPanel = new JPanel(new GridLayout(0, 1));
        JPanel controlPanel = new JPanel(new GridLayout(4, 2));

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

        JButton addContact = new JButton("Add Contact");
        controlPanel.add(addContact, BorderLayout.SOUTH);

        //run when first opening this frame
        contactListUpdate.run();
        addContact.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                account.addContact(username.getText(), domain.getText(), Integer.parseInt(extension.getText()));

                //run again every time we add a new contact to the list
                contactListUpdate.run();
            }
        });

        JButton saveAndClose = new JButton("Save and Close");
        controlPanel.add(saveAndClose, BorderLayout.SOUTH);

        saveAndClose.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                account.saveContactList();

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

                JLabel con = new JLabel("<html>" +
                        ++labelCount + " " + "<strong>" + contact.getUsername().toUpperCase() + "</strong>"
                + ": " + contact.getExtension() + "@" + contact.getDomain() + "</html>");

                contactPanel.add(con, gbc);

                con.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        System.out.println(e.getX());
                        account.removeContact(contact.getUsername());
                    }
                });
//                    contactPanel.revalidate();
            }

            gridy++;
            if (gridy >= 0) {
                gridy = 0;
                gridx++;
            }

//                pack();
//                setLocationRelativeTo(null);

            try {
                setLocationByPlatform(true);
                setMinimumSize(getSize());
            } catch (Throwable e) {
                System.out.println("Could not add to contact list");
            }
        }
    };


}
