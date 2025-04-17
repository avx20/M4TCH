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

    private static final int BUTTON_WIDTH = 200;
    private static final int BUTTON_HEIGHT = 80;
    private static final int BUTTON_SPACING = 50;

    // 按钮缩放动画
    private float resumeScale = 1f, restartScale = 1f, settingsScale = 1f, exitScale = 1f;
    private static final float SCALE_DOWN = 0.9f;
    private static final float SCALE_SPEED = 5f;

    // 延迟执行
    private enum ButtonType { NONE, RESUME, RESTART, SETTINGS, EXIT }
    private ButtonType clickedButton = ButtonType.NONE;
    private float clickTimer = 0f;
    private static final float CLICK_ANIMATION_DURATION = 0.25f;

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

        int centerX = Gdx.graphics.getWidth() / 2 - BUTTON_WIDTH;
        int centerY = Gdx.graphics.getHeight() / 2 + BUTTON_HEIGHT;

        resumeBounds = new Rectangle(centerX, centerY, BUTTON_WIDTH, BUTTON_HEIGHT);
        restartBounds = new Rectangle(centerX + BUTTON_WIDTH + BUTTON_SPACING, centerY, BUTTON_WIDTH, BUTTON_HEIGHT);
        settingsBounds = new Rectangle(centerX, centerY - BUTTON_HEIGHT - BUTTON_SPACING, BUTTON_WIDTH, BUTTON_HEIGHT);
        mainMenuBounds = new Rectangle(centerX + BUTTON_WIDTH + BUTTON_SPACING, centerY - BUTTON_HEIGHT - BUTTON_SPACING, BUTTON_WIDTH, BUTTON_HEIGHT);
    }

    public void update(float delta) {
        if (clickedButton != ButtonType.NONE) {
            clickTimer += delta;
            if (clickTimer >= CLICK_ANIMATION_DURATION) {
                // 动画完成，执行操作
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
                        // 可扩展：打开设置菜单
                        break;
                }
                clickedButton = ButtonType.NONE;
                clickTimer = 0f;
            }
            return; // 动画期间不处理其他点击
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

        batch.draw(resume_button,
                resumeBounds.x + (BUTTON_WIDTH * (1 - resumeScale) / 2),
                resumeBounds.y + (BUTTON_HEIGHT * (1 - resumeScale) / 2),
                BUTTON_WIDTH * resumeScale,
                BUTTON_HEIGHT * resumeScale);

        batch.draw(restart_button,
                restartBounds.x + (BUTTON_WIDTH * (1 - restartScale) / 2),
                restartBounds.y + (BUTTON_HEIGHT * (1 - restartScale) / 2),
                BUTTON_WIDTH * restartScale,
                BUTTON_HEIGHT * restartScale);

        batch.draw(settings_icon,
                settingsBounds.x + (BUTTON_WIDTH * (1 - settingsScale) / 2),
                settingsBounds.y + (BUTTON_HEIGHT * (1 - settingsScale) / 2),
                BUTTON_WIDTH * settingsScale,
                BUTTON_HEIGHT * settingsScale);

        batch.draw(exit_button,
                mainMenuBounds.x + (BUTTON_WIDTH * (1 - exitScale) / 2),
                mainMenuBounds.y + (BUTTON_HEIGHT * (1 - exitScale) / 2),
                BUTTON_WIDTH * exitScale,
                BUTTON_HEIGHT * exitScale);

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
