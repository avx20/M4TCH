package io.github.avx20.M4TCH;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
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

    private static final int BUTTON_SPACING = 40;
    private static final float SCALE_DOWN = 0.9f;
    private static final float SCALE_SPEED = 5f;
    private static final float CLICK_ANIMATION_DURATION = 0.5f;

    private float resumeScale = 1f, restartScale = 1f, settingsScale = 1f, exitScale = 1f;
    private enum ButtonType { NONE, RESUME, RESTART, SETTINGS, EXIT }
    private ButtonType clickedButton = ButtonType.NONE;
    private float clickTimer = 0f;

    private Music bgm;

    // Button size constants (can be flexibly modified)
    private static final float RESUME_BUTTON_WIDTH = 200f;
    private static final float RESUME_BUTTON_HEIGHT = 60f;

    private static final float RESTART_BUTTON_WIDTH = 220f;
    private static final float RESTART_BUTTON_HEIGHT = 130f;

    private static final float SETTINGS_BUTTON_WIDTH = 150f;
    private static final float SETTINGS_BUTTON_HEIGHT = 80f;

    private static final float EXIT_BUTTON_WIDTH = 200f;
    private static final float EXIT_BUTTON_HEIGHT = 80f;

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

        float topY = centerY + (RESUME_BUTTON_HEIGHT + RESTART_BUTTON_HEIGHT + SETTINGS_BUTTON_HEIGHT + EXIT_BUTTON_HEIGHT) / 2f
            + BUTTON_SPACING * 1.5f;

        resumeBounds = new Rectangle(centerX - RESUME_BUTTON_WIDTH / 2f, topY - RESUME_BUTTON_HEIGHT, RESUME_BUTTON_WIDTH, RESUME_BUTTON_HEIGHT);
        restartBounds = new Rectangle(centerX - RESTART_BUTTON_WIDTH / 2f,
            resumeBounds.y - RESTART_BUTTON_HEIGHT - BUTTON_SPACING,
            RESTART_BUTTON_WIDTH, RESTART_BUTTON_HEIGHT);
        settingsBounds = new Rectangle(centerX - SETTINGS_BUTTON_WIDTH / 2f,
            restartBounds.y - SETTINGS_BUTTON_HEIGHT - BUTTON_SPACING,
            SETTINGS_BUTTON_WIDTH, SETTINGS_BUTTON_HEIGHT);
        mainMenuBounds = new Rectangle(centerX - EXIT_BUTTON_WIDTH / 2f,
            settingsBounds.y - EXIT_BUTTON_HEIGHT - BUTTON_SPACING,
            EXIT_BUTTON_WIDTH, EXIT_BUTTON_HEIGHT);

        // Set click area sizes, ensure synchronization
        resumeBounds.setSize(RESUME_BUTTON_WIDTH, RESUME_BUTTON_HEIGHT);
        restartBounds.setSize(RESTART_BUTTON_WIDTH, RESTART_BUTTON_HEIGHT);
        settingsBounds.setSize(SETTINGS_BUTTON_WIDTH, SETTINGS_BUTTON_HEIGHT);
        mainMenuBounds.setSize(EXIT_BUTTON_WIDTH, EXIT_BUTTON_HEIGHT);

        bgm = Gdx.audio.newMusic(Gdx.files.internal("bgmmusic.mp3"));
        bgm.setLooping(true);

        // Apply volume with special handling for very low values
        float volume = M4TCH.gameVolume;
        if (volume < 0.01f) {
            volume = 0f;
        }
        bgm.setVolume(volume);

        bgm.play();
    }

    private void update(float delta) {
        if (clickedButton != ButtonType.NONE) {
            clickTimer += delta;
            if (clickTimer >= CLICK_ANIMATION_DURATION) {
                switch (clickedButton) {
                    case RESUME: game.resumeGame(); break;
                    case RESTART: game.startGame(); break;
                    case SETTINGS: game.setScreen(new SettingsScreen(game)); break;
                    case EXIT:
                        playScreen.dispose();
                        game.setPaused(false);
                        game.setScreen(new HomeScreen(game));
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
            resumeBounds.x + (resumeBounds.width * (1 - resumeScale) / 2),
            resumeBounds.y + (resumeBounds.height * (1 - resumeScale) / 2),
            resumeBounds.width * resumeScale,
            resumeBounds.height * resumeScale);

        batch.draw(restart_button,
            restartBounds.x + (restartBounds.width * (1 - restartScale) / 2),
            restartBounds.y + (restartBounds.height * (1 - restartScale) / 2),
            restartBounds.width * restartScale,
            restartBounds.height * restartScale);

        batch.draw(settings_icon,
            settingsBounds.x + (settingsBounds.width * (1 - settingsScale) / 2),
            settingsBounds.y + (settingsBounds.height * (1 - settingsScale) / 2),
            settingsBounds.width * settingsScale,
            settingsBounds.height * settingsScale);

        batch.draw(exit_button,
            mainMenuBounds.x + (mainMenuBounds.width * (1 - exitScale) / 2),
            mainMenuBounds.y + (mainMenuBounds.height * (1 - exitScale) / 2),
            mainMenuBounds.width * exitScale,
            mainMenuBounds.height * exitScale);

        batch.end();
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
        bgm.dispose();
    }

    @Override public void show() {}
    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
}
