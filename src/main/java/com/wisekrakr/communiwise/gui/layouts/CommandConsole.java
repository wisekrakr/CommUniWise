package com.wisekrakr.communiwise.gui.layouts;

import org.beryx.textio.TextIO;
import org.beryx.textio.TextIoFactory;
import org.beryx.textio.TextTerminal;

public class CommandConsole {

    //Command line
    private final TextIO textIO;

    //Input fields
    private String domain;

    public CommandConsole() {

        textIO = TextIoFactory.getTextIO();

    }

    public void start() {
        TextTerminal<?> terminal = textIO.getTextTerminal();
        terminal.println(" ═════════════════════════════════ஜ۩۞۩ஜ═════════════════════════════════ \n" +
                                     "                         Welcome to CommUniWise \n " +
                         "═════════════════════════════════ஜ۩۞۩ஜ═════════════════════════════════ ");

        domain = textIO.newStringInputReader().withDefaultValue("asterisk.interzone").read("What domain would you like to use? ( i.e. asterisk.local)");
    }

    public void stop() {
        textIO.dispose();
    }

    public String getDomain() {
        return domain;
    }
}
