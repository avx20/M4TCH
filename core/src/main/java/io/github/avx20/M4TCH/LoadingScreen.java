package io.github.avx20.M4TCH;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
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
    private Texture instructionsImage1Texture; // Left image (Loading1.png)
    private Texture instructionsImage2Texture; // Right image (Loading2.png)
    private BitmapFont loadingFont;
    private BitmapFont titleFont; // Font for the title
    private BitmapFont skipFont; // Font for the skip message
    private Skin uiSkin;
    private GlyphLayout glyphLayout;

    private Viewport viewport;

    private float progress = 0f;
    private float loadingTime = 0f;
    private static final float LOADING_DURATION = 10f; // Extended loading time to 10 seconds (from 5s)
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

    // Skip message variables
    private String skipMessage = "按空格键跳过 (Press SPACE to skip)";
    private float skipMessageAlpha = 0f;
    private boolean fadeIn = true;
    private float fadeSpeed = 1.5f; // Controls how fast the skip message fades in/out

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

            // Load the tutorial images
            instructionsImage1Texture = loadTextureWithFallbacks("Loading1.png", assetsPath);
            if (instructionsImage1Texture != null) {
                Gdx.app.log("LoadingScreen", "Successfully loaded Loading1.png");
            } else {
                Gdx.app.error("LoadingScreen", "Failed to load Loading1.png");
            }

            instructionsImage2Texture = loadTextureWithFallbacks("Loading2.png", assetsPath);
            if (instructionsImage2Texture != null) {
                Gdx.app.log("LoadingScreen", "Successfully loaded Loading2.png");
            } else {
                Gdx.app.error("LoadingScreen", "Failed to load Loading2.png");
            }

            // Load loading bar textures
            loadingBarTexture = loadTextureWithFallbacks("loading_bar.png", assetsPath);
            loadingFrameTexture = loadTextureWithFallbacks("loading_frame.png", assetsPath);

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

                // Configure font for loading text - YELLOW
                parameter.size = 36; // Larger font size for loading text
                parameter.color.set(1f, 0.9f, 0f, 1f); // Yellow color
                loadingFont = generator.generateFont(parameter);

                // Configure font for title text - YELLOW
                parameter.size = 48; // Larger font size for title
                parameter.color.set(1f, 0.9f, 0f, 1f); // Yellow color
                titleFont = generator.generateFont(parameter);

                // Configure font for skip message - YELLOW (slightly smaller)
                parameter.size = 32;
                parameter.color.set(1f, 0.9f, 0f, 1f); // Yellow color
                skipFont = generator.generateFont(parameter);

                generator.dispose(); // Clean up the generator
            } catch (Exception e) {
                Gdx.app.error("LoadingScreen", "Error loading custom font: " + e.getMessage(), e);
                // Fallback to default font - WITH YELLOW COLOR
                loadingFont = new BitmapFont();
                loadingFont.getData().setScale(2f);
                loadingFont.setColor(1f, 0.9f, 0f, 1f); // Yellow color

                titleFont = new BitmapFont();
                titleFont.getData().setScale(3f);
                titleFont.setColor(1f, 0.9f, 0f, 1f); // Yellow color

                skipFont = new BitmapFont();
                skipFont.getData().setScale(1.8f);
                skipFont.setColor(1f, 0.9f, 0f, 1f); // Yellow color
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

            titleFont = new BitmapFont();
            titleFont.getData().setScale(3f);
            titleFont.setColor(1f, 0.9f, 0f, 1f); // Yellow color

            skipFont = new BitmapFont();
            skipFont.getData().setScale(1.8f);
            skipFont.setColor(1f, 0.9f, 0f, 1f); // Yellow color
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
        // Check for space bar input to skip
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            // Skip directly to play screen
            game.setScreen(nextScreen);
            dispose(); // Clean up resources
            return;
        }

        // Update skip message fade effect
        updateSkipMessageFade(delta);

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

        // Draw instructions images first (so they're behind other UI elements if needed)
        drawInstructionsImages();

        // Draw loading frame and progress bar
        drawLoadingBar();

        // Draw loading stage text
        drawLoadingStage();

        // Draw skip message with fade effect
        drawSkipMessage();

        batch.end();

        // Switch to next screen when loading is complete
        if (progress >= 1) {
            game.setScreen(nextScreen);
            dispose(); // Clean up resources
        }
    }

    private void updateSkipMessageFade(float delta) {
        // Update alpha value for fading effect
        if (fadeIn) {
            skipMessageAlpha += delta * fadeSpeed;
            if (skipMessageAlpha >= 1.0f) {
                skipMessageAlpha = 1.0f;
                fadeIn = false;
            }
        } else {
            skipMessageAlpha -= delta * fadeSpeed;
            if (skipMessageAlpha <= 0.3f) { // Minimum alpha of 0.3 so text always remains somewhat visible
                skipMessageAlpha = 0.3f;
                fadeIn = true;
            }
        }
    }

    private void drawSkipMessage() {
        if (skipFont == null) return;

        float centerX = viewport.getWorldWidth() / 2f;
        // Position skip message at the bottom of the screen, above the loading bar
        float skipY = viewport.getWorldHeight() * 0.08f;

        // Draw skip message with current alpha value
        skipFont.setColor(1f, 0.9f, 0f, skipMessageAlpha); // Set alpha for fading effect

        glyphLayout.setText(skipFont, skipMessage);
        skipFont.draw(batch,
            skipMessage,
            centerX - glyphLayout.width / 2f,
            skipY
        );

        // Reset font color
        skipFont.setColor(1f, 0.9f, 0f, 1f);
    }

    private void drawLoadingBar() {
        if (loadingBarTexture == null) return;

        float centerX = viewport.getWorldWidth() / 2f;
        float centerY = viewport.getWorldHeight() * 0.15f; // 移到屏幕底部15%的位置

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
        float centerY = viewport.getWorldHeight() * 0.15f + 70f; // 加载文本位于进度条上方

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

    private void drawInstructionsImages() {
        float centerX = viewport.getWorldWidth() / 2f;
        float topY = viewport.getWorldHeight() * 0.95f; // 上移标题位置到屏幕95%高度处

        // Draw title with larger font
        if (titleFont != null) {
            glyphLayout.setText(titleFont, "M4TCH Game Tutorial");
            titleFont.draw(batch,
                "M4TCH Game Tutorial",
                centerX - glyphLayout.width / 2f,
                topY
            );
        }

        // 每个图片的宽度为屏幕宽度的40%
        float maxImageWidth = viewport.getWorldWidth() * 0.4f;
        // 设置图片高度为屏幕高度的65%（比原来的75%小，避免覆盖标题）
        float maxImageHeight = viewport.getWorldHeight() * 0.65f;
        // 两图片之间的间距为屏幕宽度的5%
        float spacing = viewport.getWorldWidth() * 0.05f;

        // 设置两个图片的统一尺寸
        float imgWidth = maxImageWidth;
        float imgHeight = maxImageHeight;

        // 计算图片的垂直位置 - 下移图片位置，预留足够空间给标题
        // 确保标题和图片之间有足够间距
        float titleBottomY = topY - glyphLayout.height - viewport.getWorldHeight() * 0.05f; // 标题底部Y坐标，增加5%的间距
        float progressBarTopY = viewport.getWorldHeight() * 0.15f + 100f; // 进度条顶部Y坐标

        // 计算图片顶部Y坐标，确保在标题下方
        float imageTopY = titleBottomY;
        // 计算图片底部Y坐标
        float imageBottomY = imageTopY - imgHeight;

        // 确保图片底部不会遮挡进度条及其文本
        if (imageBottomY < progressBarTopY + 30f) {
            // 如果会遮挡，则调整图片高度
            imgHeight = imageTopY - progressBarTopY - 30f;
            // 保持宽高比
            imgWidth = (imgHeight / maxImageHeight) * maxImageWidth;
        }

        // 最终图片绘制的Y坐标（左下角）
        float imageY = imageTopY - imgHeight;

        // 左图 (Loading1.png)
        if (instructionsImage1Texture != null) {
            float leftImageX = centerX - spacing/2 - imgWidth;

            batch.draw(instructionsImage1Texture,
                leftImageX,
                imageY,
                imgWidth, imgHeight
            );
        }

        // 右图 (Loading2.png)
        if (instructionsImage2Texture != null) {
            float rightImageX = centerX + spacing/2;

            batch.draw(instructionsImage2Texture,
                rightImageX,
                imageY,
                imgWidth, imgHeight
            );
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
        if (instructionsImage1Texture != null) instructionsImage1Texture.dispose();
        if (instructionsImage2Texture != null) instructionsImage2Texture.dispose();
        if (loadingFont != null) loadingFont.dispose();
        if (titleFont != null) titleFont.dispose();
        if (skipFont != null) skipFont.dispose();
        if (uiSkin != null) uiSkin.dispose();
    }

    // Placeholder methods for Screen interface
    @Override public void show() {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
}
