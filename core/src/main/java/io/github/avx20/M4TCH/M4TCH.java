package io.github.avx20.M4TCH;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class M4TCH extends Game {
    private SpriteBatch batch;
    private Viewport viewport;
    private PlayScreen playScreen;
    private PauseMenu pauseMenu;
    private HomeScreen homeScreen;
    private boolean paused = false;
    private float previousVolume;

    // Global volume setting
    public static float gameVolume = 0.5f;

    @Override
    public void create() {
        // Load volume setting from preferences
        loadSettings();
        previousVolume = gameVolume;

        batch = new SpriteBatch();
        viewport = new ScreenViewport();

        Graphics.DisplayMode displayMode = Gdx.graphics.getDisplayMode();
        Gdx.graphics.setFullscreenMode(displayMode);

        homeScreen = new HomeScreen(this);
        playScreen = new PlayScreen(this);
        pauseMenu = new PauseMenu(this, playScreen);

        setScreen(homeScreen);

        Gdx.app.log("M4TCH", "Game initialized with volume: " + gameVolume);
    }

    // Load settings from preferences
    private void loadSettings() {
        Preferences prefs = Gdx.app.getPreferences("M4TCHSettings");
        gameVolume = prefs.getFloat("volume", 0.5f); // Default 0.5 if not set

        // Ensure valid volume range
        if (gameVolume < 0.01f) {
            gameVolume = 0.0f;
        } else if (gameVolume > 1.0f) {
            gameVolume = 1.0f;
        }
    }

    @Override
    public void render() {
        // Check if volume has changed
        if (previousVolume != gameVolume) {
            previousVolume = gameVolume;
            notifyVolumeChange();
        }
        super.render();
    }

    // Notify all applicable screens of volume change
    private void notifyVolumeChange() {
        Screen currentScreen = getScreen();
        if (currentScreen instanceof VolumeChangeListener) {
            ((VolumeChangeListener) currentScreen).onVolumeChanged(gameVolume);
        }
    }

    // Interface for screens that need to listen to volume changes
    public interface VolumeChangeListener {
        void onVolumeChanged(float newVolume);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        if (getScreen() != null) {
            getScreen().resize(width, height);
        }
    }

    @Override
    public void dispose() {
        batch.dispose();
        if (playScreen != null) playScreen.dispose();
        if (pauseMenu != null) pauseMenu.dispose();
        if (homeScreen != null) homeScreen.dispose();
    }

    public SpriteBatch getBatch() {
        return batch;
    }

    public void startGame() {
        playScreen = new PlayScreen(this);
        setScreen(playScreen);
        paused = false;
    }

    public void pauseGame() {
        paused = true;
        if (playScreen != null) {
            playScreen.pauseGame();
        }
        setScreen(pauseMenu);
    }

    public void resumeGame() {
        paused = false;
        if (playScreen != null) {
            playScreen.resumeGameFromPause();
        }
        setScreen(playScreen);
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public PlayScreen getPlayScreen() {
        return playScreen;
    }
}
