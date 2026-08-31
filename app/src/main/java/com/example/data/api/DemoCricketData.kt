package com.example.data.api

import com.example.model.*

object DemoCricketData {

    val demoMatches: List<CricketMatch> = listOf(
        CricketMatch(
            id = "match_ind_aus_01",
            tournament = "International T20 Championship 2026",
            matchFormat = MatchFormat.T20,
            team1Name = "India",
            team1Short = "IND",
            team1LogoEmoji = "🇮🇳",
            team1Score = "178/4",
            team1Overs = "18.3",
            team2Name = "Australia",
            team2Short = "AUS",
            team2LogoEmoji = "🇦🇺",
            team2Score = "174/7",
            team2Overs = "20.0",
            status = MatchStatus.LIVE,
            statusSummary = "India need 5 runs in 9 balls to win",
            venue = "Eden Gardens, Kolkata",
            startTime = "Today, 7:00 PM IST",
            currentInnings = 2,
            currentBatsmen = listOf(
                BatsmanScore(name = "Virat Kohli", runs = 76, balls = 48, fours = 7, sixes = 3, strikeRate = 158.33, isStriker = true),
                BatsmanScore(name = "Hardik Pandya", runs = 29, balls = 15, fours = 2, sixes = 2, strikeRate = 193.33, isStriker = false)
            ),
            currentBowlers = listOf(
                BowlerFigure(name = "Mitchell Starc", overs = 3.3, maidens = 0, runs = 34, wickets = 2, economy = 9.71, isCurrentBowler = true),
                BowlerFigure(name = "Pat Cummins", overs = 4.0, maidens = 0, runs = 38, wickets = 1, economy = 9.50, isCurrentBowler = false)
            ),
            recentBalls = listOf(
                BallEvent("18.3", "4", "Kohli drives through extra cover for a glorious boundary!", "Virat Kohli", "Mitchell Starc", isBoundary = true),
                BallEvent("18.2", "1", "Pandya tucks it to deep square leg for a single", "Hardik Pandya", "Mitchell Starc"),
                BallEvent("18.1", "6", "PULLED AWAY! Pandya hammers a short ball over deep mid-wicket for SIX!", "Hardik Pandya", "Mitchell Starc", isBoundary = true),
                BallEvent("17.6", "1", "Kohli pushes to mid-off and scampers across", "Virat Kohli", "Pat Cummins"),
                BallEvent("17.5", "2", "Fast running between the wickets, 2 runs taken", "Virat Kohli", "Pat Cummins"),
                BallEvent("17.4", "W", "OUT! Clean bowled! Suryakumar Yadav swings and misses a fuller delivery", "Suryakumar Yadav", "Pat Cummins", isWicket = true)
            ),
            partnershipRuns = 42,
            partnershipBalls = 21,
            fallOfWickets = listOf(
                FallOfWicket(34, 1, 3.4, "Rohit Sharma"),
                FallOfWicket(82, 2, 9.2, "Shubman Gill"),
                FallOfWicket(136, 3, 15.1, "Suryakumar Yadav"),
                FallOfWicket(142, 4, 15.5, "Rishabh Pant")
            ),
            target = 175,
            requiredRunRate = 3.33,
            currentRunRate = 9.62,
            broadcasterUrl = "https://www.hotstar.com/sports/cricket",
            broadcasterName = "Disney+ Hotstar Live",
            isDemo = true
        ),
        CricketMatch(
            id = "match_eng_sa_02",
            tournament = "Tri-Nation Series 2026",
            matchFormat = MatchFormat.ODI,
            team1Name = "England",
            team1Short = "ENG",
            team1LogoEmoji = "🏴󠁧󠁢󠁥󠁮󠁧󠁿",
            team1Score = "",
            team1Overs = "",
            team2Name = "South Africa",
            team2Short = "SA",
            team2LogoEmoji = "🇿🇦",
            team2Score = "",
            team2Overs = "",
            status = MatchStatus.UPCOMING,
            statusSummary = "Match begins at 2:30 PM Local Time",
            venue = "Lord's Cricket Ground, London",
            startTime = "Today, 6:30 PM IST",
            countdownText = "Starts in 01h 45m",
            broadcasterUrl = "https://www.sonyliv.com/sports",
            broadcasterName = "Sony LIV Sports",
            isDemo = true
        ),
        CricketMatch(
            id = "match_nz_pak_03",
            tournament = "Super Series T20",
            matchFormat = MatchFormat.T20,
            team1Name = "New Zealand",
            team1Short = "NZ",
            team1LogoEmoji = "🇳🇿",
            team1Score = "158/5",
            team1Overs = "17.4",
            team2Name = "Pakistan",
            team2Short = "PAK",
            team2LogoEmoji = "🇵🇰",
            team2Score = "165/8",
            team2Overs = "20.0",
            status = MatchStatus.LIVE,
            statusSummary = "New Zealand need 8 runs in 14 balls",
            venue = "Melbourne Cricket Ground, Melbourne",
            startTime = "Today, 1:30 PM IST",
            currentInnings = 2,
            currentBatsmen = listOf(
                BatsmanScore(name = "Glenn Phillips", runs = 48, balls = 29, fours = 4, sixes = 2, strikeRate = 165.5, isStriker = true),
                BatsmanScore(name = "Mitchell Santner", runs = 14, balls = 8, fours = 1, sixes = 1, strikeRate = 175.0, isStriker = false)
            ),
            currentBowlers = listOf(
                BowlerFigure(name = "Shaheen Afridi", overs = 3.4, maidens = 0, runs = 28, wickets = 2, economy = 7.63, isCurrentBowler = true)
            ),
            recentBalls = listOf(
                BallEvent("17.4", "1", "Phillips glides to third man for single", "Glenn Phillips", "Shaheen Afridi"),
                BallEvent("17.3", "4", "Slapped through backward point for FOUR!", "Glenn Phillips", "Shaheen Afridi", isBoundary = true),
                BallEvent("17.2", "0", "Good yorker length, dug out back to bowler", "Glenn Phillips", "Shaheen Afridi"),
                BallEvent("17.1", "2", "Santner places into deep midwicket gap", "Mitchell Santner", "Shaheen Afridi")
            ),
            partnershipRuns = 32,
            partnershipBalls = 18,
            target = 166,
            currentRunRate = 8.94,
            broadcasterUrl = "https://www.fanocode.com",
            broadcasterName = "FanCode Live",
            isDemo = true
        ),
        CricketMatch(
            id = "match_wi_sl_04",
            tournament = "Caribbean Trophy 2026",
            matchFormat = MatchFormat.T20,
            team1Name = "West Indies",
            team1Short = "WI",
            team1LogoEmoji = "🌴",
            team1Score = "186/6",
            team1Overs = "20.0",
            team2Name = "Sri Lanka",
            team2Short = "SL",
            team2LogoEmoji = "🇱🇰",
            team2Score = "162/9",
            team2Overs = "20.0",
            status = MatchStatus.COMPLETED,
            statusSummary = "West Indies won by 24 runs",
            venue = "Kensington Oval, Barbados",
            startTime = "Yesterday",
            broadcasterUrl = "https://www.hotstar.com",
            broadcasterName = "Official Broadcast",
            isDemo = true
        )
    )

