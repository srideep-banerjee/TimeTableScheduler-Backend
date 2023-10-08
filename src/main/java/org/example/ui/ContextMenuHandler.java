package org.example.ui;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.callback.CefContextMenuParams;
import org.cef.callback.CefMenuModel;
import org.cef.handler.CefContextMenuHandlerAdapter;

import java.awt.*;
import java.net.URI;

public class ContextMenuHandler extends CefContextMenuHandlerAdapter {
    private final int OPEN_IN_BROWSER = 1;
    private JcefLauncher jcefLauncher;

    public ContextMenuHandler(JcefLauncher jcefLauncher) {
        this.jcefLauncher = jcefLauncher;
    }

    @Override
    public void onBeforeContextMenu(
            CefBrowser browser, CefFrame frame, CefContextMenuParams params, CefMenuModel model
    ) {
        // Clear the default context menu
        model.clear();

        // Add a custom menu item
        model.addItem(OPEN_IN_BROWSER, "Open in browser");
        model.setEnabled(OPEN_IN_BROWSER, true);
    }

    @Override
    public boolean onContextMenuCommand(CefBrowser browser, CefFrame frame,
                                        CefContextMenuParams params, int commandId, int eventFlags
    ) {
        // Register a listener for custom menu item clicks
        switch (commandId) {
            case OPEN_IN_BROWSER -> {
                try {
                    Desktop.getDesktop().browse(new URI(jcefLauncher.url));
                } catch (Exception e) {
                    System.out.println(e);
                    return false;
                }
                return true;
            }
        }
        return false;
    }
}
