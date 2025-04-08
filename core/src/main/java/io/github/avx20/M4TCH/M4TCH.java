package io.github.avx20.M4TCH;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class M4TCH extends Game {
    private SpriteBatch batch;
    private Viewport viewport;
    private PlayScreen playScreen;
    private PauseMenu pauseMenu;
    private HomeScreen homeScreen;
    private boolean paused = false; // Add the paused state

    @Override
    public void create() {
        batch = new SpriteBatch();
        viewport = new ScreenViewport();

        // 设置游戏全屏模式
        Graphics.DisplayMode displayMode = Gdx.graphics.getDisplayMode();
        Gdx.graphics.setFullscreenMode(displayMode);

        homeScreen = new HomeScreen(this);
        playScreen = new PlayScreen(this);
        pauseMenu = new PauseMenu(this, playScreen); // 传递 playScreen

        setScreen(homeScreen);
    }

    @Override
    public void render() {
        super.render();
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
        paused = false; // Reset paused state when starting a new game
    }

    public void pauseGame() {
        paused = true;
        setScreen(pauseMenu);
        if (playScreen != null) {
            playScreen.pauseGame(); // Notify PlayScreen to save its state
        }
    }

    public void resumeGame() {
        paused = false;
        setScreen(playScreen);
        if (playScreen != null) {
            playScreen.resumeGameFromPause(); // Notify PlayScreen to restore its state
        }
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }
}