    // Player pool for India vs Australia (Total 28 players across both sides to pick 17 from)
    val matchPlayersIndAus: List<Player> = listOf(
        // Wicketkeepers (3 available, need 2)
        Player("p1", "Rishabh Pant", "IND", PlayerRole.WICKET_KEEPER, "🧤", 9.5, listOf(42, 68, 18, 55, 78), totalRuns = 2450, totalCatches = 68, strikeRate = 144.2, currentPerformance = PlayerMatchPerformance(runs = 18, balls = 12, fours = 2, sixes = 1, catches = 1, fantasyPoints = 36)),
        Player("p2", "Alex Carey", "AUS", PlayerRole.WICKET_KEEPER, "🧤", 8.5, listOf(24, 39, 45, 12, 58), totalRuns = 1820, totalCatches = 54, strikeRate = 128.5, currentPerformance = PlayerMatchPerformance(runs = 31, balls = 22, fours = 3, sixes = 0, catches = 2, fantasyPoints = 51)),
        Player("p3", "Sanju Samson", "IND", PlayerRole.WICKET_KEEPER, "🧤", 8.5, listOf(85, 12, 102, 4, 30), totalRuns = 1950, totalCatches = 32, strikeRate = 152.0, currentPerformance = PlayerMatchPerformance(fantasyPoints = 15)),

        // Batsmen (9 available, need 6)
        Player("p4", "Virat Kohli", "IND", PlayerRole.BATSMAN, "👑", 10.5, listOf(92, 48, 76, 112, 54), totalRuns = 4280, strikeRate = 138.8, currentPerformance = PlayerMatchPerformance(runs = 76, balls = 48, fours = 7, sixes = 3, catches = 1, fantasyPoints = 112)),
        Player("p5", "Rohit Sharma", "IND", PlayerRole.BATSMAN, "💥", 10.0, listOf(65, 82, 15, 40, 94), totalRuns = 3980, strikeRate = 141.5, currentPerformance = PlayerMatchPerformance(runs = 34, balls = 20, fours = 4, sixes = 2, fantasyPoints = 48)),
        Player("p6", "Travis Head", "AUS", PlayerRole.BATSMAN, "⚡", 9.5, listOf(78, 92, 14, 52, 60), totalRuns = 2100, strikeRate = 158.4, currentPerformance = PlayerMatchPerformance(runs = 58, balls = 32, fours = 6, sixes = 3, fantasyPoints = 88)),
        Player("p7", "Suryakumar Yadav", "IND", PlayerRole.BATSMAN, "🎯", 9.5, listOf(35, 72, 18, 88, 42), totalRuns = 2650, strikeRate = 169.2, currentPerformance = PlayerMatchPerformance(runs = 24, balls = 14, fours = 3, sixes = 1, fantasyPoints = 33)),
        Player("p8", "David Warner", "AUS", PlayerRole.BATSMAN, "🏏", 9.0, listOf(40, 55, 28, 74, 18), totalRuns = 3200, strikeRate = 142.1, currentPerformance = PlayerMatchPerformance(runs = 42, balls = 28, fours = 4, sixes = 1, fantasyPoints = 53)),
        Player("p9", "Shubman Gill", "IND", PlayerRole.BATSMAN, "🌟", 9.0, listOf(55, 34, 82, 20, 48), totalRuns = 1850, strikeRate = 136.0, currentPerformance = PlayerMatchPerformance(runs = 28, balls = 18, fours = 3, sixes = 1, fantasyPoints = 37)),
        Player("p10", "Mitchell Marsh", "AUS", PlayerRole.BATSMAN, "💪", 9.0, listOf(32, 64, 48, 12, 70), totalRuns = 2300, strikeRate = 135.5, currentPerformance = PlayerMatchPerformance(runs = 38, balls = 24, fours = 3, sixes = 2, fantasyPoints = 52)),
        Player("p11", "Yashasvi Jaiswal", "IND", PlayerRole.BATSMAN, "🔥", 8.5, listOf(68, 44, 90, 16, 52), totalRuns = 1400, strikeRate = 162.0, currentPerformance = PlayerMatchPerformance(fantasyPoints = 10)),
        Player("p12", "Steve Smith", "AUS", PlayerRole.BATSMAN, "🛡️", 8.5, listOf(30, 42, 50, 22, 38), totalRuns = 2800, strikeRate = 125.0, currentPerformance = PlayerMatchPerformance(runs = 22, balls = 18, fours = 2, sixes = 0, fantasyPoints = 26)),

        // All-Rounders (5 available, need 3)
        Player("p13", "Hardik Pandya", "IND", PlayerRole.ALL_ROUNDER, "⚡", 9.5, listOf(60, 45, 78, 32, 85), totalRuns = 1750, totalWickets = 86, strikeRate = 145.0, economy = 8.1, currentPerformance = PlayerMatchPerformance(runs = 29, balls = 15, fours = 2, sixes = 2, wickets = 1, oversBowled = 3.0, runsConceded = 24, fantasyPoints = 68)),
        Player("p14", "Glenn Maxwell", "AUS", PlayerRole.ALL_ROUNDER, "🌪️", 9.5, listOf(88, 12, 105, 34, 42), totalRuns = 2500, totalWickets = 52, strikeRate = 154.0, economy = 7.9, currentPerformance = PlayerMatchPerformance(runs = 18, balls = 11, fours = 2, sixes = 1, wickets = 1, oversBowled = 2.0, runsConceded = 18, fantasyPoints = 44)),
        Player("p15", "Ravindra Jadeja", "IND", PlayerRole.ALL_ROUNDER, "🗡️", 9.0, listOf(42, 55, 68, 38, 70), totalRuns = 1600, totalWickets = 98, strikeRate = 129.0, economy = 6.9, currentPerformance = PlayerMatchPerformance(wickets = 2, oversBowled = 4.0, runsConceded = 28, catches = 1, fantasyPoints = 60)),
        Player("p16", "Marcus Stoinis", "AUS", PlayerRole.ALL_ROUNDER, "🦍", 8.5, listOf(35, 48, 22, 60, 40), totalRuns = 1450, totalWickets = 42, strikeRate = 139.0, economy = 8.6, currentPerformance = PlayerMatchPerformance(runs = 14, balls = 9, fours = 1, sixes = 1, oversBowled = 1.0, runsConceded = 12, fantasyPoints = 24)),
        Player("p17", "Axar Patel", "IND", PlayerRole.ALL_ROUNDER, "🎯", 8.5, listOf(50, 38, 62, 45, 58), totalRuns = 950, totalWickets = 72, strikeRate = 135.0, economy = 7.1, currentPerformance = PlayerMatchPerformance(wickets = 1, oversBowled = 4.0, runsConceded = 30, fantasyPoints = 35)),

        // Bowlers (8 available, need 5)
        Player("p18", "Jasprit Bumrah", "IND", PlayerRole.BOWLER, "🚀", 10.0, listOf(85, 90, 72, 110, 68), totalWickets = 142, economy = 6.4, currentPerformance = PlayerMatchPerformance(wickets = 3, oversBowled = 4.0, maidens = 1, runsConceded = 18, fantasyPoints = 95)),
        Player("p19", "Pat Cummins", "AUS", PlayerRole.BOWLER, "🏏", 9.5, listOf(62, 70, 45, 80, 58), totalWickets = 118, economy = 7.6, currentPerformance = PlayerMatchPerformance(wickets = 1, oversBowled = 4.0, maidens = 0, runsConceded = 38, fantasyPoints = 30)),
        Player("p20", "Mitchell Starc", "AUS", PlayerRole.BOWLER, "🎯", 9.5, listOf(74, 52, 85, 40, 92), totalWickets = 135, economy = 7.9, currentPerformance = PlayerMatchPerformance(wickets = 2, oversBowled = 3.3, maidens = 0, runsConceded = 34, fantasyPoints = 50)),
        Player("p21", "Kuldeep Yadav", "IND", PlayerRole.BOWLER, "🔮", 9.0, listOf(68, 84, 50, 78, 62), totalWickets = 94, economy = 7.0, currentPerformance = PlayerMatchPerformance(wickets = 2, oversBowled = 4.0, maidens = 0, runsConceded = 26, fantasyPoints = 50)),
        Player("p22", "Adam Zampa", "AUS", PlayerRole.BOWLER, "🌪️", 9.0, listOf(55, 72, 48, 65, 80), totalWickets = 105, economy = 7.3, currentPerformance = PlayerMatchPerformance(wickets = 1, oversBowled = 4.0, maidens = 0, runsConceded = 31, fantasyPoints = 30)),
        Player("p23", "Mohammed Siraj", "IND", PlayerRole.BOWLER, "🔥", 8.5, listOf(48, 65, 40, 85, 52), totalWickets = 82, economy = 8.0, currentPerformance = PlayerMatchPerformance(wickets = 1, oversBowled = 4.0, maidens = 0, runsConceded = 36, fantasyPoints = 30)),
        Player("p24", "Josh Hazlewood", "AUS", PlayerRole.BOWLER, "📐", 9.0, listOf(58, 62, 70, 45, 60), totalWickets = 102, economy = 7.1, currentPerformance = PlayerMatchPerformance(wickets = 1, oversBowled = 4.0, maidens = 0, runsConceded = 29, fantasyPoints = 30)),
        Player("p25", "Arshdeep Singh", "IND", PlayerRole.BOWLER, "🏹", 8.5, listOf(60, 50, 75, 42, 68), totalWickets = 88, economy = 8.2, currentPerformance = PlayerMatchPerformance(fantasyPoints = 0))
    )

