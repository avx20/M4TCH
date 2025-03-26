package io.github.avx20.M4TCH;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Tile {
    private int number;
    private String color;
    private Texture texture;
    private Vector2 position;
    private Rectangle bounds;
    private float scale = 0.0f;
    private float appearTime;
    private boolean fullyVisible = false;
    private final float TILE_SIZE = 200; // Match PlayScreen's TILE_SIZE

    public Tile(int number, String color, Texture texture, Vector2 position, int gridX, int gridY) {
        this.number = number;
        this.color = color;
        this.texture = texture;
        this.position = position;
        this.bounds = new Rectangle(position.x, position.y, TILE_SIZE, TILE_SIZE);

        // Changed to make top row (gridY=0) appear first, bottom row (gridY=3) last
        this.appearTime = gridY * 0.4f; // Removed column offset to make whole rows appear together
    }

    public void update(float elapsedTime) {
        if (elapsedTime >= appearTime && scale < 1.0f) {
            scale = Math.min(1.0f, scale + 0.06f); // Increased from 0.02f to 0.06f (3x faster)
            if (scale >= 1.0f) {
                fullyVisible = true;
            }
        }
        updateBounds();
    }

    private void updateBounds() {
        float scaledWidth = TILE_SIZE * scale;
        float scaledHeight = TILE_SIZE * scale;
        float offsetX = (TILE_SIZE - scaledWidth) / 2;
        float offsetY = (TILE_SIZE - scaledHeight) / 2;

        bounds.set(
            position.x + offsetX,
            position.y + offsetY,
            scaledWidth,
            scaledHeight
        );
    }

    // [Rest of the getters/setters remain exactly the same]
    public boolean isFullyVisible() { return fullyVisible; }
    public float getScale() { return scale; }
    public Vector2 getPosition() { return position; }
    public Texture getTexture() { return texture; }
    public void setTexture(Texture texture) { this.texture = texture; }
    public int getNumber() { return number; }
    public void setNumber(int number) { this.number = number; }
    public String getColor() { return color; }
    public Rectangle getBounds() { return bounds; }
    public void setScale(float scale) { this.scale = scale; }
}
