package application.model;

import java.util.Random;

public class MatchSimulator {
    private Team homeTeam;
    private Team awayTeam;
    private Random random;

    public MatchSimulator(Team homeTeam, Team awayTeam) {
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.random = new Random();
    }

    public void simulateMatch() {
        System.out.println("\n🏟️ MATCH START: " + awayTeam.getTeamName() + " @ " + homeTeam.getTeamName());
        System.out.println("----------------------------------------");

        int homeScore = 0;
        int awayScore = 0;

        // Simple baseball logic simulation: 9 Innings
        for (int inning = 1; inning <= 9; inning++) {
            // Away team bats first, Home team bats second
            awayScore += simulateHalfInning(awayTeam, homeTeam, inning, "Top");
            homeScore += simulateHalfInning(homeTeam, awayTeam, inning, "Bottom");
        }

        System.out.println("----------------------------------------");
        System.out.println("🏆 FINAL SCORE:");
        System.out.println(awayTeam.getTeamName() + ": " + awayScore);
        System.out.println(homeTeam.getTeamName() + ": " + homeScore);
        
        if (homeScore > awayScore) {
            System.out.println("🎉 Winner: " + homeTeam.getTeamName());
        } else if (awayScore > homeScore) {
            System.out.println("🎉 Winner: " + awayTeam.getTeamName());
        } else {
            System.out.println("🤝 It's a Tie!");
        }
        System.out.println("----------------------------------------\n");
    }

    private int simulateHalfInning(Team battingTeam, Team pitchingTeam, int inning, String half) {
        int runsScored = 0;
        int outs = 0;

        // Grab a representative batter and pitcher for the simulation calculations
        Player batter = battingTeam.getRoster().get(0); // Simple selection for now
        Player pitcher = pitchingTeam.getRoster().stream()
                .filter(p -> "Pitcher".equalsIgnoreCase(p.getPosition()))
                .findFirst()
                .orElse(pitchingTeam.getRoster().get(1));

        while (outs < 3) {
            // Core formula: Compare batter's contact vs pitcher's control + random variance
            int roll = random.nextInt(100) + 1; // 1 to 100
            int advantage = batter.getContact() - pitcher.getPitchingControl();
            int calculationValue = roll + advantage;

            if (calculationValue > 75) {
                runsScored++; // A great hit drives home a run
            } else if (calculationValue < 40) {
                outs++; // Batter gets struck out or caught out
            } else {
                // Base runner or walk scenario (chance to score or advance)
                if (random.nextBoolean()) {
                    runsScored++;
                } else {
                    outs++;
                }
            }
        }
        
        System.out.println("[" + half + " Inning " + inning + "] " + battingTeam.getTeamName() + " scores " + runsScored + " runs.");
        return runsScored;
    }
}