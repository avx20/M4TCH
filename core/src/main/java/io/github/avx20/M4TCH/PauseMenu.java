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

    public void update() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.P)) {
            game.resumeGame();
            return;
        }

        if (Gdx.input.isTouched()) {
            int x = Gdx.input.getX();
            int y = Gdx.graphics.getHeight() - Gdx.input.getY();

            if (resumeBounds.contains(x, y)) {
                game.resumeGame();
            } else if (restartBounds.contains(x, y)) {
                game.startGame();
            } else if (mainMenuBounds.contains(x, y)) {
                playScreen.dispose(); 
                game.setPaused(false);
                game.setScreen(new HomeScreen(game));
            }
        }
    }

    @Override
    public void render(float delta) {
        update();

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