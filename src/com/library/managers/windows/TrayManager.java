package com.library.managers.windows;

import java.awt.*;

public class TrayManager {
    private static final SystemTray tray = SystemTray.getSystemTray();
    private static final Image image = Toolkit.getDefaultToolkit().createImage("icon.png");
    private static final TrayIcon trayIcon = new TrayIcon(image, "Library");

    static {
        try {
            createTrayIcon();
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    private static void createTrayIcon() throws AWTException {
        trayIcon.setImageAutoSize(true);
        tray.add(trayIcon);
    }

    public static void sendNotification(String message, TrayIcon.MessageType messageType) {
        trayIcon.displayMessage("Library", message, messageType);
    }
}
