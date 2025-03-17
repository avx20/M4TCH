package io.github.avx20.M4TCH;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class PlayScreen implements Screen {
    private final Core game;
    private SpriteBatch batch;
    private Texture gameBackground;
    private BitmapFont font;
    private Grid grid; // Add the grid instance

    private float timeRemaining = 60; // 60-second countdown
    private int score = 0;

    public PlayScreen(Core game) {
        this.game = game;
        this.batch = new SpriteBatch();

        // Load assets from the asset manager
        this.gameBackground = game.getAssetManager().get("game_bg.png", Texture.class);

        // Load font
        font = new BitmapFont(); // Use default font for now

        // Initialize the grid
        grid = new Grid();
        grid.spawnTiles(); // Spawn tiles when the game starts
    }

    @Override
    public void show() {
        // Called when this screen becomes the current screen
    }

    @Override
    public void render(float delta) {
        // Update the countdown timer
        timeRemaining -= delta;
        if (timeRemaining <= 0) {
            timeRemaining = 0;
            // TODO: Handle game over scenario
        }

        // Clear the screen
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        batch.draw(gameBackground, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // Draw UI elements (timer & score)
        font.draw(batch, "Time: " + (int) timeRemaining, 50, Gdx.graphics.getHeight() - 50);
        font.draw(batch, "Score: " + score, 50, Gdx.graphics.getHeight() - 100);

        // Draw the 4x4 grid and tiles
        grid.render(batch, delta); // Render the grid and tiles

        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        // Called when the window is resized
    }

    @Override
    public void pause() {
        // Called when the game is paused
    }

    @Override
    public void resume() {
        // Called when the game is resumed
    }

    @Override
    public void hide() {
        // Called when this screen is no longer the current screen
    }

    @Override
    public void dispose() {
        // Clean up resources
        batch.dispose();
        font.dispose();
        grid.dispose(); // Dispose grid resources
    }
}
