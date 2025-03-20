package io.github.avx20.M4TCH;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Timer;
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

    // Variables for button click feedback
    private boolean isPlayButtonClicked = false;
    private boolean isSettingsButtonClicked = false;
    private boolean isLeaderboardButtonClicked = false;
    private boolean isExitButtonClicked = false;

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
        float playButtonWidth = playButtonTexture.getWidth();
        float playButtonHeight = playButtonTexture.getHeight();
        float centerX = (viewport.getWorldWidth() - playButtonWidth) / 2;

        // Play button (unchanged)
        playButtonBounds = new Rectangle(centerX, 330, playButtonWidth, playButtonHeight);

        // Settings and Leaderboard icons (smaller and perfect squares)
        float iconSize = 100; // Size for both settings and leaderboard icons
        settingsButtonBounds = new Rectangle(5, 975, iconSize, iconSize);
        leaderboardButtonBounds = new Rectangle(5, 860, iconSize, iconSize);

        // Exit button
        float exitButtonOriginalWidth = exitButtonTexture.getWidth();
        float exitButtonOriginalHeight = exitButtonTexture.getHeight();
        float exitButtonNewWidth = exitButtonOriginalWidth * 0.3f; // Reduce width by 70%
        float exitButtonNewHeight = exitButtonOriginalHeight * 0.25f; // Reduce height by 70%
        exitButtonBounds = new Rectangle(1750, -18, exitButtonNewWidth, exitButtonNewHeight);
    }

    @Override
    public void render(float delta) {
        viewport.apply();
        SpriteBatch batch = game.getBatch();
        batch.setProjectionMatrix(viewport.getCamera().combined);

        batch.begin();

        // Draw the background
        batch.draw(background, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());

        // Draw buttons with click feedback
        drawButtonWithFeedback(batch, playButtonTexture, playButtonBounds, isPlayButtonClicked);
        drawButtonWithFeedback(batch, settingsButtonTexture, settingsButtonBounds, isSettingsButtonClicked);
        drawButtonWithFeedback(batch, leaderboardButtonTexture, leaderboardButtonBounds, isLeaderboardButtonClicked);
        drawButtonWithFeedback(batch, exitButtonTexture, exitButtonBounds, isExitButtonClicked);

        batch.end();

        // Handle button clicks
        handleInput();
    }

    private void drawButtonWithFeedback(SpriteBatch batch, Texture texture, Rectangle bounds, boolean isClicked) {
        float width = bounds.width;
        float height = bounds.height;
        float x = bounds.x;
        float y = bounds.y;

        if (isClicked) {
            // Shrink the button by 20% when clicked
            width *= 0.8f;
            height *= 0.8f;
            x += (bounds.width - width) / 2; // Center the button horizontally
            y += (bounds.height - height) / 2; // Center the button vertically
        }

        // Draw the button
        batch.draw(texture, x, y, width, height);
    }

    private void handleInput() {
        if (Gdx.input.justTouched()) {
            float touchX = Gdx.input.getX();
            float touchY = viewport.getWorldHeight() - Gdx.input.getY(); // Convert to world coordinates

            if (playButtonBounds.contains(touchX, touchY)) {
                isPlayButtonClicked = true;
                Timer.schedule(new Timer.Task() {
                    @Override
                    public void run() {
                        isPlayButtonClicked = false; // Reset the button state
                        Timer.schedule(new Timer.Task() {
                            @Override
                            public void run() {
                                game.setScreen(new PlayScreen(game)); // Transition to PlayScreen
                            }
                        }, 0.1f); // Delay after the button returns to normal size
                    }
                }, 0.1f); // Delay for the shrink animation
            } else if (settingsButtonBounds.contains(touchX, touchY)) {
                isSettingsButtonClicked = true;
                Timer.schedule(new Timer.Task() {
                    @Override
                    public void run() {
                        isSettingsButtonClicked = false;
                        // game.setScreen(new SettingsScreen(game));
                    }
                }, 0.1f);
            } else if (leaderboardButtonBounds.contains(touchX, touchY)) {
                isLeaderboardButtonClicked = true;
                Timer.schedule(new Timer.Task() {
                    @Override
                    public void run() {
                        isLeaderboardButtonClicked = false;
                        // game.setScreen(new LeaderboardScreen(game));
                    }
                }, 0.1f);
            } else if (exitButtonBounds.contains(touchX, touchY)) {
                isExitButtonClicked = true;
                Timer.schedule(new Timer.Task() {
                    @Override
                    public void run() {
                        isExitButtonClicked = false;
                        Gdx.app.exit();
                    }
                }, 0.1f);
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
