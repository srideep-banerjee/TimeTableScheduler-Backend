package org.example.ui;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.callback.CefContextMenuParams;
import org.cef.callback.CefMenuModel;
import org.cef.handler.CefContextMenuHandlerAdapter;

import java.awt.*;
import java.net.URI;

public class ContextMenuHandler extends CefContextMenuHandlerAdapter {
    private final int INSPECT = 1;
    private final int OPEN_IN_BROWSER = 2;
    String url;
    private CefBrowser devTools;

    public ContextMenuHandler(CefBrowser devTools, String url) {
        this.devTools = devTools;
        this.url = url;
    }

    @Override
    public void onBeforeContextMenu(
            CefBrowser browser, CefFrame frame, CefContextMenuParams params, CefMenuModel model
    ) {
        // Clear the default context menu
        model.clear();

        // Add a custom menu item
        model.addItem(INSPECT, "Inspect");
        model.setEnabled(INSPECT, true);

        model.addItem(OPEN_IN_BROWSER, "Open in browser");
        model.setEnabled(OPEN_IN_BROWSER, true);
    }

    @Override
    public boolean onContextMenuCommand(CefBrowser browser, CefFrame frame,
                                        CefContextMenuParams params, int commandId, int eventFlags
    ) {
        // Register a listener for custom menu item clicks
        switch (commandId) {
            case INSPECT -> {
                /*try {
                    Desktop.getDesktop().browse(new URI(jcefLauncher.url));
                } catch (Exception e) {
                    System.out.println(e);
                    return false;
                }
                return true;*/
                SingletonDevTools.getInstance().launch(devTools);
                return true;
            }
            case OPEN_IN_BROWSER -> {
                try {
                    Desktop.getDesktop().browse(new URI(url));
                } catch (Exception e) {
                    System.out.println(e);
                    return false;
                }
                return true;
            }
        }
        return false;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
