package application.model;

public class Player {

    private String name;
    private String position;   // e.g., "Pitcher", "Batter"
    private String spritePath; // Path to visual sprite
    
    // Stats (Loaded from DB / Initialization)
    private double contact;
    private double power;
    private double speed;
    private double pitchSpeed;
    private double control;

    /**
     * Complete Constructor (8 Parameters)
     * Matches: name, position, spritePath, contact, power, speed, pitchSpeed, control
     */
    public Player(String name, String position, String spritePath, double contact, double power, double speed, double pitchSpeed, double control) {
        this.name = name;
        this.position = position;
        this.spritePath = spritePath;
        this.contact = contact;
        this.power = power;
        this.speed = speed;
        this.pitchSpeed = pitchSpeed;
        this.control = control;
    }

    /**
     * Overloaded Constructor (7 Parameters - Sets a default sprite path if omitted)
     */
    public Player(String name, String position, double contact, double power, double speed, double pitchSpeed, double control) {
        this(name, position, "/pictures/player.png", contact, power, speed, pitchSpeed, control);
    }

    /**
     * Dynamic Overall Rating (OVR)
     */
    public int getOverallRating() {
        if ("Pitcher".equalsIgnoreCase(position)) {
            double rating = (pitchSpeed * 0.45) + (control * 0.45) + (speed * 0.10);
            return (int) Math.round(rating);
        } else {
            double rating = (contact * 0.40) + (power * 0.40) + (speed * 0.20);
            return (int) Math.round(rating);
        }
    }

    // --- GETTERS & SETTERS ---

    public String getName() { return name; }
    public String getPosition() { return position; }
    public String getSpritePath() { return spritePath; }

    public int getContact() { return (int) contact; }
    public double getContactDouble() { return contact; }
    public double getPower() { return power; }
    public double getSpeed() { return speed; }
    public double getPitchSpeed() { return pitchSpeed; }
    public double getControl() { return control; }
    public int getPitchingControl() { return (int) control; }

    public void setPosition(String position) { this.position = position; }
    public void setSpritePath(String spritePath) { this.spritePath = spritePath; }
}