package io.github.avx20.M4TCH;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class GameOverScreen implements Screen {
    private final M4TCH game;
    private Texture background;
    private Texture restartButtonTexture;
    private Texture exitButtonTexture;
    private Rectangle restartButtonBounds;
    private Rectangle exitButtonBounds;
    private Viewport viewport;
    private BitmapFont font;

    // Final score and best score
    private final int finalScore;
    private int bestScore = 0; // This could load from storage

    // Button click feedback
    private boolean isRestartButtonClicked = false;
    private boolean isExitButtonClicked = false;

    // Flag to ensure we only record the score once
    private boolean scoreRecorded = false;

    // Debug variables
    private boolean debugMode = false; // Set to true to see button boundaries
    private Texture debugTexture;

    // Visual settings for the buttons (actual rendered size)
    private float restartVisualWidth;
    private float restartVisualHeight;
    private float exitVisualWidth;
    private float exitVisualHeight;
    private float restartVisualX;
    private float restartVisualY;
    private float exitVisualX;
    private float exitVisualY;

    public GameOverScreen(M4TCH game, int finalScore) {
        this.game = game;
        this.finalScore = finalScore;
        // Update best score if needed
        if (finalScore > bestScore) {
            bestScore = finalScore;
            // Could save the new high score here
        }

        this.viewport = new FitViewport(1920, 1080);
        this.font = new BitmapFont();
        font.getData().setScale(3.0f); // Scale up font for better visibility

        try {
            // Try to load the game over background image
            this.background = new Texture(Gdx.files.internal("game_over_bg.png"));
        } catch (Exception e) {
            Gdx.app.error("GameOverScreen", "Could not load game over background image", e);
            // If loading fails, background will be null and we'll handle it in render
        }

        try {
            // Load button textures using your custom images
            this.restartButtonTexture = new Texture("restart_button.png");
            this.exitButtonTexture = new Texture("exit_button.png");

            // For debug mode
            if (debugMode) {
                // Create a 1x1 white pixel texture for debug purposes
                Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
                pixmap.setColor(Color.WHITE);
                pixmap.fill();
                debugTexture = new Texture(pixmap);
                pixmap.dispose();
            }
        } catch (Exception e) {
            Gdx.app.error("GameOverScreen", "Could not load button images", e);
        }

        // Initialize button areas
        float centerX = viewport.getWorldWidth() / 2;

        // Calculate visual sizes for the buttons (for rendering)
        if (restartButtonTexture != null) {
            restartVisualWidth = restartButtonTexture.getWidth() * 0.4f; // Scale down to 40%
            restartVisualHeight = restartButtonTexture.getHeight() * 0.4f;
            restartVisualX = centerX - restartVisualWidth / 2;
            restartVisualY = 300; // Keep original position

            // Create a smaller hit area for RESTART button
            restartButtonBounds = new Rectangle(
                restartVisualX + 20, // Add padding to make hit area smaller than visual
                restartVisualY + 20,
                restartVisualWidth - 40, // Reduce width by padding on both sides
                restartVisualHeight - 40  // Reduce height by padding on both sides
            );
        }

        if (exitButtonTexture != null) {
            exitVisualWidth = exitButtonTexture.getWidth() * 0.4f;
            exitVisualHeight = exitButtonTexture.getHeight() * 0.4f;
            exitVisualX = centerX - exitVisualWidth / 2;
            exitVisualY = 200; // Keep original position

            // Create hit area for EXIT button
            exitButtonBounds = new Rectangle(
                exitVisualX,
                exitVisualY,
                exitVisualWidth,
                exitVisualHeight
            );
        }

        // Record the score to the leaderboard when the screen is created
        recordScore();
    }

    // Method to record the score to the leaderboard
    private void recordScore() {
        if (!scoreRecorded && finalScore > 0) {
            try {
                // Create a LeaderboardScreen to add the score
                LeaderboardScreen leaderboard = new LeaderboardScreen(game);
                leaderboard.addScore(finalScore);
                Gdx.app.log("GameOverScreen", "Score " + finalScore + " recorded successfully");
                scoreRecorded = true;

                // Don't dispose the leaderboard here, as it might interrupt the save operation
            } catch (Exception e) {
                Gdx.app.error("GameOverScreen", "Failed to record score", e);
            }
        }
    }

    @Override
    public void render(float delta) {
        // Clear screen
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.3f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        SpriteBatch batch = game.getBatch();
        batch.setProjectionMatrix(viewport.getCamera().combined);

        batch.begin();

        // Draw background
        if (background != null) {
            batch.draw(background, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
        }

        // Draw game over text and scores if no background image
        if (background == null) {
            font.draw(batch, "GAME OVER", viewport.getWorldWidth()/2 - 150, 800);
        }

        // Draw score info
        String finalScoreText = "Your Score: " + finalScore;
        float finalScoreX = viewport.getWorldWidth()/2 - 200;
        font.draw(batch, finalScoreText, finalScoreX, 700);

        String bestScoreText = "Best Score: " + bestScore;
        float bestScoreX = viewport.getWorldWidth()/2 - 200;
        font.draw(batch, bestScoreText, bestScoreX, 650);

        // Add a notification that the score has been recorded
        if (scoreRecorded) {
            String recordedText = "Score recorded to leaderboard!";
            float recordedX = viewport.getWorldWidth()/2 - 250;
            font.setColor(0.2f, 1f, 0.2f, 1f); // Green color
            font.draw(batch, recordedText, recordedX, 600);
            font.setColor(1f, 1f, 1f, 1f); // Reset to white
        }

        // Draw buttons
        if (restartButtonTexture != null) {
            float width = restartVisualWidth;
            float height = restartVisualHeight;
            float x = restartVisualX;
            float y = restartVisualY;

            if (isRestartButtonClicked) {
                // Shrink button by 20% when clicked
                width *= 0.8f;
                height *= 0.8f;
                x += (restartVisualWidth - width) / 2;
                y += (restartVisualHeight - height) / 2;
            }
            batch.draw(restartButtonTexture, x, y, width, height);

            // DEBUG: Draw RESTART button bounds
            if (debugMode && debugTexture != null) {
                batch.setColor(1, 0, 0, 0.5f); // Semi-transparent red
                batch.draw(debugTexture, restartButtonBounds.x, restartButtonBounds.y,
                    restartButtonBounds.width, restartButtonBounds.height);
            }
        }

        if (exitButtonTexture != null) {
            float width = exitVisualWidth;
            float height = exitVisualHeight;
            float x = exitVisualX;
            float y = exitVisualY;

            if (isExitButtonClicked) {
                // Shrink button by 20% when clicked
                width *= 0.8f;
                height *= 0.8f;
                x += (exitVisualWidth - width) / 2;
                y += (exitVisualHeight - height) / 2;
            }
            batch.draw(exitButtonTexture, x, y, width, height);

            // DEBUG: Draw EXIT button bounds
            if (debugMode && debugTexture != null) {
                batch.setColor(0, 1, 0, 0.5f); // Semi-transparent green
                batch.draw(debugTexture, exitButtonBounds.x, exitButtonBounds.y,
                    exitButtonBounds.width, exitButtonBounds.height);
                batch.setColor(1, 1, 1, 1); // Reset color
            }
        }

        batch.end();

        // Handle button input
        handleInput();
    }

    private void handleInput() {
        if (Gdx.input.justTouched()) {
            // Get screen coordinates
            float screenX = Gdx.input.getX();
            float screenY = Gdx.input.getY();

            // Convert screen coordinates to world coordinates
            Vector3 worldCoords = viewport.unproject(new Vector3(screenX, screenY, 0));
            float worldX = worldCoords.x;
            float worldY = worldCoords.y;

            // Debug logging
            if (debugMode) {
                Gdx.app.log("GameOverScreen", "Touch at: " + worldX + ", " + worldY);
            }

            // Check for button clicks
            // 1. First check if click is in the EXIT button area
            if (exitButtonBounds != null && exitButtonBounds.contains(worldX, worldY)) {
                isExitButtonClicked = true;
                Timer.schedule(new Timer.Task() {
                    @Override
                    public void run() {
                        isExitButtonClicked = false;
                        Timer.schedule(new Timer.Task() {
                            @Override
                            public void run() {
                                game.setScreen(new HomeScreen(game));
                                dispose();
                            }
                        }, 0.1f);
                    }
                }, 0.1f);
            }
            // 2. Only check RESTART if we didn't hit EXIT
            else if (restartButtonBounds != null && restartButtonBounds.contains(worldX, worldY)) {
                isRestartButtonClicked = true;
                Timer.schedule(new Timer.Task() {
                    @Override
                    public void run() {
                        isRestartButtonClicked = false;
                        Timer.schedule(new Timer.Task() {
                            @Override
                            public void run() {
                                game.setScreen(new PlayScreen(game));
                                dispose();
                            }
                        }, 0.1f);
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
        font.dispose();
        if (background != null) {
            background.dispose();
        }
        if (restartButtonTexture != null) {
            restartButtonTexture.dispose();
        }
        if (exitButtonTexture != null) {
            exitButtonTexture.dispose();
        }
        if (debugMode && debugTexture != null) {
            debugTexture.dispose();
        }
    }
}
