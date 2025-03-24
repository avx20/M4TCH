package io.github.avx20.M4TCH;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
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
    private Texture leaderboardButtonTexture;
    private Rectangle playAgainButtonBounds;
    private Rectangle mainMenuButtonBounds;
    private Rectangle leaderboardButtonBounds;
    private Viewport viewport;
    private BitmapFont font;
    
    // Final score and best score
    private final int finalScore;
    private int bestScore = 5000; // Default value, normally would be loaded from storage
    
    // Button click feedback
    private boolean isPlayAgainButtonClicked = false;
    private boolean isMainMenuButtonClicked = false;
    private boolean isLeaderboardButtonClicked = false;

    public GameOverScreen(M4TCH game, int finalScore) {
        this.game = game;
        this.finalScore = finalScore;
        this.viewport = new FitViewport(1920, 1080);
        this.font = new BitmapFont();
        font.getData().setScale(3.0f); // Scale up the font for better visibility

        // Load textures
        this.background = new Texture("game_over_bg.png");
        // Use the same button textures as in HomeScreen for consistency
        this.playAgainButtonTexture = new Texture("play_button.png"); // Could be reused or have a dedicated texture
        this.mainMenuButtonTexture = new Texture("exit_button.png"); // Using exit button texture for main menu
        this.leaderboardButtonTexture = new Texture("leaderboard_icon.png");

        // Initialize button bounds
        float playAgainButtonWidth = playAgainButtonTexture.getWidth();
        float playAgainButtonHeight = playAgainButtonTexture.getHeight();
        float centerX = (viewport.getWorldWidth() - playAgainButtonWidth) / 2;

        // Play Again button (centered, similar position to HomeScreen play button)
        playAgainButtonBounds = new Rectangle(centerX, 400, playAgainButtonWidth, playAgainButtonHeight);

        // Main Menu button (below Play Again)
        float mainMenuButtonWidth = mainMenuButtonTexture.getWidth() * 0.5f; // Scaling to make it appropriate size
        float mainMenuButtonHeight = mainMenuButtonTexture.getHeight() * 0.5f;
        mainMenuButtonBounds = new Rectangle(
            (viewport.getWorldWidth() - mainMenuButtonWidth) / 2, 
            280, 
            mainMenuButtonWidth, 
            mainMenuButtonHeight
        );

        // Leaderboard button (smaller icon, positioned to the side)
        float leaderboardIconSize = 100;
        leaderboardButtonBounds = new Rectangle(
            centerX + playAgainButtonWidth + 50, 
            400, 
            leaderboardIconSize, 
            leaderboardIconSize
        );
    }

    @Override
    public void render(float delta) {
        viewport.apply();
        SpriteBatch batch = game.getBatch();
        batch.setProjectionMatrix(viewport.getCamera().combined);

        batch.begin();

        // Draw the background
        batch.draw(background, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());

        // Draw Game Over text and scores
        String gameOverText = "GAME OVER";
        float gameOverX = (viewport.getWorldWidth() - font.getScaleX() * gameOverText.length() * 30) / 2;
        font.draw(batch, gameOverText, gameOverX, 800);
        
        String finalScoreText = "Your Score: " + finalScore;
        float finalScoreX = (viewport.getWorldWidth() - font.getScaleX() * finalScoreText.length() * 20) / 2;
        font.draw(batch, finalScoreText, finalScoreX, 700);
        
        String bestScoreText = "Best Score: " + (finalScore > bestScore ? finalScore : bestScore);
        float bestScoreX = (viewport.getWorldWidth() - font.getScaleX() * bestScoreText.length() * 20) / 2;
        font.draw(batch, bestScoreText, bestScoreX, 650);

        // Draw buttons with click feedback
        drawButtonWithFeedback(batch, playAgainButtonTexture, playAgainButtonBounds, isPlayAgainButtonClicked);
        drawButtonWithFeedback(batch, mainMenuButtonTexture, mainMenuButtonBounds, isMainMenuButtonClicked);
        drawButtonWithFeedback(batch, leaderboardButtonTexture, leaderboardButtonBounds, isLeaderboardButtonClicked);

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
            // Shrink the button by 20% when clicked (same as HomeScreen)
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

            if (playAgainButtonBounds.contains(touchX, touchY)) {
                isPlayAgainButtonClicked = true;
                Timer.schedule(new Timer.Task() {
                    @Override
                    public void run() {
                        isPlayAgainButtonClicked = false; // Reset the button state
                        Timer.schedule(new Timer.Task() {
                            @Override
                            public void run() {
                                game.setScreen(new PlayScreen(game)); // Start a new game
                            }
                        }, 0.1f); // Delay after the button returns to normal size
                    }
                }, 0.1f); // Delay for the shrink animation
            } else if (mainMenuButtonBounds.contains(touchX, touchY)) {
                isMainMenuButtonClicked = true;
                Timer.schedule(new Timer.Task() {
                    @Override
                    public void run() {
                        isMainMenuButtonClicked = false;
                        Timer.schedule(new Timer.Task() {
                            @Override
                            public void run() {
                                game.setScreen(new HomeScreen(game)); // Return to main menu
                            }
                        }, 0.1f);
                    }
                }, 0.1f);
            } else if (leaderboardButtonBounds.contains(touchX, touchY)) {
                isLeaderboardButtonClicked = true;
                Timer.schedule(new Timer.Task() {
                    @Override
                    public void run() {
                        isLeaderboardButtonClicked = false;
                        // If the LeaderboardScreen class exists, uncomment this:
                        // game.setScreen(new LeaderboardScreen(game)); 
                        
                        // For now, since LeaderboardScreen might not be implemented yet,
                        // we'll just log a message
                        Gdx.app.log("GameOverScreen", "Leaderboard button clicked");
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
        playAgainButtonTexture.dispose();
        mainMenuButtonTexture.dispose();
        leaderboardButtonTexture.dispose();
        font.dispose();
    }
}
