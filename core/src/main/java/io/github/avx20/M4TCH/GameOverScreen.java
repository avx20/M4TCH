package io.github.avx20.M4TCH;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class GameOverScreen implements Screen {
    private final M4TCH game;
    private Texture background;
    private Texture playAgainButtonTexture;
    private Texture mainMenuButtonTexture;
    private Rectangle playAgainButtonBounds;
    private Rectangle mainMenuButtonBounds;
    private Viewport viewport;
    private BitmapFont font;

    // Final score and best score
    private final int finalScore;
    private int bestScore = 0; // Can load high score from storage

    // Button click feedback
    private boolean isPlayAgainButtonClicked = false;
    private boolean isMainMenuButtonClicked = false;

    public GameOverScreen(M4TCH game, int finalScore) {
        this.game = game;
        this.finalScore = finalScore;
        // Update high score
        if (finalScore > bestScore) {
            bestScore = finalScore;
            // Can save new high score here
        }

        this.viewport = new FitViewport(1920, 1080);
        this.font = new BitmapFont();
        font.getData().setScale(3.0f); // Enlarge font for better display

        try {
            // Try to load game over background image, create solid color background if not exists
            this.background = new Texture(Gdx.files.internal("game_over_bg.png"));
        } catch (Exception e) {
            Gdx.app.error("GameOverScreen", "Unable to load game over background image, using solid color background", e);
            // If loading fails, background will remain null, we'll handle this in render
        }

        try {
            // Load button textures using known existing resources
            this.playAgainButtonTexture = new Texture("play_button.png");
            this.mainMenuButtonTexture = new Texture("exit_button.png");
        } catch (Exception e) {
            Gdx.app.error("GameOverScreen", "Unable to load button images", e);
            // Similarly, if loading fails, we'll handle this in render
        }

        // Initialize button areas
        float centerX = viewport.getWorldWidth() / 2;

        // Play Again button (if texture is successfully loaded)
        if (playAgainButtonTexture != null) {
            float playButtonWidth = playAgainButtonTexture.getWidth();
            float playButtonHeight = playAgainButtonTexture.getHeight();
            playAgainButtonBounds = new Rectangle(
                centerX - playButtonWidth / 2,
                400,
                playButtonWidth,
                playButtonHeight
            );
        }

        // Main Menu button (if texture is successfully loaded)
        if (mainMenuButtonTexture != null) {
            float menuButtonWidth = mainMenuButtonTexture.getWidth() * 0.5f; // Shrink button
            float menuButtonHeight = mainMenuButtonTexture.getHeight() * 0.5f;
            mainMenuButtonBounds = new Rectangle(
                centerX - menuButtonWidth / 2,
                280,
                menuButtonWidth,
                menuButtonHeight
            );
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

        // Draw game over text and score
        // If background image already contains "GAME OVER" text, this line can be omitted
        if (background == null) { // Only draw text when there's no background image
            font.draw(batch, "GAME OVER", viewport.getWorldWidth()/2 - 150, 800);
        }

        String finalScoreText = "Your Score: " + finalScore;
        float finalScoreX = viewport.getWorldWidth()/2 - 200;
        font.draw(batch, finalScoreText, finalScoreX, 700);

        String bestScoreText = "Best Score: " + bestScore;
        float bestScoreX = viewport.getWorldWidth()/2 - 200;
        font.draw(batch, bestScoreText, bestScoreX, 650);

        // Draw buttons (with click feedback effect)
        if (playAgainButtonTexture != null && playAgainButtonBounds != null) {
            drawButtonWithFeedback(batch, playAgainButtonTexture, playAgainButtonBounds, isPlayAgainButtonClicked);
        } else {
            // Draw simple text button if button texture fails to load
            font.draw(batch, "Play Again", viewport.getWorldWidth()/2 - 100, 400);
        }

        if (mainMenuButtonTexture != null && mainMenuButtonBounds != null) {
            drawButtonWithFeedback(batch, mainMenuButtonTexture, mainMenuButtonBounds, isMainMenuButtonClicked);
        } else {
            // Draw simple text button if button texture fails to load
            font.draw(batch, "Main Menu", viewport.getWorldWidth()/2 - 100, 280);
        }

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
            // Shrink button by 20% when clicked
            width *= 0.8f;
            height *= 0.8f;
            x += (bounds.width - width) / 2; // Center
            y += (bounds.height - height) / 2;
        }

        // Draw button
        batch.draw(texture, x, y, width, height);
    }

    private void handleInput() {
        if (Gdx.input.justTouched()) {
            float touchX = Gdx.input.getX();
            float touchY = viewport.getWorldHeight() - Gdx.input.getY(); // Convert to world coordinates

            // Check if Play Again button is clicked
            if (playAgainButtonBounds != null && playAgainButtonBounds.contains(touchX, touchY)) {
                isPlayAgainButtonClicked = true;
                Timer.schedule(new Timer.Task() {
                    @Override
                    public void run() {
                        isPlayAgainButtonClicked = false; // Reset button state
                        Timer.schedule(new Timer.Task() {
                            @Override
                            public void run() {
                                game.setScreen(new PlayScreen(game)); // Start new game
                                dispose();
                            }
                        }, 0.1f); // Delay after button returns to normal size
                    }
                }, 0.1f); // Button shrink animation duration
            }
            // Check if Main Menu button is clicked
            else if (mainMenuButtonBounds != null && mainMenuButtonBounds.contains(touchX, touchY)) {
                isMainMenuButtonClicked = true;
                Timer.schedule(new Timer.Task() {
                    @Override
                    public void run() {
                        isMainMenuButtonClicked = false;
                        Timer.schedule(new Timer.Task() {
                            @Override
                            public void run() {
                                game.setScreen(new HomeScreen(game)); // Return to main menu
                                dispose();
                            }
                        }, 0.1f);
                    }
                }, 0.1f);
            }
            // If no button textures are loaded, any click returns to main menu
            else if (playAgainButtonTexture == null || mainMenuButtonTexture == null) {
                game.setScreen(new HomeScreen(game));
                dispose();
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
        if (playAgainButtonTexture != null) {
            playAgainButtonTexture.dispose();
        }
        if (mainMenuButtonTexture != null) {
            mainMenuButtonTexture.dispose();
        }
    }
}
