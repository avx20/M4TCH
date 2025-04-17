package io.github.avx20.M4TCH;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public class PauseMenu implements Screen {

    private boolean isPaused = true;
    private BitmapFont font;
    private SpriteBatch batch;
    private Texture background, resume_button, restart_button, settings_icon, exit_button;
    private Rectangle resumeBounds, restartBounds, settingsBounds, mainMenuBounds;
    private M4TCH game;
    private PlayScreen playScreen;

    // 每个按钮的独立尺寸
    private static final int RESUME_BUTTON_WIDTH = 200;
    private static final int RESUME_BUTTON_HEIGHT = 60;

    private static final int RESTART_BUTTON_WIDTH = 220;
    private static final int RESTART_BUTTON_HEIGHT = 130;

    private static final int SETTINGS_BUTTON_WIDTH = 150;
    private static final int SETTINGS_BUTTON_HEIGHT = 80;

    private static final int EXIT_BUTTON_WIDTH = 200;
    private static final int EXIT_BUTTON_HEIGHT = 80;

    private static final int BUTTON_SPACING = 50;

    // 缩放动画
    private float resumeScale = 1f, restartScale = 1f, settingsScale = 1f, exitScale = 1f;
    private static final float SCALE_DOWN = 0.9f;
    private static final float SCALE_SPEED = 5f;

    // 动画延迟执行
    private enum ButtonType { NONE, RESUME, RESTART, SETTINGS, EXIT }
    private ButtonType clickedButton = ButtonType.NONE;
    private float clickTimer = 0f;
    private static final float CLICK_ANIMATION_DURATION = 0.5f;

    public PauseMenu(M4TCH game, PlayScreen playScreen) {
        this.game = game;
        this.playScreen = playScreen;
        font = new BitmapFont();
        batch = new SpriteBatch();

        background = new Texture("pausescreen_bg.png");
        resume_button = new Texture("resume_button.png");
        restart_button = new Texture("restart_button.png");
        settings_icon = new Texture("settings_icon.png");
        exit_button = new Texture("exit_button.png");

        int centerX = Gdx.graphics.getWidth() / 2;
        int centerY = Gdx.graphics.getHeight() / 2;
        float topY = Gdx.graphics.getHeight() / 2 + 150; // 初始顶部Y位置（你可以自己调）

        // Resume 按钮（顶部）
        resumeBounds = new Rectangle(
            centerX - RESUME_BUTTON_WIDTH / 2f,
            topY,
            RESUME_BUTTON_WIDTH,
            RESUME_BUTTON_HEIGHT
        );

        // Restart 按钮
        restartBounds = new Rectangle(
            centerX - RESTART_BUTTON_WIDTH / 2f,
            resumeBounds.y - RESUME_BUTTON_HEIGHT - BUTTON_SPACING,
            RESTART_BUTTON_WIDTH,
            RESTART_BUTTON_HEIGHT
        );

        // Settings 按钮
        settingsBounds = new Rectangle(
            centerX - SETTINGS_BUTTON_WIDTH / 2f,
            restartBounds.y - RESTART_BUTTON_HEIGHT - BUTTON_SPACING,
            SETTINGS_BUTTON_WIDTH,
            SETTINGS_BUTTON_HEIGHT
        );

        // Exit 按钮
        mainMenuBounds = new Rectangle(
            centerX - EXIT_BUTTON_WIDTH / 2f,
            settingsBounds.y - SETTINGS_BUTTON_HEIGHT - BUTTON_SPACING,
            EXIT_BUTTON_WIDTH,
            EXIT_BUTTON_HEIGHT
        );


        restartBounds = new Rectangle(
            centerX - RESTART_BUTTON_WIDTH / 2,
            centerY,
            RESTART_BUTTON_WIDTH,
            RESTART_BUTTON_HEIGHT
        );

        settingsBounds = new Rectangle(
            centerX - SETTINGS_BUTTON_WIDTH / 2,
            centerY - SETTINGS_BUTTON_HEIGHT - BUTTON_SPACING / 2,
            SETTINGS_BUTTON_WIDTH,
            SETTINGS_BUTTON_HEIGHT
        );

        mainMenuBounds = new Rectangle(
            centerX - EXIT_BUTTON_WIDTH / 2,
            centerY - (SETTINGS_BUTTON_HEIGHT + EXIT_BUTTON_HEIGHT + BUTTON_SPACING * 1.5f),
            EXIT_BUTTON_WIDTH,
            EXIT_BUTTON_HEIGHT
        );

    }

    public void update(float delta) {
        if (clickedButton != ButtonType.NONE) {
            clickTimer += delta;
            if (clickTimer >= CLICK_ANIMATION_DURATION) {
                switch (clickedButton) {
                    case RESUME:
                        game.resumeGame();
                        break;
                    case RESTART:
                        game.startGame();
                        break;
                    case EXIT:
                        playScreen.dispose();
                        game.setPaused(false);
                        game.setScreen(new HomeScreen(game));
                        break;
                    case SETTINGS:
                        // 未来可扩展设置功能
                        break;
                }
                clickedButton = ButtonType.NONE;
                clickTimer = 0f;
            }
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.resumeGame();
            return;
        }

        if (Gdx.input.justTouched()) {
            int x = Gdx.input.getX();
            int y = Gdx.graphics.getHeight() - Gdx.input.getY();

            if (resumeBounds.contains(x, y)) {
                clickedButton = ButtonType.RESUME;
                resumeScale = SCALE_DOWN;
            } else if (restartBounds.contains(x, y)) {
                clickedButton = ButtonType.RESTART;
                restartScale = SCALE_DOWN;
            } else if (settingsBounds.contains(x, y)) {
                clickedButton = ButtonType.SETTINGS;
                settingsScale = SCALE_DOWN;
            } else if (mainMenuBounds.contains(x, y)) {
                clickedButton = ButtonType.EXIT;
                exitScale = SCALE_DOWN;
            }
        }

        // 实时更新点击区域尺寸（可选，若尺寸不会变化也可省略）
        resumeBounds.setSize(RESUME_BUTTON_WIDTH, RESUME_BUTTON_HEIGHT);
        restartBounds.setSize(RESTART_BUTTON_WIDTH, RESTART_BUTTON_HEIGHT);
        settingsBounds.setSize(SETTINGS_BUTTON_WIDTH, SETTINGS_BUTTON_HEIGHT);
        mainMenuBounds.setSize(EXIT_BUTTON_WIDTH, EXIT_BUTTON_HEIGHT);
    }

    private void smoothScale(float delta) {
        resumeScale += (1f - resumeScale) * SCALE_SPEED * delta;
        restartScale += (1f - restartScale) * SCALE_SPEED * delta;
        settingsScale += (1f - settingsScale) * SCALE_SPEED * delta;
        exitScale += (1f - exitScale) * SCALE_SPEED * delta;
    }

    @Override
    public void render(float delta) {
        update(delta);
        smoothScale(delta);

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (!isPaused) return;

        batch.begin();
        batch.draw(background, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // Resume
        batch.draw(resume_button,
                resumeBounds.x + (RESUME_BUTTON_WIDTH * (1 - resumeScale) / 2),
                resumeBounds.y + (RESUME_BUTTON_HEIGHT * (1 - resumeScale) / 2),
                RESUME_BUTTON_WIDTH * resumeScale,
                RESUME_BUTTON_HEIGHT * resumeScale);

        // Restart
        batch.draw(restart_button,
                restartBounds.x + (RESTART_BUTTON_WIDTH * (1 - restartScale) / 2),
                restartBounds.y + (RESTART_BUTTON_HEIGHT * (1 - restartScale) / 2),
                RESTART_BUTTON_WIDTH * restartScale,
                RESTART_BUTTON_HEIGHT * restartScale);

        // Settings
        batch.draw(settings_icon,
                settingsBounds.x + (SETTINGS_BUTTON_WIDTH * (1 - settingsScale) / 2),
                settingsBounds.y + (SETTINGS_BUTTON_HEIGHT * (1 - settingsScale) / 2),
                SETTINGS_BUTTON_WIDTH * settingsScale,
                SETTINGS_BUTTON_HEIGHT * settingsScale);

        // Exit
        batch.draw(exit_button,
                mainMenuBounds.x + (EXIT_BUTTON_WIDTH * (1 - exitScale) / 2),
                mainMenuBounds.y + (EXIT_BUTTON_HEIGHT * (1 - exitScale) / 2),
                EXIT_BUTTON_WIDTH * exitScale,
                EXIT_BUTTON_HEIGHT * exitScale);

        batch.end();
    }

    @Override
    public void show() {}

    @Override
    public void resize(int width, int height) {}

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        font.dispose();
        batch.dispose();
        background.dispose();
        resume_button.dispose();
        restart_button.dispose();
        settings_icon.dispose();
        exit_button.dispose();
    }
}
