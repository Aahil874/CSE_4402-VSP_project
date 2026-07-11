package application.model;

import java.util.ArrayList;
import java.util.List;

public class Team {
    private String teamName;
    private int budget; // In-game currency for trading
    private List<Player> roster;

    public Team(String teamName, int startingBudget) {
        this.teamName = teamName;
        this.budget = startingBudget;
        this.roster = new ArrayList<>();
    }

    public void addPlayer(Player player) {
        roster.add(player);
    }

    public void removePlayer(Player player) {
        roster.remove(player);
    }

    // Getters and Setters
    public String getTeamName() { return teamName; }
    public int getBudget() { return budget; }
    public void setBudget(int budget) { this.budget = budget; }
    public List<Player> getRoster() { return roster; }
}