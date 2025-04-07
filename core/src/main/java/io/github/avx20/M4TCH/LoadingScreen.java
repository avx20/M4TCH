package io.github.avx20.M4TCH;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.io.File;

public class LoadingScreen implements Screen {
    private M4TCH game;
    private SpriteBatch batch;

    // Loading screen assets
    private Texture backgroundTexture;
    private Texture loadingFrameTexture;
    private Texture loadingBarTexture;
    private BitmapFont loadingFont;
    private Skin uiSkin;
    private GlyphLayout glyphLayout;

    private Viewport viewport;

    private float progress = 0f;
    private float loadingTime = 0f;
    private static final float LOADING_DURATION = 2f; // Reduced loading time for better UX
    private Screen nextScreen;

    // Loading stages
    private String[] loadingStages = {
        "初始化游戏...",
        "加载资源...",
        "配置游戏环境...",
        "准备游戏世界...",
        "即将开始！"
    };
    private int currentStage = 0;

    public LoadingScreen(M4TCH game, Screen nextScreen) {
        this.game = game;
        this.nextScreen = nextScreen;

        // Create a viewport to handle different screen sizes
        this.viewport = new FitViewport(1920, 1080);

        // Initialize resources
        batch = new SpriteBatch();
        glyphLayout = new GlyphLayout(); // Initialize GlyphLayout

        try {
            // Determine the correct path to assets
            String assetsPath = determineAssetsPath();

            // Try loading assets with flexible paths
            backgroundTexture = loadTextureWithFallbacks("game_bg.png", assetsPath);
            if (backgroundTexture != null) {
                Gdx.app.log("LoadingScreen", "Successfully loaded background texture");
            } else {
                Gdx.app.error("LoadingScreen", "Failed to load background texture");
            }

            loadingFrameTexture = loadTextureWithFallbacks("loading_frame.png", assetsPath);
            loadingBarTexture = loadTextureWithFallbacks("loading_bar.png", assetsPath);

            // Load UI Skin with flexible paths
            try {
                uiSkin = new Skin(Gdx.files.absolute(assetsPath + File.separator + "uiskin.atlas"));
                // Use the default font from the UI skin
                loadingFont = uiSkin.getFont("default-font");
            } catch (Exception e) {
                Gdx.app.error("LoadingScreen", "Error loading skin: " + e.getMessage());
            }

            if (loadingFont == null) {
                // Fallback to default libGDX font if skin font is not available
                loadingFont = new BitmapFont();
            }

            // Adjust font size and color
            loadingFont.getData().setScale(2f); // Increased font size
            loadingFont.setColor(1f, 1f, 1f, 1f); // Bright white color
        } catch (Exception e) {
            Gdx.app.error("LoadingScreen", "Error loading loading screen assets: " + e.getMessage(), e);
            // Fallback to default font if everything fails
            loadingFont = new BitmapFont();
        }
    }

    private Texture loadTextureWithFallbacks(String filename, String basePath) {
        // Try various combinations of paths to find the texture
        String[] pathPrefixes = {
            "", // No prefix (direct path)
            basePath + File.separator,
            basePath + File.separator + "images" + File.separator,
            basePath + File.separator + "textures" + File.separator,
            basePath + File.separator + "backgrounds" + File.separator,
            "core/assets/",
            "../core/assets/",
            "core/assets/images/",
            "core/assets/textures/",
            "core/assets/backgrounds/",
            "assets/",
            "assets/images/",
            "assets/textures/",
            "assets/backgrounds/"
        };

        for (String prefix : pathPrefixes) {
            try {
                String path = prefix + filename;
                Gdx.app.log("LoadingScreen", "Trying to load texture from: " + path);

                // Try internal files first
                if (Gdx.files.internal(path).exists()) {
                    Gdx.app.log("LoadingScreen", "Found texture at: " + path);
                    return new Texture(Gdx.files.internal(path));
                }

                // Try absolute path as fallback
                if (Gdx.files.absolute(path).exists()) {
                    Gdx.app.log("LoadingScreen", "Found texture at absolute path: " + path);
                    return new Texture(Gdx.files.absolute(path));
                }
            } catch (Exception e) {
                // Try next path
                Gdx.app.log("LoadingScreen", "Failed to load from " + prefix + filename + ": " + e.getMessage());
            }
        }

        Gdx.app.error("LoadingScreen", "Could not load texture: " + filename);
        return null;
    }

