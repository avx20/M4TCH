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
    private boolean vibrating = false;
    private float speedMultiplier = 1.0f;
    private final float TILE_SIZE = 200;
    private final int gridX;
    private final int gridY;

    public Tile(int number, String color, Texture texture, Vector2 position, int gridX, int gridY) {
        this.number = number;
        this.color = color;
        this.texture = texture;
        this.position = new Vector2(position);
        this.bounds = new Rectangle(position.x, position.y, TILE_SIZE, TILE_SIZE);
        this.gridX = gridX;
        this.gridY = gridY;
        this.appearTime = gridY * 0.4f + gridX * 0.1f;
    }

    // Copy Constructor
    public Tile(Tile other) {
        this.number = other.number;
        this.color = other.color;
        this.texture = new Texture(other.texture.getTextureData()); // Create a new Texture instance
        this.position = new Vector2(other.position);
        this.bounds = new Rectangle(other.bounds);
        this.scale = other.scale;
        this.appearTime = other.appearTime;
        this.fullyVisible = other.fullyVisible;
        this.vibrating = other.vibrating;
        this.speedMultiplier = other.speedMultiplier;
        this.gridX = other.gridX;
        this.gridY = other.gridY;
    }

    public void update(float elapsedTime) {
        if (elapsedTime >= appearTime && scale < 1.0f) {
            scale = Math.min(1.0f, scale + 0.06f * speedMultiplier);
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

    // Getters
    public boolean isFullyVisible() {
        return fullyVisible;
    }

    public float getScale() {
        return scale;
    }

    public Vector2 getPosition() {
        return new Vector2(position); // Return a copy
    }

    public Texture getTexture() {
        return texture;
    }

    public int getNumber() {
        return number;
    }

    public String getColor() {
        return color;
    }

    public Rectangle getBounds() {
        return new Rectangle(bounds); // Return a copy
    }

    public int getGridX() {
        return gridX;
    }

    public int getGridY() {
        return gridY;
    }

    public boolean isVibrating() {
        return vibrating;
    }


    // Setters
    public void setTexture(Texture texture) {
        this.texture = texture;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public void setAppearTime(float time) {
        this.appearTime = time;
        this.scale = 0.0f;
        this.fullyVisible = false;
    }

    public void setSpeedMultiplier(float multiplier) {
        this.speedMultiplier = multiplier;
    }

    public void setVibrating(boolean vibrating) {
        this.vibrating = vibrating;
    }
}