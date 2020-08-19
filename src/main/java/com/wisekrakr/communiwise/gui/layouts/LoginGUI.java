package com.wisekrakr.communiwise.gui.layouts;

import com.wisekrakr.communiwise.gui.layouts.background.AlertFrame;
import com.wisekrakr.communiwise.gui.layouts.utils.Constants;
import com.wisekrakr.communiwise.gui.ext.AbstractScreen;
import com.wisekrakr.communiwise.gui.layouts.utils.FrameDragListener;
import com.wisekrakr.communiwise.gui.layouts.utils.InputValidator;
import com.wisekrakr.communiwise.operations.apis.PhoneAPI;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;

import javax.imageio.ImageIO;
import javax.sip.message.Response;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

public class LoginGUI extends AbstractScreen {
    private final PhoneAPI phone;

    private final JPanel textPanel = new JPanel();
    private final JPanel buttonPanel = new JPanel();
    private final JPanel logoPanel = new JPanel();

    private JTextField domainInput;
    private JTextField usernameInput;
    private JPasswordField passwordInput;
    private JTextField fromInput;
    private JTextField realmInput;

    private final JLabel fromAddress = new JLabel("address");
    private final JLabel domain = new JLabel("domain");
    private final JLabel realm = new JLabel("realm");
    private final JLabel username = new JLabel("username");
    private final JLabel password = new JLabel("password");

    private Image image;

    public LoginGUI(PhoneAPI phone) throws HeadlessException {
        this.phone = phone;
    }

    private static final int DESIRED_HEIGHT = 300;
    private static final int DESIRED_WIDTH = 600;



    @Override
    public void showWindow() {
        setTitle("Login to CommUniWise");
        setUndecorated(true);
        setBackground(Color.lightGray);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width - DESIRED_WIDTH) / 2, (screenSize.height - DESIRED_HEIGHT) / 2, DESIRED_WIDTH, DESIRED_HEIGHT);
        getRootPane().setBorder(BorderFactory.createEtchedBorder(Constants.DARK_CYAN, Color.darkGray));

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


        initComponents();
        buildLogoPanel();
        buildTextPanel();
        buildButtonPanel();

        setLayout(new BorderLayout());

        add(logoPanel,BorderLayout.WEST);
        add(textPanel,BorderLayout.CENTER);
        add(buttonPanel,BorderLayout.SOUTH);

        FrameDragListener frameDragListener = new FrameDragListener(this);
        this.addMouseListener(frameDragListener);
        this.addMouseMotionListener(frameDragListener);

        setVisible(true);
        setResizable(true);

    }

    private void initComponents(){

        domainInput = new JTextField("asterisk.interzone");
        realmInput = new JTextField("asterisk");
        usernameInput = new JTextField("damian2");
        passwordInput = new JPasswordField("45jf83f");
        fromInput = new JTextField("sip:"+ usernameInput.getText() + "@" + domainInput.getText());

        usernameInput.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                fromInput.setText("sip:"+ usernameInput.getText() + "@" + domainInput.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {  }
            @Override
            public void changedUpdate(DocumentEvent e) {  }
        });

        domainInput.getDocument().addDocumentListener(new DocumentListener() {
            String realmOnly;

            @Override
            public void insertUpdate(DocumentEvent e) {
                fromInput.setText("sip:"+ usernameInput.getText() + "@" + domainInput.getText());

                int dot = domainInput.getText().indexOf(".");

                if(dot != -1){
                    realmOnly = domainInput.getText().substring(0, dot);
                }

                realmInput.setText(realmOnly);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {         }
            @Override
            public void changedUpdate(DocumentEvent e) {         }
        });
    }

    public void buildLogoPanel(){

        try {
            image = ImageIO.read(new File("src/main/resources/logo1.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        JLabel picLabel = new JLabel(new ImageIcon(image));
        logoPanel.add(picLabel);
        logoPanel.setBackground(Constants.LIGHT_CYAN);
    }

    public void buildTextPanel () {

        GridLayout gridLayout = new GridLayout(6, 2);
        gridLayout.setVgap(5);
        textPanel.setLayout(gridLayout);
        textPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Registration Panel"));

        textPanel.add(fromAddress,BorderLayout.WEST);
        textPanel.add(fromInput,BorderLayout.CENTER);
        fromInput.setEditable(false);
        textPanel.add(username,BorderLayout.WEST);
        textPanel.add(usernameInput,BorderLayout.CENTER);
        textPanel.add(password,BorderLayout.WEST);
        textPanel.add(passwordInput,BorderLayout.CENTER);
        textPanel.add(realm,BorderLayout.WEST);
        textPanel.add(realmInput,BorderLayout.CENTER);
        realmInput.setEditable(false);
        textPanel.add(domain,BorderLayout.WEST);
        textPanel.add(domainInput,BorderLayout.CENTER);

        textPanel.setBackground(Constants.LIGHT_CYAN);
    }

    public void buildButtonPanel () {
        buttonPanel.setLayout(new FlowLayout());

        java.awt.Button loginButton = new java.awt.Button("Register");
        loginButton.setFont(new java.awt.Font("Arial", Font.BOLD, 14));
        loginButton.setBackground(Constants.DARK_CYAN);
        loginButton.setForeground(Constants.LIGHT_CYAN);

        java.awt.Button closeButton = new java.awt.Button("Close");
        closeButton.setFont(new java.awt.Font("Arial", Font.BOLD, 14));
        closeButton.setBackground(Constants.DARK_CYAN);
        closeButton.setForeground(Constants.LIGHT_CYAN);

        buttonPanel.add(loginButton, BorderLayout.PAGE_END);
        buttonPanel.add(closeButton, BorderLayout.PAGE_END);

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                phone.register(realmInput.getText(), domainInput.getText(), usernameInput.getText(), new String(passwordInput.getPassword()), fromInput.getText());
            }
        });

        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                hideWindow();
            }
        });

        buttonPanel.setBackground(Constants.LIGHT_CYAN);
    }

    @Override
    public void showErrorStatus(){
        new AlertFrame().showAlert("Wrong credentials received....Please try again.", JOptionPane.ERROR_MESSAGE);
    }

}
