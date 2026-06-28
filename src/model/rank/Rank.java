package model.rank;

public record Rank(
    RankTier tier,
    RankDivision division,
    int lp
) {
    public static Rank parse(String s) {
        if (s == null || s.isBlank()) return new Rank(RankTier.UNKNOWN, RankDivision.NONE, 0);
        String[] parts = s.trim().toUpperCase().split("[_\\s]+");
        RankTier tier = RankTier.valueOf(parts[0]);
        RankDivision division = parts.length > 1 ? RankDivision.valueOf(parts[1]) : RankDivision.NONE;
        return new Rank(tier, division, 0);
    }
}