    // Demo Prediction Questions (All ₹0, free-to-play with non-cash in-app points)
    val demoPredictions: List<PredictionQuestion> = listOf(
        PredictionQuestion(
            id = "pred_01",
            matchId = "match_ind_aus_01",
            question = "Will Virat Kohli score a boundary (4 or 6) in the 19th over?",
            options = listOf(
                PredictionOption("opt_1a", "Yes, Boundary"),
                PredictionOption("opt_1b", "No Boundary")
            ),
            pointsReward = 50,
            status = PredictionStatus.OPEN,
            closingOverText = "Closes at Over 19.0"
        ),
        PredictionQuestion(
            id = "pred_02",
            matchId = "match_ind_aus_01",
            question = "Who will win the match between India and Australia?",
            options = listOf(
                PredictionOption("opt_2a", "India (Needs 5 off 9)"),
                PredictionOption("opt_2b", "Australia (Defending 4)")
            ),
            pointsReward = 100,
            status = PredictionStatus.OPEN,
            closingOverText = "Open until 19.3 Overs"
        ),
        PredictionQuestion(
            id = "pred_03",
            matchId = "match_ind_aus_01",
            question = "Will Mitchell Starc take a wicket in the 19th over?",
            options = listOf(
                PredictionOption("opt_3a", "Yes, Takes Wicket"),
                PredictionOption("opt_3b", "No Wicket")
            ),
            pointsReward = 50,
            status = PredictionStatus.OPEN,
            closingOverText = "Closes at Over 19.0"
        ),
        PredictionQuestion(
            id = "pred_04",
            matchId = "match_ind_aus_01",
            question = "Will India's 2nd innings total cross 175 runs within 19 overs?",
            options = listOf(
                PredictionOption("opt_4a", "Yes, Crossed"),
                PredictionOption("opt_4b", "No, Over 20")
            ),
            pointsReward = 50,
            status = PredictionStatus.SETTLED,
            correctOptionId = "opt_4a",
            userSelectedOptionId = "opt_4a",
            isUserCorrect = true,
            awardedPoints = 50
        )
    )

