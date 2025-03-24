package com.m4tch.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class LoadingScreen implements Screen {
    private Game game;
    private Stage stage;
    private Texture logoTexture;
    private Image logoImage;
    private ProgressBar progressBar;
    private float progress = 0f;

    public LoadingScreen(Game game) {
        this.game = game;
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());

        // Load assets
        logoTexture = new Texture(Gdx.files.internal("assets/LoadingScreen_loga.png"));
        logoImage = new Image(logoTexture);

        // Load UI skin
        Skin skin = new Skin(Gdx.files.internal("assets/uiskin.json"));

        // Create progress bar
        progressBar = new ProgressBar(0, 1, 0.01f, false, skin);
        progressBar.setValue(0);

        // Layout UI elements
        Table table = new Table();
        table.setFillParent(true);
        table.add(logoImage).center().padBottom(20);
        table.row();
        table.add(progressBar).width(300).height(20);

        stage.addActor(table);
    }

    @Override
    public void render(float delta) {
        progress += delta * 0.3f;
        if (progress > 1) {
            progress = 1;
            game.setScreen(new LeaderboardsScreen(game));
        }
        progressBar.setValue(progress);

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void dispose() {
        stage.dispose();
        logoTexture.dispose();
    }
}
