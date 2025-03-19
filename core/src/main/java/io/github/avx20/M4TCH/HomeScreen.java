package io.github.avx20.M4TCH;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class HomeScreen implements Screen {
    private final M4TCH game;
    private Texture background;
    private Texture playButtonTexture;
    private Texture settingsButtonTexture;
    private Texture leaderboardButtonTexture;
    private Texture exitButtonTexture;
    private Rectangle playButtonBounds;
    private Rectangle settingsButtonBounds;
    private Rectangle leaderboardButtonBounds;
    private Rectangle exitButtonBounds;
    private Viewport viewport;

    public HomeScreen(M4TCH game) {
        this.game = game;
        this.viewport = new FitViewport(1920, 1080);

        // Load textures
        this.background = new Texture("homescreen_bg.png");
        this.playButtonTexture = new Texture("play_button.png");
        this.settingsButtonTexture = new Texture("settings_icon.png");
        this.leaderboardButtonTexture = new Texture("leaderboard_icon.png");
        this.exitButtonTexture = new Texture("exit_button.png");

        // Initialize button bounds
        float buttonWidth = playButtonTexture.getWidth();
        float buttonHeight = playButtonTexture.getHeight();
        float centerX = (viewport.getWorldWidth() - buttonWidth) / 2;
        float spacing = 20; // Space between buttons

        playButtonBounds = new Rectangle(centerX, 300, buttonWidth, buttonHeight);
        settingsButtonBounds = new Rectangle(50, 880, buttonWidth, buttonHeight);
        leaderboardButtonBounds = new Rectangle(50, 680, buttonWidth, buttonHeight);
        exitButtonBounds = new Rectangle(1500, 750, buttonWidth, buttonHeight);
    }

    @Override
    public void render(float delta) {
        viewport.apply();
        SpriteBatch batch = game.getBatch();
        batch.setProjectionMatrix(viewport.getCamera().combined);

        batch.begin();

        // Draw the background
        batch.draw(background, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());

        // Draw buttons
        batch.draw(playButtonTexture, playButtonBounds.x, playButtonBounds.y, playButtonBounds.width, playButtonBounds.height);
        batch.draw(settingsButtonTexture, settingsButtonBounds.x, settingsButtonBounds.y, settingsButtonBounds.width, settingsButtonBounds.height);
        batch.draw(leaderboardButtonTexture, leaderboardButtonBounds.x, leaderboardButtonBounds.y, leaderboardButtonBounds.width, leaderboardButtonBounds.height);
        batch.draw(exitButtonTexture, exitButtonBounds.x, exitButtonBounds.y, exitButtonBounds.width, exitButtonBounds.height);

        batch.end();

        // Handle button clicks
        handleInput();
    }

    private void handleInput() {
        if (Gdx.input.justTouched()) {
            float touchX = Gdx.input.getX();
            float touchY = viewport.getWorldHeight() - Gdx.input.getY(); // Convert to world coordinates

            if (playButtonBounds.contains(touchX, touchY)) {
                game.setScreen(new PlayScreen(game));
            } else if (settingsButtonBounds.contains(touchX, touchY)) {
                // game.setScreen(new SettingsScreen(game));
            } else if (leaderboardButtonBounds.contains(touchX, touchY)) {
                // game.setScreen(new LeaderboardScreen(game));
            } else if (exitButtonBounds.contains(touchX, touchY)) {
                Gdx.app.exit();
            }
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void show() {}

    @Override
    public void hide() {}

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void dispose() {
        background.dispose();
        playButtonTexture.dispose();
        settingsButtonTexture.dispose();
        leaderboardButtonTexture.dispose();
        exitButtonTexture.dispose();
    }
}
