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
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.files.FileHandle;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

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
    private Random random;

    // Maximum length for player names to display
    private static final int MAX_NAME_LENGTH = 10;

    // Arrays for generating random player names (shortened versions)
    private static final String[] NAME_PREFIXES = {
        "Pro", "Max", "Top", "Ace", "Star", "Boss", "MVP", "Cool", "Fast", "Epic"
    };

    private static final String[] NAME_SUFFIXES = {
        "X", "Pro", "Ace", "Kid", "Guy", "One", "King", "Hero", "Z", "Plus"
    };

    private static final String[] ANIMAL_NAMES = {
        "Fox", "Wolf", "Cat", "Bear", "Lion", "Hawk", "Fish", "Duck", "Snake", "Frog"
    };

    // File to store leaderboard data - simplified path
    private static final String LEADERBOARD_FILE = "leaderboard.json";

    public LeaderboardScreen(M4TCH game) {
        this.game = game;
        this.random = new Random();

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

        // Initialize fonts using the correct font path
        try {
            // Use FreeTypeFontGenerator for better font rendering if available
            FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("font.ttf"));

            // For title font (largest)
            FreeTypeFontGenerator.FreeTypeFontParameter titleParams = new FreeTypeFontGenerator.FreeTypeFontParameter();
            titleParams.size = 64; // Increased base size
            titleParams.color = Color.WHITE;
            titleFont = generator.generateFont(titleParams);
            titleFont.getData().setScale(1.5f); // Reduced scale from 4.0f to 1.5f

            // For entry font (medium)
            FreeTypeFontGenerator.FreeTypeFontParameter entryParams = new FreeTypeFontGenerator.FreeTypeFontParameter();
            entryParams.size = 36;
            entryParams.color = Color.YELLOW;
            entryFont = generator.generateFont(entryParams);
            entryFont.getData().setScale(1.2f); // Reduced scale from 2.5f to 1.2f

            // For "no scores" font (smaller)
            FreeTypeFontGenerator.FreeTypeFontParameter noScoresParams = new FreeTypeFontGenerator.FreeTypeFontParameter();
            noScoresParams.size = 30;
            noScoresParams.color = Color.GRAY;
            noScoresFont = generator.generateFont(noScoresParams);
            noScoresFont.getData().setScale(1.0f); // Reduced scale from 2.0f to 1.0f

            // Dispose generator after use
            generator.dispose();

            Gdx.app.log("LeaderboardScreen", "Custom font loaded successfully");
        } catch (Exception e) {
            Gdx.app.error("LeaderboardScreen", "Error loading custom font: " + e.getMessage(), e);
            // Fallback to default bitmap font if custom font fails
            titleFont = new BitmapFont();
            titleFont.getData().setScale(1.5f);
            titleFont.setColor(Color.WHITE);

            entryFont = new BitmapFont();
            entryFont.getData().setScale(1.2f);
            entryFont.setColor(Color.YELLOW);

            noScoresFont = new BitmapFont();
            noScoresFont.getData().setScale(1.0f);
            noScoresFont.setColor(Color.GRAY);

            Gdx.app.log("LeaderboardScreen", "Using default font as fallback");
        }

        // Load leaderboard entries
        leaderboardEntries = loadLeaderboardEntries();
    }

    // Method to generate a shorter random player name
    private String generateRandomPlayerName() {
        int nameType = random.nextInt(3); // 0, 1, or 2 for different name patterns
        String name;

        switch (nameType) {
            case 0: // Prefix + Suffix (e.g., "ProX")
                name = NAME_PREFIXES[random.nextInt(NAME_PREFIXES.length)] +
                    NAME_SUFFIXES[random.nextInt(NAME_SUFFIXES.length)];
                break;

            case 1: // Animal only (e.g., "Wolf")
                name = ANIMAL_NAMES[random.nextInt(ANIMAL_NAMES.length)];
                break;

            case 2: // Short number-based (e.g., "Player42")
                name = "P" + (random.nextInt(99) + 1);
                break;

            default:
                name = "P" + random.nextInt(100); // Fallback with number
                break;
        }

        // Ensure name doesn't exceed max length
        if (name.length() > MAX_NAME_LENGTH) {
            name = name.substring(0, MAX_NAME_LENGTH);
        }

        return name;
    }

    // Method to add a new score to leaderboard with a randomly generated player name
    public void addScore(int score) {
        // Generate random player name
        String playerName = generateRandomPlayerName();

        // Get current timestamp
        String timestamp = getCurrentTimestamp();

        // Add entry with random name
        leaderboardEntries.add(new LeaderboardEntry(playerName, score, timestamp));
        sortAndTrimLeaderboard();
        saveLeaderboardEntries();
    }

    // Method to add a new score with specified player name (kept for compatibility)
    public void addScore(String playerName, int score) {
        // Ensure name doesn't exceed max length
        if (playerName.length() > MAX_NAME_LENGTH) {
            playerName = playerName.substring(0, MAX_NAME_LENGTH);
        }

        // Get current timestamp
        String timestamp = getCurrentTimestamp();
        leaderboardEntries.add(new LeaderboardEntry(playerName, score, timestamp));
        sortAndTrimLeaderboard();
        saveLeaderboardEntries();
    }

    // Helper method to get the current timestamp in a readable format
    private String getCurrentTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd HH:mm");
        return sdf.format(new Date());
    }

    private void sortAndTrimLeaderboard() {
        // Sort in descending order by score
        leaderboardEntries.sort((a, b) -> Integer.compare(b.score, a.score));

        // Keep only top 10 entries
        if (leaderboardEntries.size > 10) {
            leaderboardEntries.truncate(10);
        }
    }

    private Array<LeaderboardEntry> loadLeaderboardEntries() {
        Array<LeaderboardEntry> entries = new Array<>();
        try {
            FileHandle fileHandle = Gdx.files.local(LEADERBOARD_FILE);
            if (fileHandle.exists()) {
                JsonReader jsonReader = new JsonReader();
                JsonValue base = jsonReader.parse(fileHandle);

                for (JsonValue entry = base.child; entry != null; entry = entry.next) {
                    String name = entry.getString("name");
                    // Ensure loaded names also respect max length
                    if (name.length() > MAX_NAME_LENGTH) {
                        name = name.substring(0, MAX_NAME_LENGTH);
                    }

                    entries.add(new LeaderboardEntry(
                        name,
                        entry.getInt("score"),
                        entry.getString("timestamp", "N/A")  // Default to "N/A" if timestamp is missing
                    ));
                }
                Gdx.app.log("LeaderboardScreen", "Loaded " + entries.size + " leaderboard entries");
            } else {
                Gdx.app.log("LeaderboardScreen", "Leaderboard file doesn't exist, creating new one");
                saveLeaderboardEntries();
            }
        } catch (Exception e) {
            Gdx.app.error("LeaderboardScreen", "Error loading leaderboard", e);
        }
        return entries;
    }

    private void saveLeaderboardEntries() {
        try {
            FileHandle fileHandle = Gdx.files.local(LEADERBOARD_FILE);
            Json json = new Json();
            json.setOutputType(JsonWriter.OutputType.json);
            fileHandle.writeString(json.toJson(leaderboardEntries), false);
            Gdx.app.log("LeaderboardScreen", "Leaderboard saved successfully");
        } catch (Exception e) {
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
            float frameWidth = viewport.getWorldWidth() * 0.75f;  // Use percentage of screen width
            float frameHeight = viewport.getWorldHeight() * 0.75f; // Use percentage of screen height

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

        // Title positioned with more space from top
        float titleY = viewport.getWorldHeight() - 150;

        // Draw shadow for title text
        Color originalColor = titleFont.getColor();
        titleFont.setColor(0, 0, 0, 0.5f);
        titleFont.draw(batch, title,
            viewport.getWorldWidth() / 2 - titleWidth / 2 + 4,  // Smaller offset for shadow
            titleY - 4
        );

        // Draw title text
        titleFont.setColor(originalColor);
        titleFont.draw(batch, title,
            viewport.getWorldWidth() / 2 - titleWidth / 2,
            titleY
        );

        // Draw column headers with improved spacing
        float headersY = titleY - 100; // More spacing between title and headers

        // Move name column more to the left
        float nameX = viewport.getWorldWidth() / 2 - 450; // Moved further left from -350
        float timeX = viewport.getWorldWidth() / 2 + 100;  // Moved right from center
        float scoreX = viewport.getWorldWidth() / 2 + 350; // Keep same position

        noScoresFont.setColor(Color.WHITE);
        noScoresFont.draw(batch, "PLAYER", nameX, headersY);

        // Center align the TIME header
        layout.setText(noScoresFont, "TIME");
        noScoresFont.draw(batch, "TIME", timeX - layout.width / 2, headersY);

        // Center align the SCORE header
        layout.setText(noScoresFont, "SCORE");
        noScoresFont.draw(batch, "SCORE", scoreX - layout.width / 2, headersY);

        // Draw ESC instruction text at the bottom with proper spacing
        String escText = "Press ESC to return";
        noScoresFont.setColor(Color.LIGHT_GRAY);
        layout.setText(noScoresFont, escText);
        noScoresFont.draw(batch, escText,
            viewport.getWorldWidth() / 2 - layout.width / 2,
            100  // Position at bottom of screen with proper spacing
        );

        // Draw leaderboard entries or "No scores yet" message
        float startY = headersY - 60;  // Better spacing from headers

        if (leaderboardEntries.size == 0) {
            // Display "No scores yet" message if there are no entries
            String noScoresText = "No scores available yet. Play the game to set records!";
            layout.setText(noScoresFont, noScoresText);
            noScoresFont.draw(batch, noScoresText,
                viewport.getWorldWidth() / 2 - layout.width / 2,
                viewport.getWorldHeight() / 2
            );
        } else {
            // Draw available leaderboard entries with improved visual hierarchy and spacing
            for (int i = 0; i < leaderboardEntries.size; i++) {
                LeaderboardEntry entry = leaderboardEntries.get(i);
                String rankText = String.format("%d.", i + 1);
                String nameText = entry.name;
                String timeText = entry.timestamp;
                String scoreText = String.valueOf(entry.score);

                // Set color based on rank
                if (i == 0) entryFont.setColor(1f, 0.84f, 0f, 1f); // Gold color
                else if (i == 1) entryFont.setColor(0.75f, 0.75f, 0.75f, 1f); // Silver color
                else if (i == 2) entryFont.setColor(0.65f, 0.16f, 0.16f, 1f); // Bronze color
                else entryFont.setColor(Color.WHITE);

                // Increased vertical spacing between entries
                float rowY = startY - (i * 55);  // Adjusted spacing for better readability

                // Draw rank (aligned right)
                layout.setText(entryFont, rankText);
                entryFont.draw(batch, rankText,
                    nameX - layout.width - 20,  // Better spacing
                    rowY
                );

                // Draw name (aligned left)
                entryFont.draw(batch, nameText, nameX, rowY);

                // Draw timestamp (center aligned)
                layout.setText(entryFont, timeText);
                entryFont.draw(batch, timeText,
                    timeX - layout.width / 2,
                    rowY
                );

                // Draw score (center aligned with header)
                layout.setText(entryFont, scoreText);
                entryFont.draw(batch, scoreText,
                    scoreX - layout.width / 2,
                    rowY
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

    // Helper class for leaderboard entries (enhanced with timestamp)
    public static class LeaderboardEntry {
        public String name;
        public int score;
        public String timestamp;

        public LeaderboardEntry() {
            // No-arg constructor needed for JSON serialization
        }

        public LeaderboardEntry(String name, int score, String timestamp) {
            this.name = name;
            this.score = score;
            this.timestamp = timestamp;
        }
    }

    // Placeholder methods for Screen interface
    @Override public void show() {}
    @Override public void hide() {}
    @Override public void pause() {}
    @Override public void resume() {}
}
