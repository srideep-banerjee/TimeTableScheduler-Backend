package org.example.ui;

import org.cef.browser.CefBrowser;

import javax.swing.*;
import java.awt.*;

public class SingletonDevTools extends JFrame {
    private static SingletonDevTools instance;

    private SingletonDevTools(){
        super("Dev Tools");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    public static SingletonDevTools getInstance(){
        if(instance == null || !instance.isDisplayable()) {
            instance = new SingletonDevTools();
        }
        return instance;
    }

    public void launch(CefBrowser devToolsBrowser) {
        if(!isVisible()){
            getContentPane().add(devToolsBrowser.getUIComponent(), BorderLayout.CENTER);
            setVisible(true);
        } else {
            toFront();
        }
    }
}
