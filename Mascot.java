package application;

public enum Mascot {
    TURBO_TANUKI("TURBO TANUKI", "Tanuki", "Fearless and upbeat", "#ff5b35", "#ffd166",
            1.10, 1.00, 1.00, 1.12, 1.02, 1.06, "TURBO TAILS", "NEON HARBOR PARK",
            "TAILWIND FASTBALL", "GRAND SLAM BURST", "Tail-spin victory dance", "Determined cap-tip"),
    BUBBLE_BUNNY("BUBBLE BUNNY", "Rabbit", "Cheerful tactician", "#ff71ce", "#fff1f7",
            0.90, 1.22, 0.94, 1.20, 0.94, 1.14, "BUBBLE BLITZ", "MOONBOUNCE FIELD",
            "CLOUD CHANGEUP", "BOUNCE-BACK LINER", "Bubble-hop celebration", "Ears-down reset"),
    ROCKET_REX("ROCKET REX", "Dinosaur", "Loud powerhouse", "#66d450", "#ffe066",
            1.28, 0.84, 0.98, 0.94, 1.20, 0.98, "ROCKET ROAR", "METEOR YARD",
            "METEOR SINKER", "METEOR SMASH", "Roaring stomp", "Tiny frustrated roar"),
    NOVA_NEKO("NOVA NEKO", "Cat", "Cool and calculating", "#7c6cff", "#56f2e3",
            0.96, 1.05, 1.22, 1.16, 1.12, 1.14, "NOVA STRIKERS", "STARDUST DOME",
            "STARBREAKER CURVE", "COMET CONTACT", "Starlight paw pose", "Focused tail flick"),
    BLAZE_FALCON("BLAZE FALCON", "Falcon", "Bold aerial ace", "#e34234", "#ffd166",
            1.02, 0.96, 1.12, 1.25, 1.18, 1.18, "SKYFIRE WINGS", "SUNBURST AERIE",
            "FIRECUTTER", "WINGSHOT DRIVE", "Victory wing flare", "Feather shake-off"),
    FROST_WOLF("FROST WOLF", "Wolf", "Calm clutch defender", "#66c7ff", "#eaf8ff",
            1.06, 1.03, 1.10, 1.16, 1.12, 1.15, "GLACIER HOWL", "FROSTBITE PARK",
            "ICE SLIDER", "AVALANCHE LINER", "Moon-howl celebration", "Quiet snow-paw reset"),
    REEF_SHARK("REEF SHARK", "Shark", "Relentless competitor", "#16b8b1", "#133c78",
            1.18, 0.92, 1.04, 1.05, 1.16, 1.00, "TIDAL FINS", "DEEPWATER DIAMOND",
            "RIPTIDE SINKER", "TIDAL UPPERCUT", "Fin-wave celebration", "Circling regroup"),
    MAPLE_RED_PANDA("MAPLE RED PANDA", "Red panda", "Quick and clever", "#b83b3b", "#fff0d0",
            0.94, 1.14, 0.95, 1.22, 0.98, 1.10, "MAPLE DASH", "CANOPY FIELD",
            "LEAFCUT CHANGE", "CANOPY CHOP", "Tail-banner spin", "Paw-to-cap salute"),
    HARBOR_TURTLE("HARBOR TURTLE", "Turtle", "Patient defensive wall", "#3ba55d", "#d98932",
            1.12, 1.04, 1.08, 0.82, 1.08, 1.20, "HARBOR SHELLS", "ANCHOR BAY PARK",
            "ANCHOR KNUCKLER", "SHELLSHOCK DRIVE", "Shell-spin shuffle", "Steady shell tap"),
    EMBER_DRAGON("EMBER DRAGON", "Dragon", "Fiery showstopper", "#8b1e3f", "#ff7a2f",
            1.24, 0.90, 1.16, 1.04, 1.20, 1.02, "EMBER FLIGHT", "CINDER CROWN STADIUM",
            "DRAGONFIRE FORK", "INFERNO BLAST", "Spark-breath celebration", "Smoke-ring sigh"),
    VOLT_TIGER("VOLT TIGER", "Tiger", "Electric speedster", "#f5bd1f", "#1f78ff",
            1.15, 1.02, 1.04, 1.20, 1.12, 1.08, "VOLT STRIPES", "THUNDERGRID FIELD",
            "LIGHTNING SCREWBALL", "THUNDERCLAP HIT", "Electric claw pose", "Stripe-reset shake"),
    CIRCUIT_BOT("CIRCUIT BOT", "Robot", "Precise and friendly", "#f4fbff", "#22e6e6",
            1.00, 1.10, 1.14, 1.06, 1.15, 1.16, "CIRCUIT NINES", "NEON CORE ARENA",
            "PIXEL PERFECT FOUR-SEAM", "BINARY BASH", "Hologram victory loop", "Recalibration blink");

    private final String displayName, species, personality, primaryColor, accentColor;
    private final double powerMultiplier, contactMultiplier, pitchingMultiplier;
    private final double speedMultiplier, throwingMultiplier, fieldingMultiplier;
    private final String teamName, stadiumName, signaturePitch, signatureHit;
    private final String celebration, losingReaction;

    Mascot(String displayName, String species, String personality, String primaryColor,
           String accentColor, double power, double contact, double pitching, double speed,
           double throwing, double fielding, String teamName, String stadiumName,
           String signaturePitch, String signatureHit, String celebration, String losingReaction) {
        this.displayName = displayName; this.species = species; this.personality = personality;
        this.primaryColor = primaryColor; this.accentColor = accentColor;
        this.powerMultiplier = power; this.contactMultiplier = contact;
        this.pitchingMultiplier = pitching; this.speedMultiplier = speed;
        this.throwingMultiplier = throwing; this.fieldingMultiplier = fielding;
        this.teamName = teamName; this.stadiumName = stadiumName;
        this.signaturePitch = signaturePitch; this.signatureHit = signatureHit;
        this.celebration = celebration; this.losingReaction = losingReaction;
    }

    public String getDisplayName() { return displayName; }
    public String getSpecies() { return species; }
    public String getPersonality() { return personality; }
    public String getPrimaryColor() { return primaryColor; }
    public String getAccentColor() { return accentColor; }
    public double getPowerMultiplier() { return powerMultiplier; }
    public double getContactMultiplier() { return contactMultiplier; }
    public double getPitchingMultiplier() { return pitchingMultiplier; }
    public double getSpeedMultiplier() { return speedMultiplier; }
    public double getThrowingMultiplier() { return throwingMultiplier; }
    public double getFieldingMultiplier() { return fieldingMultiplier; }
    public String getTeamName() { return teamName; }
    public String getStadiumName() { return stadiumName; }
    public String getSignaturePitch() { return signaturePitch; }
    public String getSignatureHit() { return signatureHit; }
    public String getSignatureMove() { return signatureHit; }
    public String getCelebration() { return celebration; }
    public String getLosingReaction() { return losingReaction; }
    public String getDescription() { return personality + " " + species + " all-star."; }
    @Override public String toString() { return displayName; }
}
