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
    private float TILE_SIZE = 200; // Size of each tile
    private float TILE_SPACING = 10; // Space between tiles

    // Tile selection
    private Tile firstSelectedTile = null;
    private Tile secondSelectedTile = null;

    public PlayScreen(M4TCH game) {
        this.game = game;
        this.viewport = new FitViewport(1920, 1080);
        this.gameBackground = new Texture("game_bg.png");
        this.font = new BitmapFont();

        // Initialize the grid with tiles
        initializeGrid();
    }

    private void initializeGrid() {
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 4; col++) {
                String color = getRandomColor(); // Random color for each tile
                Texture texture = new Texture(color + "_tile_one.png");
                Vector2 position = new Vector2(
                    col * (TILE_SIZE + TILE_SPACING) + (viewport.getWorldWidth() - (4 * (TILE_SIZE + TILE_SPACING))) / 2,
                    row * (TILE_SIZE + TILE_SPACING) + (viewport.getWorldHeight() - (4 * (TILE_SIZE + TILE_SPACING))) / 2
                );
                grid[row][col] = new Tile(1, color, texture, position);
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
    if (timeRemaining <= 0) {
        game.setScreen(new GameOverScreen(game, score));
        dispose();
        return;
    }

        viewport.apply();
        SpriteBatch batch = game.getBatch();
        batch.setProjectionMatrix(viewport.getCamera().combined);

        batch.begin();

        // Draw the background
        batch.draw(gameBackground, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());

        // Draw the grid
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 4; col++) {
                Tile tile = grid[row][col];
                if (tile != null) {
                    // Apply scaling effect
                    float scale = tile.getScale();
                    float scaledWidth = TILE_SIZE * scale;
                    float scaledHeight = TILE_SIZE * scale;
                    float offsetX = (TILE_SIZE - scaledWidth) / 2; // Center the scaled tile
                    float offsetY = (TILE_SIZE - scaledHeight) / 2;

                    batch.draw(tile.getTexture(),
                        tile.getPosition().x + offsetX, tile.getPosition().y + offsetY,
                        scaledWidth, scaledHeight);
                }
            }
        }

        // Draw UI elements
        font.draw(batch, "Time: " + (int) timeRemaining, 50, viewport.getWorldHeight() - 50);
        font.draw(batch, "Score: " + score, 50, viewport.getWorldHeight() - 100);

        batch.end();

        // Handle tile selection
        handleTileSelection();

        // Handle ESC key to return to home screen
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            dispose();
            game.setScreen(new HomeScreen(game));
        }
    }

    private void handleTileSelection() {
        if (Gdx.input.justTouched()) {
            float touchX = Gdx.input.getX();
            float touchY = viewport.getWorldHeight() - Gdx.input.getY(); // Convert to world coordinates

            for (int row = 0; row < 4; row++) {
                for (int col = 0; col < 4; col++) {
                    Tile tile = grid[row][col];
                    if (tile != null && tile.getBounds().contains(touchX, touchY)) {
                        // Scale down the tile by 10% when clicked
                        tile.setScale(0.9f);

                        if (firstSelectedTile == null) {
                            firstSelectedTile = tile;
                        } else if (secondSelectedTile == null && tile != firstSelectedTile) {
                            secondSelectedTile = tile;
                            checkForMatch();
                        }
                    }
                }
            }
        } else {
            // Reset scale for all tiles when not clicked
            for (int row = 0; row < 4; row++) {
                for (int col = 0; col < 4; col++) {
                    Tile tile = grid[row][col];
                    if (tile != null && tile.getScale() != 1.0f) {
                        tile.setScale(1.0f); // Reset scale to default
                    }
                }
            }
        }
    }

    private void checkForMatch() {
        if (firstSelectedTile != null && secondSelectedTile != null) {
            if (firstSelectedTile.getNumber() == secondSelectedTile.getNumber() &&
                firstSelectedTile.getColor().equals(secondSelectedTile.getColor())) {
                // Tiles match - combine them
                combineTiles(firstSelectedTile, secondSelectedTile);
            } else {
                // Tiles do not match - reset selection
                firstSelectedTile = null;
                secondSelectedTile = null;
            }
        }
    }

    private void combineTiles(Tile tile1, Tile tile2) {
        // Combine tiles into a higher-numbered tile
        int newNumber = tile1.getNumber() + 1;
        String color = tile1.getColor();
        Texture newTexture = new Texture(color + "_tile_" + newNumber + ".png");

        tile1.setNumber(newNumber);
        tile1.setTexture(newTexture);

        // Reset the second tile (make it empty or invisible)
        tile2.setNumber(0);
        tile2.setTexture(new Texture("tile_clicked_icon.png"));

        // Reset selection
        firstSelectedTile = null;
        secondSelectedTile = null;

        // Update score
        score += calculateScore(newNumber, color);
    }

    private int calculateScore(int number, String color) {
        // Implement scoring logic based on the requirements
        if (number == 2) {
            return 50;
        } else if (number == 3) { // Star tile
            return 500;
        }
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
