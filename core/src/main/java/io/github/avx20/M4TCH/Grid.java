package io.github.avx20.M4TCH;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import java.util.Random;

public class Grid {
    private static final int GRID_SIZE = 4; // 4x4 grid
    private Tile[][] tiles;
    private Random random;

    // Grid positioning and scaling
    private float startX; // Starting X position of the grid
    private float startY; // Starting Y position of the grid
    private final float TILE_SIZE = 64 * 3f; // Scaled tile size (192px)
    private final float TILE_SPACING = 6 * 3f; // Scaled spacing (18px)
    private final float STEP = TILE_SIZE + TILE_SPACING; // Total step per tile (210px)

    // ShapeRenderer for drawing grid lines
    private ShapeRenderer shapeRenderer;

    public Grid() {
        tiles = new Tile[GRID_SIZE][GRID_SIZE];
        random = new Random();
        shapeRenderer = new ShapeRenderer();
        calculateGridPosition(); // Calculate grid's starting position
        spawnTiles(); // Initialize tiles
    }

    /** Calculate the starting position of the grid to center it on the screen. */
    private void calculateGridPosition() {
        // Total grid width and height (including spacing)
        float totalWidth = GRID_SIZE * STEP - TILE_SPACING;
        float totalHeight = totalWidth; // Square grid

        // Center the grid on the screen
        startX = (Gdx.graphics.getWidth() - totalWidth) / 2;
        startY = (Gdx.graphics.getHeight() - totalHeight) / 2;

        // Debug logs
        Gdx.app.log("Grid", "startX: " + startX + ", startY: " + startY);
    }

    /** Spawn tiles in the grid with random colors. */
    public void spawnTiles() {
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                int number = 1; // Start with base tile (1)
                String color = getRandomColor(); // Random color
                Texture texture = new Texture(color + "_tile_one.png"); // Load texture

                // Calculate tile position with centering offset
                // Adjusted positioning for perfect grid alignment
                Vector2 position = new Vector2(
                    startX + col * STEP + TILE_SPACING / 2, // X position with small offset
                    startY + (GRID_SIZE - 1 - row) * STEP + TILE_SPACING / 2  // Y position with small offset
                );

                // Log the position of the tile for debugging
                Gdx.app.log("Tile Position", "Tile at (" + col + ", " + row + ") position: " + position);

                // Create and store the tile
                tiles[row][col] = new Tile(number, color, texture, position);
                tiles[row][col].setScale(0); // Start as a dot
            }
        }
    }


    /** Get a random color for the tile. */
    private String getRandomColor() {
        String[] colors = {"red", "blue", "green"};
        return colors[random.nextInt(colors.length)];
    }

    /** Render the grid and its tiles. */
    public void render(SpriteBatch batch, float delta) {
        // Draw tiles with animation
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                if (tiles[row][col] != null) {
                    tiles[row][col].update(delta); // Update tile animation
                    tiles[row][col].render(batch); // Render each tile
                }
            }
        }

        // Draw grid lines with rounded corners
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE); // Grid line color

        // Set the line width for the grid lines (thicker lines)
        Gdx.gl.glLineWidth(4); // Make the grid lines thicker

        // Draw the outer border with rounded corners
        drawRoundedRectangle(startX, startY, GRID_SIZE * STEP - TILE_SPACING, GRID_SIZE * STEP - TILE_SPACING, 20);

        // Draw inner grid lines
        for (int i = 1; i < GRID_SIZE; i++) {
            // Vertical lines
            float x = startX + i * STEP;
            shapeRenderer.line(x, startY, x, startY + GRID_SIZE * STEP - TILE_SPACING);

            // Horizontal lines
            float y = startY + i * STEP;
            shapeRenderer.line(startX, y, startX + GRID_SIZE * STEP - TILE_SPACING, y);
        }

        shapeRenderer.end();
    }


    /** Draw a rectangle with rounded corners. */
    private void drawRoundedRectangle(float x, float y, float width, float height, float radius) {
        // Draw the four straight edges without any overlapping arcs
        shapeRenderer.line(x + radius, y, x + width - radius, y); // Top edge
        shapeRenderer.line(x + radius, y + height, x + width - radius, y + height); // Bottom edge
        shapeRenderer.line(x, y + radius, x, y + height - radius); // Left edge
        shapeRenderer.line(x + width, y + radius, x + width, y + height - radius); // Right edge

        // Draw the rounded corners at the four corners of the rectangle
        shapeRenderer.arc(x + width - radius, y + radius, radius, 270, 90); // Top-right corner
        shapeRenderer.arc(x + radius, y + radius, radius, 180, 90); // Top-left corner
        shapeRenderer.arc(x + radius, y + height - radius, radius, 90, 90); // Bottom-left corner
        shapeRenderer.arc(x + width - radius, y + height - radius, radius, 0, 90); // Bottom-right corner
    }



    /** Dispose resources. */
    public void dispose() {
        shapeRenderer.dispose();
    }
}
