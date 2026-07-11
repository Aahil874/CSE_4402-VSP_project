package application.model;

public class Player {
    private String name;
    private String position; // e.g., "Batter" or "Pitcher"
    
    // Core Attributes (0 to 100 scale)
    private int contact;
    private int power;
    private int speed;
    private int pitchingVelocity;
    private int pitchingControl;
    private int stamina;

    // Constructor
    public Player(String name, String position, int contact, int power, int speed, 
                  int pitchingVelocity, int pitchingControl, int stamina) {
        this.name = name;
        this.position = position;
        this.contact = contact;
        this.power = power;
        this.speed = speed;
        this.pitchingVelocity = pitchingVelocity;
        this.pitchingControl = pitchingControl;
        this.stamina = stamina;
    }

    // Standard Getters and Setters
    public String getName() { return name; }
    public String getPosition() { return position; }
    public int getContact() { return contact; }
    public int getPower() { return power; }
    public int getSpeed() { return speed; }
    public int getPitchingVelocity() { return pitchingVelocity; }
    public int getPitchingControl() { return pitchingControl; }
    public int getStamina() { return stamina; }
    
    // Quick calculation to show a visual average rating in your menus
    public int getOverallRating() {
        if ("Pitcher".equalsIgnoreCase(position)) {
            return (pitchingVelocity + pitchingControl + stamina) / 3;
        } else {
            return (contact + power + speed) / 3;
        }
    }
}