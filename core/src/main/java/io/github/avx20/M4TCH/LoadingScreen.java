package io.github.avx20.M4TCH;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

public class LoadingScreen implements Screen {
    private final Core game;
    private SpriteBatch batch;
    private AssetManager assetManager;
    private BitmapFont font;
    private float elapsedTime = 0f; // Timer to track time

    public LoadingScreen(Core game) {
        this.game = game;
        this.assetManager = new AssetManager();
        this.batch = new SpriteBatch();

        // Load assets asynchronously
        assetManager.load("homescreen_bg.png", Texture.class);
        assetManager.load("play_button.png", Texture.class);
        assetManager.load("game_bg.png", Texture.class);  // Example for game screen

        // Load custom font (Optional)
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("font.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 36;
        font = generator.generateFont(parameter);
        generator.dispose();
    }

    @Override
    public void show() {}

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        elapsedTime += delta; // Update timer

        batch.begin();
        font.draw(batch, "Loading... " + (int) (assetManager.getProgress() * 100) + "%",
            Gdx.graphics.getWidth() / 2f - 50, Gdx.graphics.getHeight() / 2f);
        batch.end();

        // Check if loading is done & switch to GameScreen after 3 seconds
        if (assetManager.update() && elapsedTime >= 1) {
            game.setScreen(new PlayScreen(game)); // Switch to the game screen
        }
    }

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
        batch.dispose();
        font.dispose();
        assetManager.dispose();
    }
}
