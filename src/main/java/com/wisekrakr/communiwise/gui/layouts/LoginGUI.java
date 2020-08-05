package com.wisekrakr.communiwise.gui.layouts;

import com.wisekrakr.communiwise.gui.layouts.utils.Constants;
import com.wisekrakr.communiwise.gui.ext.AbstractScreen;
import com.wisekrakr.communiwise.phone.device.PhoneAPI;
import com.wisekrakr.communiwise.gui.layouts.objects.Button;

import javax.sip.address.Address;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginGUI extends AbstractScreen {
    private final PhoneAPI phoneAPI;

    private final JPanel textPanel = new JPanel();
    private final JPanel buttonPanel = new JPanel();

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


    public LoginGUI(PhoneAPI phoneAPI) throws HeadlessException {
        this.phoneAPI = phoneAPI;
    }

    private static final int DESIRED_HEIGHT = 300;
    private static final int DESIRED_WIDTH = 500;


    @Override
    public void showWindow() {
        setTitle("Login to CommUniWise");

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width - DESIRED_WIDTH) / 2, (screenSize.height - DESIRED_HEIGHT) / 2, DESIRED_WIDTH, DESIRED_HEIGHT);
        getRootPane().setBorder(BorderFactory.createMatteBorder(4,4,4,4, Constants.SUNSET_ORANGE));

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        initComponents();
        buildTextPanel();
        buildButtonPanel();

        setLayout(new BorderLayout());

        add(textPanel,BorderLayout.NORTH);
        add(buttonPanel,BorderLayout.CENTER);

        setVisible(true);

    }

    private void initComponents(){
        fromInput = new JTextField("sip:damian2@asterisk.interzone <Damian 2>");
        domainInput = new JTextField("asterisk.interzone");
        realmInput = new JTextField("asterisk");
        usernameInput = new JTextField("damian2");
        passwordInput = new JPasswordField("45jf83f");
    }

    public void buildTextPanel () {
        GridLayout gridLayout = new GridLayout(6, 2);
        gridLayout.setVgap(5);
        textPanel.setLayout(gridLayout);
        textPanel.setBorder(new EmptyBorder(10,10,10,10));


        fromInput.setText("sip:"+ usernameInput.getText() + "@" + domainInput.getText());

        textPanel.add(fromAddress,BorderLayout.WEST);
        textPanel.add(fromInput,BorderLayout.CENTER);
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

        Button loginButton = new Button("Login", 10, 160);

        buttonPanel.add(loginButton);

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                phoneAPI.register(realmInput.getText(), domainInput.getText(), usernameInput.getText(), new String(passwordInput.getPassword()), fromInput.getText());
            }
        });

        buttonPanel.setBackground(Color.LIGHT_GRAY);
    }


}
