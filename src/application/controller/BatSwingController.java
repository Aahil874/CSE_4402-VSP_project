package application.controller;

import javafx.geometry.Rectangle2D;
import javafx.scene.image.ImageView;

public final class BatSwingController {

    public enum Phase { READY, LOAD, SWING, CONTACT, FOLLOW_THROUGH, RECOVERY }

    private Phase phase = Phase.READY;
    private double elapsed;

    // Sprite Sheet Configuration
    private ImageView batterImageView;
    private int frameWidth = 64;   // Set to your single frame width in pixels
    private int frameHeight = 64;  // Set to your single frame height in pixels

    public BatSwingController() {}

    public BatSwingController(ImageView batterImageView, int frameWidth, int frameHeight) {
        this.batterImageView = batterImageView;
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;
    }

    /**
     * Starts the swing sequence.
     */
    public void startSwing() {
        phase = Phase.LOAD;
        elapsed = 0;
    }

    /**
     * Legacy/alias trigger method for external callers.
     */
    public void begin() {
        startSwing();
    }

    /**
     * Call this inside your main animation loop (passing frame delta time in seconds).
     */
    public void update(double dt) {
        if (phase == Phase.READY && elapsed == 0) return;

        elapsed += Math.max(0, dt);

        // Update Phase
        phase = elapsed < 0.07 ? Phase.LOAD
                : elapsed < 0.14 ? Phase.SWING
                : elapsed < 0.18 ? Phase.CONTACT
                : elapsed < 0.31 ? Phase.FOLLOW_THROUGH
                : elapsed < 0.43 ? Phase.RECOVERY : Phase.READY;

        if (phase == Phase.READY) {
            elapsed = 0; // Reset after recovery completes
        }

        // Update Sprite Sheet Viewport Frame
        updateSpriteFrame();
    }

    /**
     * Maps the active swing phase to the corresponding column frame on the sprite sheet.
     */
    private void updateSpriteFrame() {
        if (batterImageView == null) return;

        int frameIndex;
        switch (phase) {
            case LOAD:           frameIndex = 1; break;
            case SWING:          frameIndex = 2; break;
            case CONTACT:        frameIndex = 3; break;
            case FOLLOW_THROUGH: frameIndex = 4; break;
            case RECOVERY:       frameIndex = 5; break;
            case READY:
            default:             frameIndex = 0; break;
        }

        // Sets the visible crop area on the sprite sheet PNG (assuming 1 row of frames)
        batterImageView.setViewport(new Rectangle2D(frameIndex * frameWidth, 0, frameWidth, frameHeight));
    }

    public Phase phase() { return phase; }
    
    public boolean collisionEnabled() { 
        return phase == Phase.SWING || phase == Phase.CONTACT; 
    }

    public void setBatterImageView(ImageView imageView) {
        this.batterImageView = imageView;
    }
}