package com.wisekrakr.communiwise.gui.layouts.background;

import javax.swing.*;
import java.awt.*;


public class AlertFrame  {

    public AlertFrame showAlert(Component component,String errorMessage, int errorCode){


        JOptionPane.showMessageDialog(
                component,
                "<html><body><p style='width: 200px;'>"+errorMessage+"</p></body></html>",
                "Error",
                errorCode);



        return this;
    }


}
