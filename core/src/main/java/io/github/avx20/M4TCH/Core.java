package io.github.avx20.M4TCH;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Core extends Game {
    private AssetManager assetManager;

    @Override
    public void create() {
        assetManager = new AssetManager();

        // ✅ Load assets (ensure these files exist)
        assetManager.load("homescreen_bg.png", Texture.class);
        assetManager.load("play_button.png", Texture.class);
        assetManager.load("game_bg.png", Texture.class);

        assetManager.finishLoading(); // ✅ Ensure assets are loaded before continuing

        setScreen(new HomeScreen(this)); // ✅ Start with HomeScreen
    }

    public AssetManager getAssetManager() {
        return assetManager;
    }

    @Override
    public void dispose() {
        assetManager.dispose();
    }
}
