package io.github.avx20.M4TCH;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
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
    private BitmapFont titleFont;
    private BitmapFont entryFont;
    private BitmapFont noScoresFont;
    private Array<LeaderboardEntry> leaderboardEntries;
    private GlyphLayout layout;

    // File to store leaderboard data
    private static final String LEADERBOARD_FILE = "core/assets/leaderboard.json";

    public LeaderboardScreen(M4TCH game) {
        this.game = game;

        // Initialize
        batch = new SpriteBatch();
        viewport = new FitViewport(1920, 1080);
        layout = new GlyphLayout();

        // Load textures
        try {
            backgroundTexture = new Texture(Gdx.files.internal("game_bg.png"));
            leaderboardFrameTexture = new Texture("leaderboard_frame.png");
        } catch (Exception e) {
            Gdx.app.error("LeaderboardScreen", "Error loading textures", e);
        }

        // Initialize fonts
        try {
            titleFont = new BitmapFont(Gdx.files.internal("core/assets/uiskin.atlas"), Gdx.files.internal("core/assets/font.ttf"), false);
            titleFont.setColor(Color.WHITE);
            titleFont.getData().setScale(4f);  // Even larger scale for title

            entryFont = new BitmapFont(Gdx.files.internal("core/assets/uiskin.atlas"), Gdx.files.internal("core/assets/font.ttf"), false);
            entryFont.setColor(Color.YELLOW);
            entryFont.getData().setScale(2.5f);  // Larger scale for entries

            noScoresFont = new BitmapFont(Gdx.files.internal("core/assets/uiskin.atlas"), Gdx.files.internal("core/assets/font.ttf"), false);
            noScoresFont.setColor(Color.GRAY);
            noScoresFont.getData().setScale(2.0f);  // Larger scale for "no scores" message
        } catch (Exception e) {
            Gdx.app.error("LeaderboardScreen", "Error loading fonts", e);
            // Fallback to default bitmap font if custom font fails
            titleFont = new BitmapFont();
            titleFont.getData().setScale(4f);
            entryFont = new BitmapFont();
            entryFont.getData().setScale(2.5f);
            noScoresFont = new BitmapFont();
            noScoresFont.getData().setScale(2.0f);
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

    // Method to add a new score with player name
    public void addScore(String playerName, int score) {
        leaderboardEntries.add(new LeaderboardEntry(playerName, score));
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
                // Start with an empty leaderboard instead of default entries
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

        // Draw leaderboard frame with a slightly larger size to make it more prominent
        if (leaderboardFrameTexture != null) {
            float frameWidth = leaderboardFrameTexture.getWidth() * 1.3f;
            float frameHeight = leaderboardFrameTexture.getHeight() * 1.3f;

            batch.draw(leaderboardFrameTexture,
                viewport.getWorldWidth() / 2 - frameWidth / 2,
                viewport.getWorldHeight() / 2 - frameHeight / 2,
                frameWidth, frameHeight
            );
        }

        // Draw title with better positioning and a shadow effect
        String title = "LEADERBOARD";
        layout.setText(titleFont, title);
        float titleWidth = layout.width;

        // Draw shadow for title text
        Color originalColor = titleFont.getColor();
        titleFont.setColor(0, 0, 0, 0.5f);
        titleFont.draw(batch, title,
            viewport.getWorldWidth() / 2 - titleWidth / 2 + 6,  // Larger offset for larger text
            viewport.getWorldHeight() - 100 - 6
        );

        // Draw title text
        titleFont.setColor(originalColor);
        titleFont.draw(batch, title,
            viewport.getWorldWidth() / 2 - titleWidth / 2,
            viewport.getWorldHeight() - 100
        );

        // Draw ESC instruction text at the bottom
        String escText = "Press ESC to return";
        noScoresFont.setColor(Color.LIGHT_GRAY);
        layout.setText(noScoresFont, escText);
        noScoresFont.draw(batch, escText,
            viewport.getWorldWidth() / 2 - layout.width / 2,
            80  // Position at bottom of screen
        );

        // Draw leaderboard entries or "No scores yet" message
        float startY = viewport.getWorldHeight() - 280;  // Moved down a bit to accommodate larger title

        if (leaderboardEntries.size == 0) {
            // Display "No scores yet" message if there are no entries
            String noScoresText = "No scores available yet. Play the game to set records!";
            layout.setText(noScoresFont, noScoresText);
            noScoresFont.draw(batch, noScoresText,
                viewport.getWorldWidth() / 2 - layout.width / 2,
                viewport.getWorldHeight() / 2
            );
        } else {
            // Draw available leaderboard entries with improved visual hierarchy
            for (int i = 0; i < leaderboardEntries.size; i++) {
                LeaderboardEntry entry = leaderboardEntries.get(i);
                String rankText = String.format("%d.", i + 1);
                String nameText = entry.name;
                String scoreText = String.valueOf(entry.score);

                // Set color based on rank
                if (i == 0) entryFont.setColor(1f, 0.84f, 0f, 1f); // Gold color
                else if (i == 1) entryFont.setColor(0.75f, 0.75f, 0.75f, 1f); // Silver color
                else if (i == 2) entryFont.setColor(0.65f, 0.16f, 0.16f, 1f); // Bronze color
                else entryFont.setColor(Color.WHITE);

                // Draw rank (aligned right)
                layout.setText(entryFont, rankText);
                entryFont.draw(batch, rankText,
                    viewport.getWorldWidth() / 2 - 270 - layout.width,  // Moved further left for better spacing
                    startY - (i * 90)  // Increased vertical spacing for larger text
                );

                // Draw name (aligned left)
                entryFont.draw(batch, nameText,
                    viewport.getWorldWidth() / 2 - 220,  // Adjusted for better spacing
                    startY - (i * 90)
                );

                // Draw score (aligned right)
                layout.setText(entryFont, scoreText);
                entryFont.draw(batch, scoreText,
                    viewport.getWorldWidth() / 2 + 220 - layout.width,  // Adjusted for better spacing
                    startY - (i * 90)
                );
            }
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
        titleFont.dispose();
        entryFont.dispose();
        noScoresFont.dispose();
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
