package application;


public record Team(String name, String stadium, Mascot mascot,
                   String primaryColor, String accentColor) {
    public static Team fromMascot(Mascot mascot) {
        return new Team(mascot.getTeamName(), mascot.getStadiumName(), mascot,
                mascot.getPrimaryColor(), mascot.getAccentColor());
    }
}
