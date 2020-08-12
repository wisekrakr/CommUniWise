package com.wisekrakr.communiwise.gui.layouts;

import com.sun.xml.internal.bind.v2.runtime.reflect.opt.Const;
import com.wisekrakr.communiwise.gui.layouts.utils.Constants;
import com.wisekrakr.communiwise.gui.ext.AbstractScreen;
import com.wisekrakr.communiwise.gui.layouts.utils.FrameDragListener;
import com.wisekrakr.communiwise.phone.device.PhoneAPI;
import com.wisekrakr.communiwise.gui.layouts.objects.Button;

import javax.imageio.ImageIO;
import javax.sip.address.Address;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class LoginGUI extends AbstractScreen {
    private final PhoneAPI phoneAPI;

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

    public LoginGUI(PhoneAPI phoneAPI) throws HeadlessException {
        this.phoneAPI = phoneAPI;
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
        fromInput = new JTextField("sip:damian2@asterisk.interzone <Damian 2>");
        domainInput = new JTextField("asterisk.interzone");
        realmInput = new JTextField("asterisk");
        usernameInput = new JTextField("damian2");
        passwordInput = new JPasswordField("45jf83f");
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

        fromInput.setText("sip:"+ usernameInput.getText() + "@" + domainInput.getText());

        textPanel.add(fromAddress,BorderLayout.WEST);
        textPanel.add(fromInput,BorderLayout.CENTER);
        fromInput.setEditable(false);
        textPanel.add(username,BorderLayout.WEST);
        textPanel.add(usernameInput,BorderLayout.CENTER);
        textPanel.add(password,BorderLayout.WEST);
        textPanel.add(passwordInput,BorderLayout.CENTER);
        textPanel.add(realm,BorderLayout.WEST);
        textPanel.add(realmInput,BorderLayout.CENTER);
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
                phoneAPI.register(realmInput.getText(), domainInput.getText(), usernameInput.getText(), new String(passwordInput.getPassword()), fromInput.getText());
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


}
