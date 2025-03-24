package com.m4tch.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class LeaderboardsScreen implements Screen {
    private Game game;
    private Stage stage;
    private Texture backgroundTexture;
    private Image backgroundImage;

    // Sample leaderboard data
    private String[] playerNames = {"Alice", "Bob", "Charlie", "David", "Emma"};
    private int[] playerScores = {1500, 1200, 1100, 900, 800};

    public LeaderboardsScreen(Game game) {
        this.game = game;
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());

        // Load background image
        backgroundTexture = new Texture(Gdx.files.internal("assets/game_bg.png"));
        backgroundImage = new Image(backgroundTexture);

        // Load UI skin
        Skin skin = new Skin(Gdx.files.internal("assets/uiskin.json"));

        // Create UI elements
        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(backgroundImage);
        stage.addActor(table);

        Label titleLabel = new Label("Leaderboard", skin);
        table.add(titleLabel).center().padBottom(20);
        table.row();

        // Add player scores
        for (int i = 0; i < playerNames.length; i++) {
            Label rankLabel = new Label((i + 1) + ". " + playerNames[i] + " - " + playerScores[i] + " pts", skin);
            table.add(rankLabel).left().padBottom(10);
            table.row();
        }
    }

    @Override
    public void render(float delta) {
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
        backgroundTexture.dispose();
    }
}
