package io.github.avx20.M4TCH;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.Preferences;

public class SettingScreen implements Screen, M4TCH.VolumeChangeListener {
    private final M4TCH game;
    private SpriteBatch batch;
    private Viewport viewport;
    private Texture backgroundTexture;
    private Texture volumeBarTexture;
    private Texture volumeBarFillTexture;
    private Texture volumeKnobTexture;
    private Texture backButtonTexture;
    private BitmapFont titleFont;
    private BitmapFont normalFont;

    private Rectangle volumeBarBounds;
    private Rectangle volumeKnobBounds;
    private Rectangle backButtonBounds;

    private float volumeLevel = 0.5f;
    private boolean isDraggingKnob = false;
    private boolean isBackButtonClicked = false;

    private Music bgm;

    // Always use generated textures for safety
    private boolean useGeneratedTextures = true;

    // Volume bar and button dimensions
    private static final float VOLUME_BAR_WIDTH = 600f;
    private static final float VOLUME_BAR_HEIGHT = 40f;
    private static final float VOLUME_KNOB_SIZE = 60f;
    private static final float BACK_BUTTON_WIDTH = 200f;
    private static final float BACK_BUTTON_HEIGHT = 80f;

    public SettingScreen(M4TCH game) {
        this.game = game;
        this.viewport = new FitViewport(1920, 1080);
        this.batch = new SpriteBatch();

        // Initial volume from global setting
        this.volumeLevel = M4TCH.gameVolume;

        // First try to load background with multiple paths
        backgroundTexture = tryLoadTexture("game_bg.png");

        // Create all textures
        createGeneratedTextures();

        // Initialize fonts
        this.titleFont = new BitmapFont();
        this.titleFont.getData().setScale(3.0f);
        this.titleFont.setColor(Color.WHITE);

        this.normalFont = new BitmapFont();
        this.normalFont.getData().setScale(2.0f);
        this.normalFont.setColor(Color.WHITE);

        // Initialize volume bar position and size
        float centerX = viewport.getWorldWidth() / 2;
        float centerY = viewport.getWorldHeight() / 2;

        volumeBarBounds = new Rectangle(
            centerX - VOLUME_BAR_WIDTH / 2,
            centerY,
            VOLUME_BAR_WIDTH,
            VOLUME_BAR_HEIGHT
        );

        // Initialize volume knob position based on volumeLevel
        updateVolumeKnobPosition();

        // Back button position
        backButtonBounds = new Rectangle(
            centerX - BACK_BUTTON_WIDTH / 2,
            centerY - 200,
            BACK_BUTTON_WIDTH,
            BACK_BUTTON_HEIGHT
        );

        // Try to load and play background music
        try {
            bgm = Gdx.audio.newMusic(Gdx.files.internal("bgmmusic.mp3"));
            bgm.setLooping(true);

            // Apply volume with special handling for very low values
            applyVolume();

            bgm.play();
        } catch (Exception e) {
            Gdx.app.log("SettingsScreen", "Background music loading failed", e);
        }

        Gdx.app.log("SettingsScreen", "Settings screen initialized with volume: " + volumeLevel);
    }

    // Try loading texture from multiple possible locations
    private Texture tryLoadTexture(String filename) {
        try {
            return new Texture(Gdx.files.internal(filename));
        } catch (Exception e1) {
            try {
                return new Texture(Gdx.files.internal("assets/" + filename));
            } catch (Exception e2) {
                try {
                    return new Texture(Gdx.files.internal("core/assets/" + filename));
                } catch (Exception e3) {
                    Gdx.app.log("SettingsScreen", "Could not load texture: " + filename);
                    return null;
                }
            }
        }
    }

    // Apply volume with proper handling for very low values
    private void applyVolume() {
        // Force true zero when volume is very low
        if (volumeLevel < 0.01f) {
            volumeLevel = 0f;
        }

        if (bgm != null) {
            bgm.setVolume(volumeLevel);
        }

        // Update global volume and save setting
        M4TCH.gameVolume = volumeLevel;
        saveVolumeSettings();

        Gdx.app.log("SettingsScreen", "Volume set to: " + volumeLevel);
    }

    // Save volume setting to preferences
    private void saveVolumeSettings() {
        Preferences prefs = Gdx.app.getPreferences("M4TCHSettings");
        prefs.putFloat("volume", volumeLevel);
        prefs.flush();
    }

