package model;

public class MatchPerformance {

    private String matchId;
    private int kills;
    private int deaths;
    private int assists;
    private int cs;
    private double minutes;
    private int visionScore;
    private int damageDealt;
    private boolean win;
    private String champion;
    private String role;
    private long gameStartTimestamp;

    public MatchPerformance(String matchId, int kills, int deaths, int assists, int cs,
                            double minutes, int visionScore, int damageDealt, boolean win,
                            String champion, String role, long gameStartTimestamp) {
        this.matchId            = matchId;
        this.kills              = kills;
        this.deaths             = deaths;
        this.assists            = assists;
        this.cs                 = cs;
        this.minutes            = minutes;
        this.visionScore        = visionScore;
        this.damageDealt        = damageDealt;
        this.win                = win;
        this.champion           = champion;
        this.role               = role;
        this.gameStartTimestamp = gameStartTimestamp;
    }

    public String getMatchId()           { return matchId; }
    public int getKills()                { return kills; }
    public int getDeaths()               { return deaths; }
    public int getAssists()              { return assists; }
    public int getCs()                   { return cs; }
    public double getMinutes()           { return minutes; }
    public int getVisionScore()          { return visionScore; }
    public int getDamageDealt()          { return damageDealt; }
    public boolean isWin()               { return win; }
    public String getChampion()          { return champion; }
    public String getRole()              { return role; }
    public long getGameStartTimestamp()  { return gameStartTimestamp; }
}
