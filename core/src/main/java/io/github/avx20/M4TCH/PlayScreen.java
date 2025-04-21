package io.github.avx20.M4TCH;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
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
    private Sound matchSuccessSound;
    private Sound matchFailSound;

    private Tile[][] grid = new Tile[4][4];
    private final float TILE_SIZE = 200;
    private final float TILE_SPACING = 5;

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

    // Power-up states
    private boolean freezeTimeActive = false;
    private float freezeTimeRemaining = 0;
    private boolean comboMultiplierActive = false;
    private float comboMultiplierRemaining = 0;
    private boolean instantTilesActive = false;
    private float instantTilesRemaining = 0;

    // Combo system
    private int comboMultiplier = 1;
    private float comboTimeRemaining = 0;
    private boolean redMatchDuringAllPowerUps = false;
    private int redComboCount = 0;

    public PlayScreen(M4TCH game) {
        this.game = game;
        this.viewport = new FitViewport(1920, 1080);
        this.gameBackground = new Texture("game_bg.png");
        this.font = new BitmapFont();
        initializeGrid();
        matchSuccessSound = Gdx.audio.newSound(Gdx.files.internal("match_success.mp3"));
        matchFailSound = Gdx.audio.newSound(Gdx.files.internal("match_fail.mp3"));
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

        // Update power-up timers
        updatePowerUpTimers(delta);

        // Update time remaining (considering freeze time power-up)
        if (!freezeTimeActive) {
            timeRemaining -= delta;
        } else {
            timeRemaining -= delta * 0.25f; // 75% slower
        }

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

        // Display power-up status
        if (freezeTimeActive) {
            font.draw(batch, "Freeze Time: " + (int) freezeTimeRemaining, 50, viewport.getWorldHeight() - 150);
        }
        if (comboMultiplierActive) {
            font.draw(batch, "Combo Multiplier: " + (int) comboMultiplierRemaining, 50, viewport.getWorldHeight() - 200);
        }
        if (instantTilesActive) {
            font.draw(batch, "Instant Tiles: " + (int) instantTilesRemaining, 50, viewport.getWorldHeight() - 250);
        }
        if (comboMultiplier > 1) {
            font.draw(batch, "Combo: x" + comboMultiplier, 50, viewport.getWorldHeight() - 300);
        }

        batch.end();

        if (!inputBlocked && !game.isPaused()) {
            handleTileSelection();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            pauseGame();
            game.pauseGame();
        }
    }

    private void updatePowerUpTimers(float delta) {
        // Update combo timer
        if (comboTimeRemaining > 0) {
            comboTimeRemaining -= delta;
            if (comboTimeRemaining <= 0) {
                comboMultiplier = 1;
                redComboCount = 0;
            }
        }

        // Update power-up timers
        if (freezeTimeActive) {
            freezeTimeRemaining -= delta;
            if (freezeTimeRemaining <= 0) {
                freezeTimeActive = false;
            }
        }

        if (comboMultiplierActive) {
            comboMultiplierRemaining -= delta;
            if (comboMultiplierRemaining <= 0) {
                comboMultiplierActive = false;
            }
        }

        if (instantTilesActive) {
            instantTilesRemaining -= delta;
            if (instantTilesRemaining <= 0) {
                instantTilesActive = false;
            }
        }
    }

    private void handleTileSelection() {
        if (Gdx.input.justTouched()) {
            float touchX = Gdx.input.getX();
            float touchY = viewport.getWorldHeight() - Gdx.input.getY();

            for (int row = 3; row >= 0; row--) {
                for (int col = 0; col < 4; col++) {
                    Tile tile = grid[row][col];
                    if (tile != null && (tile.isFullyVisible() || instantTilesActive)) {
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
                matchFailSound.play();
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
        matchSuccessSound.play();
        int row1 = tile1.getGridY();
        int col1 = tile1.getGridX();
        int row2 = tile2.getGridY();
        int col2 = tile2.getGridX();

        String color = tile1.getColor();

        // Activate power-ups based on color
        if (color.equals("blue")) {
            freezeTimeActive = true;
            freezeTimeRemaining = 5;
        } else if (color.equals("red")) {
            comboMultiplierActive = true;
            comboMultiplierRemaining = 7;

            // Check for red combo
            if (allPowerUpsActive()) {
                redMatchDuringAllPowerUps = true;
                comboTimeRemaining = 0.5f;
                redComboCount++;
                comboMultiplier = (int) Math.pow(2, redComboCount);
            }
        } else if (color.equals("green")) {
            instantTilesActive = true;
            if (instantTilesRemaining > 0) {
                instantTilesRemaining += 10; // Extend duration
            } else {
                instantTilesRemaining = 10;
            }
        }

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

        int baseScore = calculateScore(3, color);
        score += baseScore * comboMultiplier;
    }

    private void combineTiles(Tile tile1, Tile tile2) {
        matchSuccessSound.play();
        int originalNumber = tile1.getNumber(); // Store the original number for scoring
        int newNumber = originalNumber + 1;
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
    
        // Calculate score based on the original number before combination
        int baseScore = calculateScore(originalNumber, color);
        score += baseScore * comboMultiplier;
    
        // Reset combo timer if not a red match during all power-ups
        if (!(color.equals("red") && allPowerUpsActive())) {
            comboTimeRemaining = 0.5f;
        }
    }

    private int calculateScore(int number, String color) {
        boolean allPowerUpsActive = allPowerUpsActive();
        boolean cmActive = comboMultiplierActive;
    
        if (number == 1) { // Base tiles (number one tiles)
            if (allPowerUpsActive) return 290;
            if (cmActive) return 100;
            return 50;
        } 
        else if (number == 2) { // Intermediate tiles (number two tiles)
            if (allPowerUpsActive) return 610;
            if (cmActive) return 300;
            return 150;
        } 
        else if (number == 3) { // Star tiles
            // Special case for red star tiles when all power-ups are active
            if (color.equals("red") && allPowerUpsActive && redMatchDuringAllPowerUps) {
                return 5000;
            }
            // Normal star tile cases
            if (allPowerUpsActive) return 2500;
            if (cmActive) return 1000;
            return 500;
        }
        return 0;
    }

    private boolean allPowerUpsActive() {
        return freezeTimeActive && comboMultiplierActive && instantTilesActive;
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
        matchSuccessSound.dispose();
        matchFailSound.dispose();
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