    private void createGeneratedTextures() {
        // Create background texture if needed
        if (backgroundTexture == null) {
            Pixmap bgPixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            bgPixmap.setColor(new Color(0.1f, 0.1f, 0.2f, 1f));
            bgPixmap.fill();
            backgroundTexture = new Texture(bgPixmap);
            bgPixmap.dispose();
        }

        // Create volume bar texture
        Pixmap barPixmap = new Pixmap((int)VOLUME_BAR_WIDTH, (int)VOLUME_BAR_HEIGHT, Pixmap.Format.RGBA8888);
        barPixmap.setColor(new Color(0.3f, 0.3f, 0.3f, 1f));
        barPixmap.fill();
        barPixmap.setColor(Color.DARK_GRAY);
        barPixmap.drawRectangle(0, 0, (int)VOLUME_BAR_WIDTH, (int)VOLUME_BAR_HEIGHT);
        volumeBarTexture = new Texture(barPixmap);
        barPixmap.dispose();

        // Create volume bar fill texture
        Pixmap barFillPixmap = new Pixmap((int)VOLUME_BAR_WIDTH, (int)VOLUME_BAR_HEIGHT, Pixmap.Format.RGBA8888);
        barFillPixmap.setColor(new Color(0.2f, 0.7f, 1f, 1f));
        barFillPixmap.fill();
        volumeBarFillTexture = new Texture(barFillPixmap);
        barFillPixmap.dispose();

        // Create volume knob texture
        Pixmap knobPixmap = new Pixmap((int)VOLUME_KNOB_SIZE, (int)VOLUME_KNOB_SIZE, Pixmap.Format.RGBA8888);
        knobPixmap.setColor(Color.WHITE);
        knobPixmap.fillCircle((int)VOLUME_KNOB_SIZE/2, (int)VOLUME_KNOB_SIZE/2, (int)VOLUME_KNOB_SIZE/2);
        volumeKnobTexture = new Texture(knobPixmap);
        knobPixmap.dispose();

        // Create back button texture
        Pixmap backPixmap = new Pixmap((int)BACK_BUTTON_WIDTH, (int)BACK_BUTTON_HEIGHT, Pixmap.Format.RGBA8888);
        backPixmap.setColor(new Color(0.8f, 0.2f, 0.2f, 1f));
        backPixmap.fillRectangle(0, 0, (int)BACK_BUTTON_WIDTH, (int)BACK_BUTTON_HEIGHT);
        backPixmap.setColor(Color.WHITE);
        backPixmap.drawRectangle(0, 0, (int)BACK_BUTTON_WIDTH, (int)BACK_BUTTON_HEIGHT);
        backButtonTexture = new Texture(backPixmap);
        backPixmap.dispose();
    }

    private void updateVolumeKnobPosition() {
        float knobX = volumeBarBounds.x + (volumeBarBounds.width * volumeLevel) - (VOLUME_KNOB_SIZE / 2);
        volumeKnobBounds = new Rectangle(
            knobX,
            volumeBarBounds.y - (VOLUME_KNOB_SIZE - VOLUME_BAR_HEIGHT) / 2,
            VOLUME_KNOB_SIZE,
            VOLUME_KNOB_SIZE
        );
    }

    @Override
    public void render(float delta) {
        // Clear screen
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Handle input
        handleInput();

        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);

        batch.begin();

        // Draw background
        if (backgroundTexture != null) {
            batch.draw(backgroundTexture, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
        }

        // Draw title
        titleFont.draw(batch, "Settings", viewport.getWorldWidth() / 2 - 100, viewport.getWorldHeight() - 100);

        // Draw volume label
        normalFont.draw(batch, "Volume", volumeBarBounds.x, volumeBarBounds.y + 80);

        // Draw volume bar background
        if (volumeBarTexture != null) {
            batch.draw(volumeBarTexture, volumeBarBounds.x, volumeBarBounds.y, volumeBarBounds.width, volumeBarBounds.height);
        }

        // Draw volume bar fill - volumeBarFillTexture should never be null now
        if (volumeBarFillTexture != null) {
            batch.draw(volumeBarFillTexture, volumeBarBounds.x, volumeBarBounds.y,
                volumeBarBounds.width * volumeLevel, volumeBarBounds.height);
        }

        // Draw volume value
        normalFont.draw(batch, String.format("%d%%", (int)(volumeLevel * 100)),
            volumeBarBounds.x + volumeBarBounds.width + 20, volumeBarBounds.y + 30);

        // Draw volume knob
        if (volumeKnobTexture != null) {
            batch.draw(volumeKnobTexture, volumeKnobBounds.x, volumeKnobBounds.y,
                volumeKnobBounds.width, volumeKnobBounds.height);
        }

        // Draw back button
        if (backButtonTexture != null) {
            float width = backButtonBounds.width;
            float height = backButtonBounds.height;
            float x = backButtonBounds.x;
            float y = backButtonBounds.y;

            if (isBackButtonClicked) {
                // Shrink button when clicked
                width *= 0.8f;
                height *= 0.8f;
                x += (backButtonBounds.width - width) / 2;
                y += (backButtonBounds.height - height) / 2;
            }

            batch.draw(backButtonTexture, x, y, width, height);
            normalFont.draw(batch, "Back", x + width/2 - 30, y + height/2 + 10);
        }

        batch.end();
    }

