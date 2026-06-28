package datasource.riot;

class RiotEndpoints {
    static final String ACCOUNT =
        "https://europe.api.riotgames.com/riot/account/v1/accounts/by-riot-id/{gameName}/{tagLine}";
    static final String MATCH_IDS =
        "https://europe.api.riotgames.com/lol/match/v5/matches/by-puuid/{puuid}/ids?count={count}";
    static final String MATCH =
        "https://europe.api.riotgames.com/lol/match/v5/matches/{matchId}";
}
