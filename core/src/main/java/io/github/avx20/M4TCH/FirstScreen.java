package io.github.avx20.M4TCH;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class FirstScreen implements Screen {
    private Core game;
    private SpriteBatch batch;
    private Texture background;

    public FirstScreen(Core game) {  // Constructor to accept Core instance
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();

        // Check if file exists before loading
        if (!Gdx.files.internal("homescreen_bg.png").exists()) {
            Gdx.app.error("File Error", "homescreen_bg.png not found in assets folder!");
        }

        // Load the texture (assuming it's directly in assets/)
        background = new Texture("homescreen_bg.png");
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        batch.draw(background, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.end();
    }

    @Override
    public void resize(int width, int height) {}

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        background.dispose();
    }
}