    private String determineAssetsPath() {
        // Try multiple potential paths
        String[] possiblePaths = {
            "core/assets",
            "../core/assets",
            "M4TCH(2)/core/assets",
            "../M4TCH(2)/core/assets",
            "assets",
            "../assets",
            "." // Current directory as last resort
        };

        for (String path : possiblePaths) {
            File assetsDir = new File(path);
            if (assetsDir.exists() && assetsDir.isDirectory()) {
                Gdx.app.log("LoadingScreen", "Found assets directory at: " + assetsDir.getAbsolutePath());
                return assetsDir.getAbsolutePath();
            }
        }

        // If no path is found, log an error
        Gdx.app.error("LoadingScreen", "Could not find assets directory");
        return "."; // Fallback to current directory
    }

    @Override
    public void render(float delta) {
        // Clear the screen with a transparent background
        // This is still needed to clear previous frame but won't affect our background texture
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Update loading progress and time
        loadingTime += delta;
        progress = Math.min(loadingTime / LOADING_DURATION, 1f);

        // Update loading stage
        currentStage = (int)(progress * loadingStages.length);
        currentStage = Math.min(currentStage, loadingStages.length - 1);

        // Apply viewport
        viewport.apply();

        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();

        // Draw background
        if (backgroundTexture != null) {
            // Draw the background texture to fill the entire screen
            batch.draw(backgroundTexture, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
        }

        // Draw loading frame and progress bar
        drawLoadingBar();

        // Draw loading stage text
        drawLoadingStage();

        batch.end();

        // Switch to next screen when loading is complete
        if (progress >= 1) {
            game.setScreen(nextScreen);
            dispose(); // Clean up resources
        }
    }

    private void drawLoadingBar() {
        if (loadingBarTexture == null) return;

        float centerX = viewport.getWorldWidth() / 2f;
        float centerY = viewport.getWorldHeight() / 2f;

        // Larger loading bar size
        float barWidth = viewport.getWorldWidth() * 0.8f; // 80% of screen width
        float barHeight = 50f; // Fixed height

        // Interpolate progress for smoother animation
        float interpolatedProgress = Interpolation.pow2.apply(progress);

        // Draw semi-transparent background frame
        batch.setColor(1f, 1f, 1f, 0.3f);
        if (loadingFrameTexture != null) {
            batch.draw(loadingFrameTexture,
                centerX - barWidth / 2f,
                centerY - barHeight / 2f,
                barWidth,
                barHeight
            );
        } else {
            // Fallback if frame texture is missing
            batch.draw(loadingBarTexture,
                centerX - barWidth / 2f,
                centerY - barHeight / 2f,
                barWidth,
                barHeight
            );
        }

        // Restore full opacity
        batch.setColor(1f, 1f, 1f, 1f);

        // Draw progress bar with bright color
        batch.setColor(0.2f, 0.7f, 1f, 1f); // Bright blue
        batch.draw(loadingBarTexture,
            centerX - barWidth / 2f,
            centerY - barHeight / 2f,
            barWidth * interpolatedProgress,
            barHeight
        );

        // Reset color
        batch.setColor(1f, 1f, 1f, 1f);
    }

    private void drawLoadingStage() {
        if (loadingFont == null) return;

        float centerX = viewport.getWorldWidth() / 2f;
        float centerY = viewport.getWorldHeight() / 2f;

        // Draw current loading stage text
        String currentStageText = loadingStages[currentStage];

        // Use GlyphLayout to calculate text width
        glyphLayout.setText(loadingFont, currentStageText);

        // Center-align text, slightly raised position
        loadingFont.draw(batch,
            currentStageText,
            centerX - glyphLayout.width / 2f,
            centerY + 100f
        );

        // Draw percentage
        String percentageText = String.format("%d%%", (int)(progress * 100));
        glyphLayout.setText(loadingFont, percentageText);
        loadingFont.draw(batch,
            percentageText,
            centerX - glyphLayout.width / 2f,
            centerY + 50f
        );
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        batch.dispose();

        // Safely dispose of textures and font
        if (backgroundTexture != null) backgroundTexture.dispose();
        if (loadingFrameTexture != null) loadingFrameTexture.dispose();
        if (loadingBarTexture != null) loadingBarTexture.dispose();
        if (loadingFont != null) loadingFont.dispose();
        if (uiSkin != null) uiSkin.dispose();
    }

    // Placeholder methods for Screen interface
    @Override public void show() {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
}
