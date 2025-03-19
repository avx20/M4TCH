package io.github.avx20.M4TCH.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import io.github.avx20.M4TCH.M4TCH; // Import Core

public class Lwjgl3Launcher {
    public static void main(String[] args) {
        new Lwjgl3Application(new M4TCH(), getDefaultConfiguration());
    }

    private static Lwjgl3ApplicationConfiguration getDefaultConfiguration() {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("M4TCH");
        config.setWindowedMode(800, 600);
        config.setForegroundFPS(60);
        return config;
    }
}
