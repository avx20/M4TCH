package io.github.avx20.M4TCH; // Update this to match your actual package

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import io.github.avx20.M4TCH.Core;  // Import your main game class

public class DesktopLauncher {
    public static void main(String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.title = "M4TCH";  // Set game title
        config.width = 800;      // Set window width
        config.height = 600;     // Set window height
        config.resizable = false; // Change if you want resizable window

        new LwjglApplication(new Core(), config);
    }
}
