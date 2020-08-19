package com.wisekrakr.communiwise.gui.layouts.utils;

import javax.swing.*;

public class InputValidator extends InputVerifier{

    @Override
    public boolean verify(JComponent input) {
        return !input.toString().equals("") && input.toString() != null;
    }


}