    // Demo Leaderboards
    val demoGlobalLeaderboard: List<LeaderboardEntry> = listOf(
        LeaderboardEntry(1, "u_01", "CricMaster99", "👑", 4820, "Legendary 17", false, "Bangalore", 0),
        LeaderboardEntry(2, "u_02", "YorkerKing", "⚡", 4690, "Prediction Master", false, "Delhi", 1),
        LeaderboardEntry(3, "u_03", "SixerSam", "🔥", 4540, "Top 10", false, "Chennai", -1),
        LeaderboardEntry(4, "u_04", "SpinWizard", "🎯", 4320, "Cricket Fan", false, "Kolkata", 2),
        LeaderboardEntry(42, "usr_cricket_01", "CricketChamp17", "🏏", 1850, "Cricket Fan", true, "Mumbai", 3),
        LeaderboardEntry(43, "u_05", "BoundaryBoy", "🦁", 1820, "Cricket Fan", false, "Pune", -2),
        LeaderboardEntry(44, "u_06", "AllRounderPro", "🌟", 1790, "Cricket Fan", false, "Hyderabad", 0)
    )

    val demoFriendsLeaderboard: List<LeaderboardEntry> = listOf(
        LeaderboardEntry(1, "usr_cricket_01", "CricketChamp17 (You)", "🏏", 1850, "Cricket Fan", true, "Mumbai", 0),
        LeaderboardEntry(2, "f_01", "Rahul_Cricket", "🚀", 1620, "Cricket Fan", false, "Mumbai", 0),
        LeaderboardEntry(3, "f_02", "AmitSharma_07", "💥", 1490, "Cricket Fan", false, "Navi Mumbai", 0),
        LeaderboardEntry(4, "f_03", "Pooja_17", "✨", 1250, "Cricket Fan", false, "Thane", 0)
    )

