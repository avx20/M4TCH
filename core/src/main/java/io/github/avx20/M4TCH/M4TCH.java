package io.github.avx20.M4TCH;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;

public class M4TCH extends Game {
    private SpriteBatch batch;
    private Viewport viewport;

    @Override
    public void create() {
        batch = new SpriteBatch();
        viewport = new ScreenViewport();

        // Set the game to launch in fullscreen mode
        Graphics.DisplayMode displayMode = Gdx.graphics.getDisplayMode();
        Gdx.graphics.setFullscreenMode(displayMode);

        // Set the initial screen
        setScreen(new HomeScreen(this));
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
    }

    public SpriteBatch getBatch() {
        return batch;
    }
}
