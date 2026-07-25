package application.model;

public class MatchSimulator {
    private Team homeTeam;
    private Team awayTeam;

    // Match State Variables
    private int homeScore = 0;
    private int awayScore = 0;
    private int currentInning = 1;
    private boolean isTopInning = true; // true = Top (Away batting), false = Bottom (Home batting)
    
    private int balls = 0;
    private int strikes = 0;
    private int outs = 0;

    // Roster Indexes to rotate through batters/pitchers
    private int homeBatterIndex = 0;
    private int awayBatterIndex = 0;

    public MatchSimulator(Team homeTeam, Team awayTeam) {
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
    }

    // --- Active Player Retrieval ---

    /**
     * Returns the active batter based on who is at bat (Top = Away, Bottom = Home).
     */
    public Player getCurrentBatter() {
        Team battingTeam = isTopInning ? awayTeam : homeTeam;
        int index = isTopInning ? awayBatterIndex : homeBatterIndex;
        
        if (battingTeam.getRoster() == null || battingTeam.getRoster().isEmpty()) {
            return null;
        }
        return battingTeam.getRoster().get(index % battingTeam.getRoster().size());
    }

    /**
     * Returns the active pitcher from the defending team.
     */
    public Player getCurrentPitcher() {
        Team pitchingTeam = isTopInning ? homeTeam : awayTeam;
        if (pitchingTeam.getRoster() == null || pitchingTeam.getRoster().isEmpty()) {
            return null;
        }

        // Try finding a designated Pitcher, or fallback to the second player
        return pitchingTeam.getRoster().stream()
                .filter(p -> "Pitcher".equalsIgnoreCase(p.getPosition()))
                .findFirst()
                .orElse(pitchingTeam.getRoster().get(Math.min(1, pitchingTeam.getRoster().size() - 1)));
    }

    // --- Real-time Game Event Handlers ---

    public void recordStrike() {
        strikes++;
        if (strikes >= 3) {
            recordOut();
        }
    }

    public void recordBall() {
        balls++;
        if (balls >= 4) {
            // Walk / Base on balls
            resetCount();
            rotateBatter();
        }
    }

    public void recordOut() {
        outs++;
        resetCount();
        rotateBatter();

        if (outs >= 3) {
            advanceInning();
        }
    }

    public void recordRun(int count) {
        if (isTopInning) {
            awayScore += count;
        } else {
            homeScore += count;
        }
    }

    public void recordHitSuccess() {
        resetCount();
        rotateBatter();
    }

    private void resetCount() {
        this.balls = 0;
        this.strikes = 0;
    }

    private void rotateBatter() {
        if (isTopInning) {
            awayBatterIndex++;
        } else {
            homeBatterIndex++;
        }
    }

    private void advanceInning() {
        outs = 0;
        resetCount();

        if (isTopInning) {
            isTopInning = false; // Switch to Bottom Inning
        } else {
            isTopInning = true;  // Switch to Top of next Inning
            currentInning++;
        }
    }

    // --- Display Utility Helpers ---

    public String getInningDisplay() {
        String half = isTopInning ? "TOP" : "BOT";
        return "Inning: " + currentInning + " " + half;
    }

    public String getCountDisplay() {
        return "B: " + balls + " | S: " + strikes + " | O: " + outs;
    }

    // --- Getters & Setters ---

    public Team getHomeTeam() { return homeTeam; }
    public Team getAwayTeam() { return awayTeam; }
    public int getHomeScore() { return homeScore; }
    public int getAwayScore() { return awayScore; }
    public int getCurrentInning() { return currentInning; }
    public boolean isTopInning() { return isTopInning; }
    public int getBalls() { return balls; }
    public int getStrikes() { return strikes; }
    public int getOuts() { return outs; }
}