    // Demo Badges
    val demoBadges: List<AchievementBadge> = listOf(
        AchievementBadge("b1", "First Match", "Created your first 17-player squad", "🏏", true, 1, 1, 100, "2026-08-25"),
        AchievementBadge("b2", "First Prediction", "Made your first match prediction", "🔮", true, 1, 1, 100, "2026-08-26"),
        AchievementBadge("b3", "1,000 Points", "Earned 1,000 lifetime in-app points", "🎖️", true, 1850, 1000, 200, "2026-08-28"),
        AchievementBadge("b4", "5,000 Points", "Reach 5,000 lifetime in-app points", "🏆", false, 1850, 5000, 500),
        AchievementBadge("b5", "Top 100", "Break into the top 100 global leaderboard", "🌟", true, 1, 1, 300, "2026-08-29"),
        AchievementBadge("b6", "Top 10", "Reach the top 10 ranking in any leaderboard", "👑", false, 0, 1, 1000),
        AchievementBadge("b7", "Prediction Master", "Get 15+ correct predictions in a month", "🎯", true, 19, 15, 400, "2026-08-29"),
        AchievementBadge("b8", "Cricket Fan", "Follow 10+ live matches", "❤️", true, 14, 10, 150, "2026-08-27"),
        AchievementBadge("b9", "10 Matches Played", "Form a 17-player squad in 10 matches", "🛡️", true, 14, 10, 250, "2026-08-28"),
        AchievementBadge("b10", "50 Matches Played", "Form squads in 50 matches", "⚔️", false, 14, 50, 750),
        AchievementBadge("b11", "Referral Champion", "Successfully invite 5 friends to 17Player", "🤝", false, 4, 5, 500)
    )

