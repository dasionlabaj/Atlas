package model.rank;

public record RankGap(
    Rank predicted,
    Rank actual,
    int gapInDivisions
) {
    public boolean isAligned()         { return gapInDivisions == 0; }
    public boolean isPredictedHigher() { return gapInDivisions > 0; }
}
