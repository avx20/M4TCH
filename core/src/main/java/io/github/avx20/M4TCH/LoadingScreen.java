package io.github.avx20.M4TCH;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Align;
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
    private Texture instructionsImageTexture; // Optional: Image to show game instructions
    private BitmapFont loadingFont;
    private BitmapFont instructionsFont; // Font for instructions text
    private Skin uiSkin;
    private GlyphLayout glyphLayout;

    private Viewport viewport;

    private float progress = 0f;
    private float loadingTime = 0f;
    private static final float LOADING_DURATION = 10f; // Extended loading time to 10 seconds (from 5s)
    private Screen nextScreen;

    // Instructions text
    private String[] instructionsText = {
        "How to Play:",
        "1. Match identical numbered tiles of the same color",
        "2. Combine 1+1 of same color → 2",
        "3. Combine 2+2 of same color → star",
        "4. Match special tiles to activate power-ups",
        "5. Score points before time runs out!",
        "",
        "Power-ups:",
        "• Blue stars: Freeze Time - slows the timer",
        "• Red stars: Combo Multiplier - doubles score",
        "• Green stars: Instant Tiles - makes all tiles visible"
    };

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

            

            // Load the custom font.ttf file
            try {
                // Try different paths for the font file
                String fontPath = "C:\\Users\\hua16\\Desktop\\M4TCH\\core\\assets\\font.ttf";
                if (!new File(fontPath).exists()) {
                    // Try alternative paths
                    String[] fontPaths = {
                        assetsPath + File.separator + "font.ttf",
                        "font.ttf",
                        "assets/font.ttf",
                        "core/assets/font.ttf"
                    };

                    for (String path : fontPaths) {
                        if (new File(path).exists() || Gdx.files.internal(path).exists()) {
                            fontPath = path;
                            break;
                        }
                    }
                }

                Gdx.app.log("LoadingScreen", "Attempting to load font from: " + fontPath);

                // Generate fonts using FreeType
                FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.absolute(fontPath));
                FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();

                // Configure font for loading text - CHANGED TO YELLOW
                parameter.size = 36; // Larger font size for loading text
                parameter.color.set(1f, 0.9f, 0f, 1f); // Yellow color
                loadingFont = generator.generateFont(parameter);

                // Configure font for instructions text - ALSO CHANGED TO YELLOW
                parameter.size = 28; // Smaller font size for instructions
                parameter.color.set(1f, 0.9f, 0f, 1f); // Yellow color
                instructionsFont = generator.generateFont(parameter);

                generator.dispose(); // Clean up the generator
            } catch (Exception e) {
                Gdx.app.error("LoadingScreen", "Error loading custom font: " + e.getMessage(), e);
                // Fallback to default font - WITH YELLOW COLOR
                loadingFont = new BitmapFont();
                loadingFont.getData().setScale(2f);
                loadingFont.setColor(1f, 0.9f, 0f, 1f); // Yellow color
                instructionsFont = new BitmapFont();
                instructionsFont.getData().setScale(1.5f);
                instructionsFont.setColor(1f, 0.9f, 0f, 1f); // Yellow color
            }

            // Load UI Skin with flexible paths (fallback)
            try {
                uiSkin = new Skin(Gdx.files.absolute(assetsPath + File.separator + "uiskin.atlas"));
            } catch (Exception e) {
                Gdx.app.error("LoadingScreen", "Error loading skin: " + e.getMessage());
            }

        } catch (Exception e) {
            Gdx.app.error("LoadingScreen", "Error loading loading screen assets: " + e.getMessage(), e);
            // Fallback to default font if everything fails - WITH YELLOW COLOR
            loadingFont = new BitmapFont();
            loadingFont.getData().setScale(2f);
            loadingFont.setColor(1f, 0.9f, 0f, 1f); // Yellow color
            instructionsFont = new BitmapFont();
            instructionsFont.getData().setScale(1.5f);
            instructionsFont.setColor(1f, 0.9f, 0f, 1f); // Yellow color
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
            "C:\\Users\\hua16\\Desktop\\M4TCH\\core\\assets", // Direct path specified in requirements
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

        // Draw instructions on how to play
        drawInstructions();

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
        float centerY = viewport.getWorldHeight() / 2f - 200f; // Lower position for the loading bar

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
        float centerY = viewport.getWorldHeight() / 2f - 150f; // Adjusted position below instructions

        // Draw current loading stage text
        String currentStageText = loadingStages[currentStage];

        // Use GlyphLayout to calculate text width
        glyphLayout.setText(loadingFont, currentStageText);

        // Center-align text
        loadingFont.draw(batch,
            currentStageText,
            centerX - glyphLayout.width / 2f,
            centerY
        );

        // Draw percentage
        String percentageText = String.format("%d%%", (int)(progress * 100));
        glyphLayout.setText(loadingFont, percentageText);
        loadingFont.draw(batch,
            percentageText,
            centerX - glyphLayout.width / 2f,
            centerY - 60f
        );
    }

    private void drawInstructions() {
        if (instructionsFont == null) return;

        float centerX = viewport.getWorldWidth() / 2f;
        float startY = viewport.getWorldHeight() * 0.75f; // Instructions in upper portion of screen

        // Draw title with larger font
        if (loadingFont != null) {
            glyphLayout.setText(loadingFont, "HOW TO PLAY");
            loadingFont.draw(batch,
                "HOW TO PLAY",
                centerX - glyphLayout.width / 2f,
                startY + 60f
            );
        }

        // Draw instruction image if available
        if (instructionsImageTexture != null) {
            float imgWidth = Math.min(600f, viewport.getWorldWidth() * 0.5f);
            float imgHeight = imgWidth * 0.75f; // Assuming 4:3 aspect ratio
            batch.draw(instructionsImageTexture,
                centerX - imgWidth / 2f,
                startY - 80f - imgHeight,
                imgWidth, imgHeight
            );
        } else {
            // Draw text instructions if no image
            float lineHeight = 40f;
            float textY = startY;

            for (String line : instructionsText) {
                glyphLayout.setText(instructionsFont, line);
                instructionsFont.draw(batch,
                    line,
                    centerX - glyphLayout.width / 2f,
                    textY
                );
                textY -= lineHeight;
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

        // Safely dispose of textures and font
        if (backgroundTexture != null) backgroundTexture.dispose();
        if (loadingFrameTexture != null) loadingFrameTexture.dispose();
        if (loadingBarTexture != null) loadingBarTexture.dispose();
        if (instructionsImageTexture != null) instructionsImageTexture.dispose();
        if (loadingFont != null) loadingFont.dispose();
        if (instructionsFont != null) instructionsFont.dispose();
        if (uiSkin != null) uiSkin.dispose();
    }

    // Placeholder methods for Screen interface
    @Override public void show() {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
}
