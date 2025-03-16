package io.github.avx20.M4TCH;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.Input;

public class HomeScreen implements Screen {
    private final Core game;
    private Texture background;
    private Texture playButton;
    private SpriteBatch batch;
    private Viewport viewport;
    private Rectangle playButtonBounds;

    public HomeScreen(Core game) {
        this.game = game;
        this.batch = new SpriteBatch();
        this.viewport = new FitViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    @Override
    public void show() {
        Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
        background = new Texture("homescreen_bg.png");
        playButton = new Texture("play_button.png"); // Ensure this exists in the assets folder
    }

    @Override
    public void render(float delta) {
        // Handle fullscreen toggle
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.F)) {
            if (Gdx.graphics.isFullscreen()) {
                Gdx.graphics.setWindowedMode(1280, 720);
            } else {
                Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
            }
        }

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);

        batch.begin();

        float screenWidth = viewport.getWorldWidth();
        float screenHeight = viewport.getWorldHeight();

        // Background image scaling
        float imgWidth = background.getWidth();
        float imgHeight = background.getHeight();
        float scale = Math.min(screenWidth / imgWidth, screenHeight / imgHeight);
        float newWidth = imgWidth * scale;
        float newHeight = imgHeight * scale;
        float xPos = (screenWidth - newWidth) / 2;
        float yPos = (screenHeight - newHeight) / 2;
        batch.draw(background, xPos, yPos, newWidth, newHeight);

        // Play Button Scaling & Position
        float playWidth = playButton.getWidth() * 1f;
        float playHeight = playButton.getHeight() * 1f;
        float playX = (screenWidth - playWidth) / 2;
        float playY = yPos + newHeight * 0.32f;
        playButtonBounds = new Rectangle(playX, playY, playWidth, playHeight);
        batch.draw(playButton, playX, playY, playWidth, playHeight);

        batch.end();

        // Handle Play Button Click
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            float mouseX = Gdx.input.getX();
            float mouseY = Gdx.graphics.getHeight() - Gdx.input.getY(); // Flip Y-axis

            if (playButtonBounds.contains(mouseX, mouseY)) {
                game.setScreen(new FirstScreen(game)); // Switch to FirstScreen
                dispose();
            }
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.setWorldSize(width, height);
        viewport.update(width, height, true);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        background.dispose();
        playButton.dispose();
        batch.dispose();
    }
}
