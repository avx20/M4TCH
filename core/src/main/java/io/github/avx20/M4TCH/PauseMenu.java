package io.github.avx20.M4TCH;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public class PauseMenu {
    private boolean isPaused = false;
    private BitmapFont font;
    private SpriteBatch batch;
    private Texture resume_button, restart_button, settings_icon, exit_button;
    private Rectangle resumeBounds, restartBounds, settingsBounds, mainMenuBounds;

    public PauseMenu() {
        font = new BitmapFont();
        batch = new SpriteBatch();
        
        // 加载按钮图标
        resume_button = new Texture("resume.png");
        restart_button = new Texture("restart.png");
        settings_icon = new Texture("settings.png");
        exit_button = new Texture("mainmenu.png");
        
        // 定义按钮区域（假设按钮大小为 100x50）
        resumeBounds = new Rectangle(350, 450, 100, 50);
        restartBounds = new Rectangle(350, 380, 100, 50);
        settingsBounds = new Rectangle(350, 310, 100, 50);
        mainMenuBounds = new Rectangle(350, 240, 100, 50);
    }

    public void update() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            isPaused = !isPaused;
        }

        if (isPaused && Gdx.input.justTouched()) {
            float x = Gdx.input.getX();
            float y = Gdx.graphics.getHeight() - Gdx.input.getY(); // 适配坐标系
            
            if (resumeBounds.contains(x, y)) {
                isPaused = false;
            } else if (restartBounds.contains(x, y)) {
                restartGame();
            } else if (settingsBounds.contains(x, y)) {
                openSettings();
            } else if (mainMenuBounds.contains(x, y)) {
                returnToMainMenu();
            }
        }
    }

    public void render() {
        if (!isPaused) return;
        
        batch.begin();
        font.draw(batch, "Paused", 400, 500);
        batch.draw(resume_button, resumeBounds.x, resumeBounds.y);
        batch.draw(restart_button, restartBounds.x, restartBounds.y);
        batch.draw(settings_icon, settingsBounds.x, settingsBounds.y);
        batch.draw(exit_button, mainMenuBounds.x, mainMenuBounds.y);
        batch.end();
    }

    public boolean isPaused() {
        return isPaused;
    }

    private void restartGame() {
        // 重新启动游戏的逻辑
    }

    private void openSettings() {
        // 打开设置菜单的逻辑
    }

    private void returnToMainMenu() {
        // 返回主菜单的逻辑
    }

    public void dispose() {
        font.dispose();
        batch.dispose();
        resume_button.dispose();
        restart_button.dispose();
        settings_icon.dispose();
        exit_button.dispose();
    }
}