    // Demo Cricket Updates (original content cards)
    val demoUpdates: List<CricketUpdate> = listOf(
        CricketUpdate(
            "up_1",
            "Pitch Report: Kolkata Stadium",
            "Eden Gardens pitch is offering excellent turn under the lights with high bounce favoring pacers in early overs.",
            "Pitch Report",
            "10m ago"
        ),
        CricketUpdate(
            "up_2",
            "Mastering the 17-Player Squad Dynamic",
            "Selecting 2 WK, 6 BAT, 3 AR, and 5 BOWL ensures balanced points across both batting phases and death-overs bowling.",
            "Strategy Guide",
            "1h ago"
        ),
        CricketUpdate(
            "up_3",
            "Tournament Points Surge This Weekend",
            "Earn extra in-app non-cash achievement badges and leaderboard ranks across the tri-nation clashes.",
            "Announcement",
            "3h ago"
        )
    )

    // Demo Friends Leagues
    val demoLeagues: List<FriendsLeague> = listOf(
        FriendsLeague("lg_1", "Colleagues Cricket Cup", "17PCCC", "CricketChamp17", "India vs Australia", 6, 1, 1850, true),
        FriendsLeague("lg_2", "Weekend Warriors 17", "WW17KB", "AmitSharma_07", "India vs Australia", 4, 2, 1850, false)
    )
}
