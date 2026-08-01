package application;

import java.sql.SQLException;
import java.util.List;


public final class LeaderboardService {
    public List<PlayerStats> load(int limit) throws SQLException {
        return GameBackendService.getInstance()
                .findChampionshipStandings(Math.max(1, limit));
    }
}
