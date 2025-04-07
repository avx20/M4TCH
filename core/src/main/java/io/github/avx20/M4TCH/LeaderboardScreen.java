package io.github.avx20.M4TCH;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class LeaderboardScreen implements Screen {
    private final M4TCH game;
    private SpriteBatch batch;
    private Viewport viewport;
    private Texture backgroundTexture;
    private Texture leaderboardFrameTexture;
    private Texture backButtonTexture;
    private Rectangle backButtonBounds;
    private BitmapFont titleFont;
    private BitmapFont entryFont;
    private Array<LeaderboardEntry> leaderboardEntries;
    
    private boolean isBackButtonClicked = false;

    // File to store leaderboard data
    private static final String LEADERBOARD_FILE = "core/assets/leaderboard.json";

    public LeaderboardScreen(M4TCH game) {
        this.game = game;
        
        // Initialize
        batch = new SpriteBatch();
        viewport = new FitViewport(1920, 1080);
        
        // Load textures
        try {
            backgroundTexture = new Texture(Gdx.files.internal("game_bg.png"));
            leaderboardFrameTexture = new Texture("leaderboard_frame.png");
            backButtonTexture = new Texture("back_button.png");
        } catch (Exception e) {
            Gdx.app.error("LeaderboardScreen", "Error loading textures", e);
            // Create fallback textures if needed
            try {
                if (backButtonTexture == null) {
                    backButtonTexture = new Texture("exit_button.png"); // Try to use exit button as fallback
                }
            } catch (Exception ex) {
                Gdx.app.error("LeaderboardScreen", "Failed to load fallback textures", ex);
            }
        }
        
        // Initialize back button
        if (backButtonTexture != null) {
            float buttonSize = 100;
            backButtonBounds = new Rectangle(50, viewport.getWorldHeight() - 150, buttonSize, buttonSize);
        }
        
        // Initialize fonts
        try {
            titleFont = new BitmapFont(Gdx.files.internal("core/assets/uiskin.atlas"), Gdx.files.internal("core/assets/font.ttf"), false);
            titleFont.setColor(Color.WHITE);
            titleFont.getData().setScale(2f);
            
            entryFont = new BitmapFont(Gdx.files.internal("core/assets/uiskin.atlas"), Gdx.files.internal("core/assets/font.ttf"), false);
            entryFont.setColor(Color.YELLOW);
            entryFont.getData().setScale(1.5f);
        } catch (Exception e) {
            Gdx.app.error("LeaderboardScreen", "Error loading fonts", e);
            // Fallback to default bitmap font if custom font fails
            titleFont = new BitmapFont();
            entryFont = new BitmapFont();
        }
        
        // Load leaderboard entries
        leaderboardEntries = loadLeaderboardEntries();
    }

    // Method to add a new score to leaderboard
    public void addScore(int score) {
        leaderboardEntries.add(new LeaderboardEntry("Player", score));
        sortAndTrimLeaderboard();
        saveLeaderboardEntries();
    }

    private void sortAndTrimLeaderboard() {
        // Sort in descending order
        leaderboardEntries.sort((a, b) -> Integer.compare(b.score, a.score));
        
        // Keep only top 10 entries
        if (leaderboardEntries.size > 10) {
            leaderboardEntries.truncate(10);
        }
    }

    private Array<LeaderboardEntry> loadLeaderboardEntries() {
        Array<LeaderboardEntry> entries = new Array<>();
        try {
            File file = new File(LEADERBOARD_FILE);
            if (!file.exists()) {
                // Create default entries
                entries.addAll(
                    new LeaderboardEntry("AAA", 5000),
                    new LeaderboardEntry("BBB", 4000),
                    new LeaderboardEntry("CCC", 3000),
                    new LeaderboardEntry("DDD", 2000),
                    new LeaderboardEntry("EEE", 1000)
                );
                saveLeaderboardEntries();
                return entries;
            }

            JsonReader jsonReader = new JsonReader();
            JsonValue base = jsonReader.parse(new FileReader(file));
            
            for (JsonValue entry = base.child; entry != null; entry = entry.next) {
                entries.add(new LeaderboardEntry(
                    entry.getString("name"),
                    entry.getInt("score")
                ));
            }
        } catch (Exception e) {
            Gdx.app.error("LeaderboardScreen", "Error loading leaderboard", e);
        }
        return entries;
    }

    private void saveLeaderboardEntries() {
        try (FileWriter writer = new FileWriter(LEADERBOARD_FILE)) {
            Json json = new Json();
            json.setOutputType(JsonWriter.OutputType.json);
            writer.write(json.toJson(leaderboardEntries));
        } catch (IOException e) {
            Gdx.app.error("LeaderboardScreen", "Error saving leaderboard", e);
        }
    }

    @Override
    public void render(float delta) {
        // Clear to a dark blue-gray background instead of black
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);

        batch.begin();

        // Draw background - ensure it covers the entire screen
        if (backgroundTexture != null) {
            batch.draw(backgroundTexture, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
        }

        // Draw leaderboard frame
        if (leaderboardFrameTexture != null) {
            batch.draw(leaderboardFrameTexture, 
                viewport.getWorldWidth() / 2 - leaderboardFrameTexture.getWidth() / 2, 
                viewport.getWorldHeight() / 2 - leaderboardFrameTexture.getHeight() / 2
            );
        }

        // Draw back button with feedback
        if (backButtonTexture != null) {
            float width = backButtonBounds.width;
            float height = backButtonBounds.height;
            float x = backButtonBounds.x;
            float y = backButtonBounds.y;

            if (isBackButtonClicked) {
                // Shrink the button by 20% when clicked
                width *= 0.8f;
                height *= 0.8f;
                x += (backButtonBounds.width - width) / 2;
                y += (backButtonBounds.height - height) / 2;
            }

            batch.draw(backButtonTexture, x, y, width, height);
        }

        // Draw title
        titleFont.draw(batch, "LEADERBOARD", 
            viewport.getWorldWidth() / 2 - 200, 
            viewport.getWorldHeight() - 100
        );

        // Draw leaderboard entries
        float startY = viewport.getWorldHeight() - 250;
        for (int i = 0; i < leaderboardEntries.size; i++) {
            LeaderboardEntry entry = leaderboardEntries.get(i);
            String entryText = String.format("%d. %s - %d", 
                i + 1, entry.name, entry.score);
            
            // Color entries differently based on ranking
            if (i == 0) entryFont.setColor(1f, 0.84f, 0f, 1f); // Gold color
            else if (i == 1) entryFont.setColor(0.75f, 0.75f, 0.75f, 1f); // Silver color (light gray)
            else if (i == 2) entryFont.setColor(0.65f, 0.16f, 0.16f, 1f); // Bronze/dark red color
            else entryFont.setColor(Color.WHITE);

            entryFont.draw(batch, entryText, 
                viewport.getWorldWidth() / 2 - 200, 
                startY - (i * 70)
            );
        }

        batch.end();

        // Handle input
        handleInput();
    }

    private void handleInput() {
        // Return to HomeScreen when ESC is pressed
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new HomeScreen(game));
            dispose();
        }
        
        // Handle back button click
        if (Gdx.input.justTouched() && backButtonTexture != null) {
            // Get screen coordinates
            float screenX = Gdx.input.getX();
            float screenY = Gdx.input.getY();

            // Convert screen coordinates to world coordinates
            Vector3 worldCoords = viewport.unproject(new Vector3(screenX, screenY, 0));
            float worldX = worldCoords.x;
            float worldY = worldCoords.y;

            if (backButtonBounds != null && backButtonBounds.contains(worldX, worldY)) {
                isBackButtonClicked = true;
                Timer.schedule(new Timer.Task() {
                    @Override
                    public void run() {
                        isBackButtonClicked = false;
                        Timer.schedule(new Timer.Task() {
                            @Override
                            public void run() {
                                game.setScreen(new LoadingScreen(game, new HomeScreen(game)));
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
    public void dispose() {
        batch.dispose();
        if (backgroundTexture != null) backgroundTexture.dispose();
        if (leaderboardFrameTexture != null) leaderboardFrameTexture.dispose();
        if (backButtonTexture != null) backButtonTexture.dispose();
        titleFont.dispose();
        entryFont.dispose();
    }

    // Helper class for leaderboard entries
    private static class LeaderboardEntry {
        String name;
        int score;

        LeaderboardEntry(String name, int score) {
            this.name = name;
            this.score = score;
        }
    }

    // Placeholder methods for Screen interface
    @Override public void show() {}
    @Override public void hide() {}
    @Override public void pause() {}
    @Override public void resume() {}
}