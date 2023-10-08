package org.example.ui;

import me.friwi.jcefmaven.CefAppBuilder;
import me.friwi.jcefmaven.CefInitializationException;
import me.friwi.jcefmaven.UnsupportedPlatformException;
import org.cef.CefApp;
import org.cef.CefClient;
import org.cef.CefSettings;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.handler.CefDisplayHandler;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class JcefLauncher {
    String url;

    public JcefLauncher(String url) {
        this.url = url;
    }

    public void launch() {
        //Create a new CefAppBuilder instance
        CefAppBuilder builder = new CefAppBuilder();

        //Configure the builder instance
        builder.getCefSettings().windowless_rendering_enabled = false;

        //Build a CefApp instance using the configuration above
        CefApp app=null;
        try {
            app = builder.build();
        } catch (IOException | InterruptedException | CefInitializationException | UnsupportedPlatformException e) {
            System.out.println(e);
            System.exit(1);
        }

        // Create a CefClient instance
        CefClient client = app.createClient();

        // Create a CefBrowser instance
        CefBrowser browser = client.createBrowser(this.url, false, false);

        final ContextMenuHandler contextMenuHandler = new ContextMenuHandler(browser.getDevTools(), this.url);
        client.addContextMenuHandler(contextMenuHandler);

        client.addDisplayHandler(new CefDisplayHandler() {
            @Override
            public void onAddressChange(CefBrowser browser, CefFrame frame, String url) {
                contextMenuHandler.setUrl(url);
            }

            @Override
            public void onTitleChange(CefBrowser browser, String title) {

            }

            @Override
            public boolean onTooltip(CefBrowser browser, String text) {
                return false;
            }

            @Override
            public void onStatusMessage(CefBrowser browser, String value) {

            }

            @Override
            public boolean onConsoleMessage(CefBrowser browser, CefSettings.LogSeverity level, String message, String source, int line) {
                return false;
            }

            @Override
            public boolean onCursorChange(CefBrowser browser, int cursorType) {
                return false;
            }
        });

        // Create a JFrame to host the browser
        JFrame frame = new JFrame("Time Table Scheduler");
        frame.setSize(1260,700);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(browser.getUIComponent(), BorderLayout.CENTER);
        frame.setVisible(true);
    }
}
