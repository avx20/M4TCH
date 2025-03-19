package io.github.avx20.M4TCH;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Tile {
    private int number; // 1, 2, or star
    private String color; // "red", "blue", "green"
    private Texture texture;
    private Vector2 position;
    private Rectangle bounds;

    public Tile(int number, String color, Texture texture, Vector2 position) {
        this.number = number;
        this.color = color;
        this.texture = texture;
        this.position = position;
        this.bounds = new Rectangle(position.x, position.y, texture.getWidth(), texture.getHeight());
    }

    public int getNumber() { return number; }
    public String getColor() { return color; }
    public Texture getTexture() { return texture; }
    public Vector2 getPosition() { return position; }
    public Rectangle getBounds() { return bounds; }

    public void setNumber(int number) { this.number = number; }
    public void setTexture(Texture texture) { this.texture = texture; }
}
