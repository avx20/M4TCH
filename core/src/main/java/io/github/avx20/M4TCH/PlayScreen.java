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

    // Grid and tiles
    private Tile[][] grid = new Tile[4][4];
    private final float TILE_SIZE = 200;
    private final float TILE_SPACING = 10;

    // Tile selection
    private Tile firstSelectedTile = null;
    private Tile secondSelectedTile = null;
    private float animationTimer = 0;

    public PlayScreen(M4TCH game) {
        this.game = game;
        this.viewport = new FitViewport(1920, 1080);
        this.gameBackground = new Texture("game_bg.png");
        this.font = new BitmapFont();

        initializeGrid();
    }

    private void initializeGrid() {
        for (int row = 0; row < 4; row++) {  // Changed to top to bottom
            for (int col = 0; col < 4; col++) {  // Left to right
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
        timeRemaining -= delta;
        animationTimer += delta;

        if (timeRemaining <= 0) {
            game.setScreen(new GameOverScreen(game, score));
            dispose();
            return;
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

                    batch.draw(tile.getTexture(),
                        tile.getPosition().x + offsetX, tile.getPosition().y + offsetY,
                        scaledWidth, scaledHeight);
                }
            }
        }

        font.draw(batch, "Time: " + (int) timeRemaining, 50, viewport.getWorldHeight() - 50);
        font.draw(batch, "Score: " + score, 50, viewport.getWorldHeight() - 100);
        batch.end();

        handleTileSelection();

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            dispose();
            game.setScreen(new HomeScreen(game));
        }
    }

    private void handleTileSelection() {
        if (Gdx.input.justTouched()) {
            float touchX = Gdx.input.getX();
            float touchY = viewport.getWorldHeight() - Gdx.input.getY();

            // Check from top to bottom for accurate click detection
            for (int row = 3; row >= 0; row--) {
                for (int col = 0; col < 4; col++) {
                    Tile tile = grid[row][col];
                    if (tile != null && tile.isFullyVisible()) {
                        Rectangle bounds = tile.getBounds();
                        if (bounds.contains(touchX, touchY)) {
                            tile.setScale(0.9f); // Original click behavior

                            if (firstSelectedTile == null) {
                                firstSelectedTile = tile;
                            } else if (secondSelectedTile == null && tile != firstSelectedTile) {
                                secondSelectedTile = tile;
                                checkForMatch();
                            }
                            return; // Only process one tile per click
                        }
                    }
                }
            }
        }
    }

    private void checkForMatch() {
        if (firstSelectedTile != null && secondSelectedTile != null) {
            if (firstSelectedTile.getNumber() == secondSelectedTile.getNumber() &&
                firstSelectedTile.getColor().equals(secondSelectedTile.getColor())) {
                combineTiles(firstSelectedTile, secondSelectedTile);
            } else {
                firstSelectedTile = null;
                secondSelectedTile = null;
            }
        }
    }

    private void combineTiles(Tile tile1, Tile tile2) {
        int newNumber = tile1.getNumber() + 1;
        String color = tile1.getColor();
        Texture newTexture;

        // Special case: when matching two "2" tiles
        if (tile1.getNumber() == 2 && tile2.getNumber() == 2) {
            newNumber = 3; // Star tile will be considered level 3
            newTexture = new Texture(color + "_tile_star.png"); // Use star texture
        } else {
            newTexture = new Texture(color + "_tile_" + newNumber + ".png"); // Normal progression
        }

        // Create new tile at second clicked position
        int secondRow = tile2.getGridY();
        int secondCol = tile2.getGridX();
        grid[secondRow][secondCol] = new Tile(newNumber, color, newTexture,
            tile2.getPosition(), secondCol, secondRow);
        grid[secondRow][secondCol].setAppearTime(animationTimer);

        // Create new random tile at first clicked position
        int firstRow = tile1.getGridY();
        int firstCol = tile1.getGridX();
        String newColor = getRandomColor();
        Texture firstTexture = new Texture(newColor + "_tile_one.png");
        grid[firstRow][firstCol] = new Tile(1, newColor, firstTexture,
            tile1.getPosition(), firstCol, firstRow);
        grid[firstRow][firstCol].setAppearTime(animationTimer);

        firstSelectedTile = null;
        secondSelectedTile = null;
        score += calculateScore(newNumber, color);
    }

    private int calculateScore(int number, String color) {
        if (number == 2) return 50;
        if (number == 3) return 500;
        return 0;
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
