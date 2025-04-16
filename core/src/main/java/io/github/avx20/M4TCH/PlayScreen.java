package io.github.avx20.M4TCH;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class PlayScreen implements Screen {
    private final M4TCH game;
    private Texture gameBackground;
    private BitmapFont font;
    private Viewport viewport;
    private float timeRemaining = 60;
    private int score = 0;

    private Tile[][] grid = new Tile[4][4];
    private final float TILE_SIZE = 200;
    private final float TILE_SPACING = 10;

    private Tile firstSelectedTile = null;
    private Tile secondSelectedTile = null;
    private float animationTimer = 0;

    private boolean inputBlocked = false;
    private float inputBlockTimer = 0;
    private Tile[] vibratingTiles = new Tile[2];
    
    private boolean isPaused = false;
    private float pausedTimeRemaining;
    private int pausedScore;
    private Tile[][] pausedGrid = new Tile[4][4];

    public PlayScreen(M4TCH game) {
        this.game = game;
        this.viewport = new FitViewport(1920, 1080);
        this.gameBackground = new Texture("game_bg.png");
        this.font = new BitmapFont();
        initializeGrid();
    }

    private void initializeGrid() {
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 4; col++) {
                String color = getRandomColor();
                Texture texture = new Texture(color + "_tile_one.png");
                Vector2 position = new Vector2(
                    col * (TILE_SIZE + TILE_SPACING) + (viewport.getWorldWidth() - (4 * (TILE_SIZE + TILE_SPACING))) / 2,
                    row * (TILE_SIZE + TILE_SPACING) + (viewport.getWorldHeight() - (4 * (TILE_SIZE + TILE_SPACING))) / 2
                );

                grid[row][col] = new Tile(1, color, texture, position, col, row);
            }
        }
    }

    private String getRandomColor() {
        String[] colors = {"red", "blue", "green"};
        return colors[(int) (Math.random() * colors.length)];
    }

    @Override
    public void render(float delta) {
        if (game.isPaused()) {
            renderPausedState();
            return;
        }

        timeRemaining -= delta;
        animationTimer += delta;

        if (timeRemaining <= 0) {
            game.setScreen(new GameOverScreen(game, score));
            dispose();
            return;
        }

        if (inputBlocked) {
            inputBlockTimer += delta;
            if (inputBlockTimer >= 0.5f) {
                inputBlocked = false;
                inputBlockTimer = 0;
                if (vibratingTiles[0] != null) vibratingTiles[0].setVibrating(false);
                if (vibratingTiles[1] != null) vibratingTiles[1].setVibrating(false);
                vibratingTiles[0] = null;
                vibratingTiles[1] = null;
            }
        }

        viewport.apply();
        SpriteBatch batch = game.getBatch();
        batch.setProjectionMatrix(viewport.getCamera().combined);

        batch.begin();
        batch.draw(gameBackground, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());

        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 4; col++) {
                Tile tile = grid[row][col];
                if (tile != null) {
                    tile.update(animationTimer);
                    float scale = tile.getScale();
                    float scaledWidth = TILE_SIZE * scale;
                    float scaledHeight = TILE_SIZE * scale;
                    float offsetX = (TILE_SIZE - scaledWidth) / 2;
                    float offsetY = (TILE_SIZE - scaledHeight) / 2;

                    float vibrationOffsetX = 0;
                    float vibrationOffsetY = 0;
                    if (tile.isVibrating()) {
                        float vibrationAmount = (float) Math.sin(animationTimer * 30) * 5;
                        vibrationOffsetX = vibrationAmount;
                        vibrationOffsetY = vibrationAmount;
                    }

                    batch.draw(tile.getTexture(),
                        tile.getPosition().x + offsetX + vibrationOffsetX,
                        tile.getPosition().y + offsetY + vibrationOffsetY,
                        scaledWidth, scaledHeight);
                }
            }
        }

        font.draw(batch, "Time: " + (int) timeRemaining, 50, viewport.getWorldHeight() - 50);
        font.draw(batch, "Score: " + score, 50, viewport.getWorldHeight() - 100);
        batch.end();

        if (!inputBlocked && !game.isPaused()) {
            handleTileSelection();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            dispose();
            game.setScreen(new HomeScreen(game));
        }
        
        if (Gdx.input.isKeyJustPressed(Input.Keys.P)) {
            pauseGame();
            game.pauseGame();
        }
    }

    private void handleTileSelection() {
        if (Gdx.input.justTouched()) {
            float touchX = Gdx.input.getX();
            float touchY = viewport.getWorldHeight() - Gdx.input.getY();

            for (int row = 3; row >= 0; row--) {
                for (int col = 0; col < 4; col++) {
                    Tile tile = grid[row][col];
                    if (tile != null && tile.isFullyVisible()) {
                        Rectangle bounds = tile.getBounds();
                        if (bounds.contains(touchX, touchY)) {
                            tile.setScale(0.9f);

                            if (firstSelectedTile == null) {
                                firstSelectedTile = tile;
                            } else if (secondSelectedTile == null && tile != firstSelectedTile) {
                                secondSelectedTile = tile;
                                checkForMatch();
                            }
                            return;
                        }
                    }
                }
            }
        }
    }
    private void renderPausedState() {
    viewport.apply();
    SpriteBatch batch = game.getBatch();
    batch.setProjectionMatrix(viewport.getCamera().combined);

    batch.begin();
    batch.draw(gameBackground, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());

    for (int row = 0; row < 4; row++) {
        for (int col = 0; col < 4; col++) {
            Tile tile = grid[row][col];
            if (tile != null) {
                float scale = tile.getScale();
                float scaledWidth = TILE_SIZE * scale;
                float scaledHeight = TILE_SIZE * scale;
                float offsetX = (TILE_SIZE - scaledWidth) / 2;
                float offsetY = (TILE_SIZE - scaledHeight) / 2;

                batch.draw(tile.getTexture(),
                    tile.getPosition().x + offsetX,
                    tile.getPosition().y + offsetY,
                    scaledWidth, scaledHeight);
            }
        }
    }

    font.draw(batch, "Time: " + (int) pausedTimeRemaining, 50, viewport.getWorldHeight() - 50);
    font.draw(batch, "Score: " + pausedScore, 50, viewport.getWorldHeight() - 100);
    batch.end();
}


    private void checkForMatch() {
        if (firstSelectedTile != null && secondSelectedTile != null) {
            if (firstSelectedTile.getNumber() == secondSelectedTile.getNumber() &&
                firstSelectedTile.getColor().equals(secondSelectedTile.getColor())) {

                if (firstSelectedTile.getNumber() == 3) {
                    handleStarTileMatch(firstSelectedTile, secondSelectedTile);
                } else {
                    combineTiles(firstSelectedTile, secondSelectedTile);
                }
            } else {
                firstSelectedTile.setVibrating(true);
                secondSelectedTile.setVibrating(true);
                vibratingTiles[0] = firstSelectedTile;
                vibratingTiles[1] = secondSelectedTile;
                inputBlocked = true;
                inputBlockTimer = 0;

                firstSelectedTile = null;
                secondSelectedTile = null;
            }
        }
    }

    private void handleStarTileMatch(Tile tile1, Tile tile2) {
        int row1 = tile1.getGridY();
        int col1 = tile1.getGridX();
        int row2 = tile2.getGridY();
        int col2 = tile2.getGridX();

        String color1 = getRandomColor();
        String color2 = getRandomColor();

        grid[row1][col1] = new Tile(1, color1, new Texture(color1 + "_tile_one.png"),
            tile1.getPosition(), col1, row1);
        grid[row1][col1].setAppearTime(animationTimer);

        grid[row2][col2] = new Tile(1, color2, new Texture(color2 + "_tile_one.png"),
            tile2.getPosition(), col2, row2);
        grid[row2][col2].setAppearTime(animationTimer);

        firstSelectedTile = null;
        secondSelectedTile = null;
        score += calculateScore(3, tile1.getColor());
    }

    private void combineTiles(Tile tile1, Tile tile2) {
        int newNumber = tile1.getNumber() + 1;
        String color = tile1.getColor();
        Texture newTexture;

        if (tile1.getNumber() == 2 && tile2.getNumber() == 2) {
            newNumber = 3;
            newTexture = new Texture(color + "_tile_star.png");
        } else {
            newTexture = new Texture(color + "_tile_" + newNumber + ".png");
        }

        int secondRow = tile2.getGridY();
        int secondCol = tile2.getGridX();
        grid[secondRow][secondCol] = new Tile(newNumber, color, newTexture,
            tile2.getPosition(), secondCol, secondRow);
        grid[secondRow][secondCol].setAppearTime(animationTimer);

        int firstRow = tile1.getGridY();
        int firstCol = tile1.getGridX();
        String newColor = getRandomColor();
        Texture firstTexture = new Texture(newColor + "_tile_one.png");
        grid[firstRow][firstCol] = new Tile(1, newColor, firstTexture,
            tile1.getPosition(), firstCol, firstRow);
        grid[firstRow][firstCol].setAppearTime(animationTimer);
        grid[firstRow][firstCol].setSpeedMultiplier(0.2f);

        firstSelectedTile = null;
        secondSelectedTile = null;
        score += calculateScore(newNumber, color);
    }

    private int calculateScore(int number, String color) {
        if (number == 2) return 50;
        if (number == 3) return 500;
        return 0;
    }

    public void pauseGame() {
        isPaused = true;
        pausedTimeRemaining = timeRemaining;
        pausedScore = score;
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 4; col++) {
                if (grid[row][col] != null) {
                    pausedGrid[row][col] = new Tile(grid[row][col]);
                } else {
                    pausedGrid[row][col] = null;
                }
            }
        }
    }

    public void resumeGameFromPause() {
        isPaused = false;
        timeRemaining = pausedTimeRemaining;
        score = pausedScore;
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 4; col++) {
                if (pausedGrid[row][col] != null) {
                    grid[row][col] = new Tile(pausedGrid[row][col]);
                } else {
                    grid[row][col] = null;
                }
            }
        }
        firstSelectedTile = null;
        secondSelectedTile = null;
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void show() {}

    @Override
    public void hide() {}

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void dispose() {
        gameBackground.dispose();
        font.dispose();
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 4; col++) {
                if (grid[row][col] != null) {
                    grid[row][col].getTexture().dispose();
                }
            }
        }
    }
}