package io.github.avx20.M4TCH;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class Tile {
    private int number; // 1, 2, or star
    private String color; // "red", "blue", "green"
    private Texture texture;
    private Vector2 position;
    private float scale; // Animation scale (0 to 1)
    private boolean isActive; // Whether the tile is clickable

    public Tile(int number, String color, Texture texture, Vector2 position) {
        this.number = number;
        this.color = color;
        this.texture = texture;
        this.position = position;
        this.scale = 0; // Start as a dot
        this.isActive = false; // Not clickable initially
    }

    /** Update the tile's animation. */
    public void update(float delta) {
        if (scale < 1) {
            scale += delta; // Grow the tile over time
            if (scale >= 1) {
                scale = 1;
                isActive = true; // Tile is now clickable
            }
        }
    }

    /** Render the tile at its position with animation. */
    public void render(SpriteBatch batch) {
        float size = 200 * scale; // Scaled size (160px when fully grown)
        batch.draw(texture, position.x + (200 - size) / 2, position.y + (200 - size) / 2, size, size);
    }

    // Getters and setters
    public int getNumber() { return number; }
    public String getColor() { return color; }
    public Vector2 getPosition() { return position; }
    public boolean isActive() { return isActive; }
    public void setNumber(int number) { this.number = number; }
    public void setTexture(Texture texture) { this.texture = texture; }
    public void setScale(float scale) { this.scale = scale; }
}
