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
    private boolean isPaused = true; // 默认暂停状态
    private BitmapFont font;
    private SpriteBatch batch;
    private Texture background, resume_button, restart_button, settings_icon, exit_button;
    private Rectangle resumeBounds, restartBounds, settingsBounds, mainMenuBounds;
    private M4TCH game; // 添加 M4TCH 实例
    private PlayScreen playScreen; // 添加 PlayScreen 实例

    private static final int BUTTON_WIDTH = 200;
    private static final int BUTTON_HEIGHT = 80;
    private static final int BUTTON_SPACING = 50; // 按钮间距

    // 修改构造函数，添加 PlayScreen 作为参数
    public PauseMenu(M4TCH game, PlayScreen playScreen) {
        this.game = game; // 赋值 game 实例
        this.playScreen = playScreen; // 赋值 PlayScreen 实例
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

    public void update() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.P)) {
            isPaused = false;
            game.resumeGame(); // 调用 M4TCH 的 resumeGame 方法
        }

        if (Gdx.input.isTouched()) {
            int x = Gdx.input.getX();
            int y = Gdx.graphics.getHeight() - Gdx.input.getY(); // 转换 y 坐标

            if (resumeBounds.contains(x, y)) {
                isPaused = false;
                game.resumeGame();
                // 确保 PlayScreen 的状态被正确恢复
                playScreen.resumeGameFromPause();
            } else if (restartBounds.contains(x, y)) {
                game.startGame(); // 重新开始游戏
            } else if (mainMenuBounds.contains(x, y)) {
                game.setScreen(new HomeScreen(game)); // 返回主菜单
            }
        }
    }

    @Override
    public void render(float delta) {
        update(); // 在 render 方法中调用 update 方法

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (!isPaused) return;

        batch.begin();
        batch.draw(background, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.draw(resume_button, resumeBounds.x, resumeBounds.y, BUTTON_WIDTH, BUTTON_HEIGHT);
        batch.draw(restart_button, restartBounds.x, restartBounds.y, BUTTON_WIDTH, BUTTON_HEIGHT);
        batch.draw(settings_icon, settingsBounds.x, settingsBounds.y, BUTTON_WIDTH, BUTTON_HEIGHT);
        batch.draw(exit_button, mainMenuBounds.x, mainMenuBounds.y, BUTTON_WIDTH, BUTTON_HEIGHT);
        batch.end();
    }

    @Override
    public void show() {
        // 设置输入处理器（如果需要）
    }

    @Override
    public void resize(int width, int height) {
        // 处理窗口大小调整
    }

    @Override
    public void pause() {
        // 处理游戏暂停
    }

    @Override
    public void resume() {
        // 处理游戏恢复
    }

    @Override
    public void hide() {
        // 处理屏幕隐藏
    }

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