    private void handleInput() {
        // Return to previous screen if ESC key is pressed
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            safeReturn();
            return;
        }

        // Handle mouse/touch input
        if (Gdx.input.isTouched()) {
            float screenX = Gdx.input.getX();
            float screenY = Gdx.input.getY();

            // Convert screen coordinates to world coordinates
            Vector3 worldCoords = viewport.unproject(new Vector3(screenX, screenY, 0));
            float worldX = worldCoords.x;
            float worldY = worldCoords.y;

            // Debug touch coordinates
            if (Gdx.input.justTouched()) {
                Gdx.app.debug("SettingsScreen", "Touch at: " + worldX + "," + worldY);
            }

            // Handle volume knob dragging
            if (isDraggingKnob || volumeKnobBounds.contains(worldX, worldY)) {
                isDraggingKnob = true;

                // Calculate new volume value
                float newX = Math.max(volumeBarBounds.x, Math.min(volumeBarBounds.x + volumeBarBounds.width, worldX));
                volumeLevel = (newX - volumeBarBounds.x) / volumeBarBounds.width;

                // Update knob position
                updateVolumeKnobPosition();

                // Apply updated volume
                applyVolume();

            } else if (volumeBarBounds.contains(worldX, worldY)) {
                // Direct click on volume bar
                volumeLevel = (worldX - volumeBarBounds.x) / volumeBarBounds.width;
                updateVolumeKnobPosition();

                // Apply updated volume
                applyVolume();

            } else if (backButtonBounds.contains(worldX, worldY)) {
                isBackButtonClicked = true;
            }
        } else {
            // Mouse released
            if (isDraggingKnob) {
                isDraggingKnob = false;
            }

            if (isBackButtonClicked) {
                float screenX = Gdx.input.getX();
                float screenY = Gdx.input.getY();
                Vector3 worldCoords = viewport.unproject(new Vector3(screenX, screenY, 0));

                if (backButtonBounds.contains(worldCoords.x, worldCoords.y)) {
                    safeReturn();
                }
                isBackButtonClicked = false;
            }
        }
    }

    // A smarter way to return to the appropriate previous screen
    private void safeReturn() {
        // Make sure to save volume setting
        M4TCH.gameVolume = volumeLevel;
        saveVolumeSettings();

        // Stop the music safely
        if (bgm != null) {
            try {
                bgm.stop();
            } catch (Exception e) {
                Gdx.app.error("SettingsScreen", "Error stopping music", e);
            }
        }

        // Check game state to determine where to return
        if (game.isPaused()) {
            try {
                // Return to pause menu if coming from a paused game
                PauseMenu pauseMenu = new PauseMenu(game, game.getPlayScreen());
                game.setScreen(pauseMenu);
                Gdx.app.log("SettingsScreen", "Returning to pause menu");
            } catch (Exception e) {
                Gdx.app.error("SettingsScreen", "Error creating pause menu", e);
                // Fallback to home screen if pause menu creation fails
                game.setPaused(false);
                game.setScreen(new HomeScreen(game));
            }
        } else {
            // If not from paused game, return to home screen
            game.setPaused(false);
            game.setScreen(new HomeScreen(game));
            Gdx.app.log("SettingsScreen", "Returning to home screen");
        }

        // Resources will be disposed by LibGDX when this screen is replaced
    }

    @Override
    public void onVolumeChanged(float newVolume) {
        this.volumeLevel = newVolume;
        updateVolumeKnobPosition();

        if (bgm != null) {
            if (newVolume < 0.01f) newVolume = 0f;
            bgm.setVolume(newVolume);
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        try {
            if (batch != null) batch.dispose();
            if (backgroundTexture != null) backgroundTexture.dispose();
            if (volumeBarTexture != null) volumeBarTexture.dispose();
            if (volumeBarFillTexture != null) volumeBarFillTexture.dispose();
            if (volumeKnobTexture != null) volumeKnobTexture.dispose();
            if (backButtonTexture != null) backButtonTexture.dispose();
            if (titleFont != null) titleFont.dispose();
            if (normalFont != null) normalFont.dispose();
            if (bgm != null) bgm.dispose();
        } catch (Exception e) {
            Gdx.app.error("SettingsScreen", "Error in dispose", e);
        }
    }

    // Required empty methods for Screen interface
    @Override public void show() {}
    @Override public void hide() {}
    @Override public void pause() {}
    @Override public void resume() {}
}
