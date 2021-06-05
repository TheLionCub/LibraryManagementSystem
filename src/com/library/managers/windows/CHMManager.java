package com.library.managers.windows;

import java.awt.*;
import java.io.File;
import java.io.IOException;

public class CHMManager {
    private static final String chmPath = "data/library.chm";

    public static void loadStart() {
        File file = new File(chmPath);
        try {
            Desktop.getDesktop().open(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadWithURL(String path) {
        try {
            File chmFile = new File(chmPath);
            Runtime.getRuntime().exec("hh.exe mk:@MSITStore:" + chmFile + "::/" + path + ".htm");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
