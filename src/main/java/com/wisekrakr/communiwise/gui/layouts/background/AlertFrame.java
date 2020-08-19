package com.wisekrakr.communiwise.gui.layouts.background;

import javax.swing.*;

public class AlertFrame extends JFrame {

    public AlertFrame showAlert(String errorMessage, int errorCode){
        setUndecorated(true);

        JOptionPane.showMessageDialog(
                this,
                "<html><body><p style='width: 200px;'>"+errorMessage+"</p></body></html>",
                "Error",
                errorCode);

        setVisible(true);

        return this;
    }
}
