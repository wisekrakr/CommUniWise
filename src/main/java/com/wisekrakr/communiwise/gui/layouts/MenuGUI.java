package com.wisekrakr.communiwise.gui.layouts;

import com.wisekrakr.communiwise.gui.ext.AbstractScreen;
import com.wisekrakr.communiwise.gui.layouts.panes.main.OptionsPane;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class MenuGUI {

    private JFrame mainFrame;

    public MenuGUI(JFrame mainFrame) {
        this.mainFrame = mainFrame;
    }

    public void init() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Throwable e) {
            System.out.println("WARNING: unable to set look and feel, will continue");
        }

        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("File");
        menu.setMnemonic('F');
        JMenuItem menuItem = new JMenuItem("Exit");
        menuItem.setMnemonic('x');
//        menuItem.setActionCommand(EventManager.ACTION_EXIT);
//
//        menuItem.addActionListener(eventManager);
        menu.add(menuItem);
        menuBar.add(menu);

        menu = new JMenu("Edit");
        menu.setMnemonic('E');
        menuItem = new JMenuItem("Account");
        menuItem.setMnemonic('A');
//        menuItem.setActionCommand(EventManager.ACTION_ACCOUNT);
//        menuItem.addActionListener(eventManager);
        menu.add(menuItem);
        menuItem = new JMenuItem("Preferences");
        menuItem.setMnemonic('P');
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                OptionsPane optionsPane = new OptionsPane(mainFrame);
                optionsPane.init();
            }
        });
        menu.add(menuItem);
        menuBar.add(menu);

        menu = new JMenu("Help");
        menu.setMnemonic('H');
        menuItem = new JMenuItem("User manual");
        menuItem.setMnemonic('D');
//        menuItem.setActionCommand(EventManager.ACTION_DOCUMENTATION);
//        menuItem.addActionListener(eventManager);
        menu.add(menuItem);
        menuItem = new JMenuItem("About");
        menuItem.setMnemonic('A');
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                AboutFrame aboutFrame = new AboutFrame();
                aboutFrame.setVisible(true);
            }
        });
        menu.add(menuItem);
        menuBar.add(menu);

        mainFrame.setJMenuBar(menuBar);
    }




}
