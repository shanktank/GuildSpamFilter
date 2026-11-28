package com.GuildSpamFilter;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class MiscellaneousFiltersTest extends FilterTestBase
{
    private static final String BICEPS_DROP = "Biceps Btw received a drop: Abyssal whip (1,500,000 coins).";
    private static final String STORE_DROP = "Store Biceps received a drop: Abyssal whip (1,500,000 coins).";
    private static final String TANZANITE_DROP = "Biceps Btw received a drop: Tanzanite fang (4,000,000 coins).";
    private static final String TANZANITE_DROP_WORTH_40M = "Biceps Btw received a drop: Tanzanite fang (40,000,000 coins).";
    private static final String VORKATH_PERSONAL_BEST = "Biceps Btw has achieved a new Vorkath personal best: 1:05.40";

    // Player names to always include

    @Test
    public void alwaysIncludedPlayersAreNeverHidden()
    {
        when(config.filterRegularDrops()).thenReturn(true);
        setAlwaysIncludedPlayers("Biceps Btw");

        assertShown(BICEPS_DROP);
        assertHidden(STORE_DROP);
    }

    @Test
    public void alwaysIncludedPlayersAcceptSeveralNamesInAnyCase()
    {
        when(config.filterRegularDrops()).thenReturn(true);
        setAlwaysIncludedPlayers("biceps btw, STORE BICEPS");

        assertShown(BICEPS_DROP);
        assertShown(STORE_DROP);
    }

    @Test
    public void alwaysIncludedPlayersMatchNamesWithNonBreakingSpaces()
    {
        when(config.filterRegularDrops()).thenReturn(true);
        setAlwaysIncludedPlayers("Biceps Btw");

        // The game writes spaces in player names as non-breaking spaces
        assertShown("Biceps Btw received a drop: Abyssal whip (1,500,000 coins).");
    }

    @Test
    public void alwaysIncludedPlayersMatchBroadcastsWithAnIconInFront()
    {
        when(config.filterRegularDrops()).thenReturn(true);
        setAlwaysIncludedPlayers("Biceps Btw");

        assertShown("<img=41>Biceps Btw received a drop: Abyssal whip (1,500,000 coins).");
    }

    @Test
    public void alwaysIncludedPlayersMatchBroadcastsSentAsTemplates()
    {
        // Seen in RuneLite's debug log, November 2026: the game sets a value in front of the player's name
        when(config.filterXpMilestone()).thenReturn(true);
        when(config.filterQuestComplete()).thenReturn(true);
        when(config.filterPets()).thenReturn(true);
        setAlwaysIncludedPlayers("Playername1, Playername2, Playername3");

        assertShown("<int_0=52000000>Playername1 has reached <int_0:spacer> XP in Cooking.");
        assertShown("<img=56> <str_quest_name_0=Daddy's Home>Playername2 has completed a quest: <str_quest_name_0>");
        assertShown("<img=2> <int_p1=0>Playername3 feels something weird sneaking into <switch,int_p1,his,her,their> " +
                "backpack: <int_0=13>Skotos at <int_0:spacer> killcount from Skotizo.");
    }

    @Test
    public void alwaysIncludedPlayersMatchTheirOwnHighlightedName()
    {
        // Seen in RuneLite's debug log, November 2026: RuneLite's Chat Notifications plugin, with its "Highlight own
        // name" setting on, wraps the player's own name in tags
        when(config.filterRegularDrops()).thenReturn(true);
        setAlwaysIncludedPlayers("Playername2");

        assertShown("<img=2> <colHIGHLIGHT><u>Playername2</u><colNORMAL> received a drop: Yew seed<int_0=9826> " +
                "(<int_0:spacer> coins) from Hespori.");
        assertHidden("<img=2> Playername1 received a drop: Yew seed<int_0=9826> (<int_0:spacer> coins) from Hespori.");
    }

    @Test
    public void customFiltersSeeTemplatesAsTheChatBoxShowsThem()
    {
        setCustomFilters("14 x Runite ore (142,926 coins)");
        setCustomRegexFilters("^Playername1 received a drop: \\d+ x Rune halberd \\([\\d,]+ coins\\)\\.$\n" +
                "into their backpack: Skotos at 13 killcount");

        assertHidden("<img=2> Playername1 received a drop: <int_0=14><int_0:spacer> x Runite ore<int_0=142926> " +
                "(<int_0:spacer> coins) from Zalcano.");
        assertHidden("<img=2> Playername1 received a drop: <int_0=3><int_0:spacer> x Rune halberd<int_0=113133> " +
                "(<int_0:spacer> coins).");
        assertShown("<img=2> <int_p1=0>Playername1 feels something weird sneaking into <switch,int_p1,his,her,their> " +
                "backpack: <int_0=13>Skotos at <int_0:spacer> killcount from Skotizo.");
        assertHidden("<img=2> <int_p1=2>Playername1 feels something weird sneaking into <switch,int_p1,his,her,their> " +
                "backpack: <int_0=13>Skotos at <int_0:spacer> killcount from Skotizo.");
    }

    @Test
    public void templateValuesAreFilledInAsTheyAre()
    {
        // Made up: "$" and "\" would mean something to a regex replacement
        setCustomFilters("quest: cost $1 \\ more");

        assertHidden("<str_quest_name_0=Cost $1 \\ more>Playername1 has completed a quest: <str_quest_name_0>");
    }

    @Test
    public void alwaysIncludedPlayersAcceptAnySeparator()
    {
        when(config.filterRegularDrops()).thenReturn(true);

        for (String list : listsOf("Biceps Btw", "Store Biceps"))
        {
            setAlwaysIncludedPlayers(list);
            assertFalse("Always-included players " + describeList(list), isHidden(BICEPS_DROP) || isHidden(STORE_DROP));
        }
    }

    @Test
    public void alwaysIncludedPlayerDoesNotCoverLongerNames()
    {
        when(config.filterRegularDrops()).thenReturn(true);
        setAlwaysIncludedPlayers("Bob");

        assertHidden("Bobby received a drop: Abyssal whip (1,500,000 coins).");
    }

    // Custom filters

    @Test
    public void customFiltersHideBroadcastsContainingAnyEntry()
    {
        setCustomFilters("tanzanite, Vorkath");

        assertHidden("Biceps Btw received a drop: Tanzanite fang (4,000,000 coins).");
        assertHidden("Biceps Btw has achieved a new Vorkath personal best: 1:05.40");
        assertShown(BICEPS_DROP);
    }

    @Test
    public void customFiltersIgnoreCase()
    {
        setCustomFilters("ABYSSAL WHIP");

        assertHidden(BICEPS_DROP);
    }

    @Test
    public void customFiltersAcceptAnySeparator()
    {
        for (String list : listsOf("tanzanite", "Vorkath"))
        {
            setCustomFilters(list);
            assertTrue("Custom filters " + describeList(list), isHidden(TANZANITE_DROP) && isHidden(VORKATH_PERSONAL_BEST));
            assertShown(BICEPS_DROP);
        }
    }

    @Test
    public void customFiltersIgnoreSpacesAroundEntries()
    {
        setCustomFilters("    Chambers of Xeric, Armadyl Godsword");

        assertHidden("Biceps Btw has achieved a new Chambers of Xeric (Team Size: 3 players) personal best: 18:03");
        assertHidden("Biceps Btw received a drop: Armadyl godsword (15,000,000 coins).");
        assertShown(BICEPS_DROP);
    }

    @Test
    public void customFiltersMatchNamesWithNonBreakingSpaces()
    {
        setCustomFilters("Biceps Btw");

        // The game writes spaces in player names as non-breaking spaces
        assertHidden("Biceps Btw received a drop: Abyssal whip (1,500,000 coins).");
    }

    @Test
    public void alwaysIncludedPlayersWinOverCustomFilters()
    {
        setAlwaysIncludedPlayers("Biceps Btw");
        setCustomFilters("whip");

        assertShown(BICEPS_DROP);
        assertHidden(STORE_DROP);
    }

    // Custom regex filters

    @Test
    public void customRegexFiltersHideBroadcastsWithAMatch()
    {
        setCustomRegexFilters("has reached (Fishing|Cooking) level");

        assertHidden("Biceps Btw has reached Fishing level 90.");
        assertHidden("Biceps Btw has reached Cooking level 90.");
        assertShown("Biceps Btw has reached Mining level 90.");
    }

    @Test
    public void customRegexFiltersIgnoreCase()
    {
        setCustomRegexFilters("ABYSSAL WHIP");

        assertHidden(BICEPS_DROP);
    }

    @Test
    public void customRegexFiltersAreOnePerLineSoTheyCanContainCommas()
    {
        // Drops worth 1,000,000 to 9,999,999 coins, and every Vorkath personal best
        setCustomRegexFilters("\\(\\d,\\d{3},\\d{3} coins\\)\r\n\n    Vorkath personal best\t  \n");

        assertHidden(BICEPS_DROP);
        assertShown(TANZANITE_DROP_WORTH_40M);
        assertHidden(VORKATH_PERSONAL_BEST);
    }

    @Test
    public void customRegexFiltersSkipInvalidExpressions()
    {
        setCustomRegexFilters("(unclosed\nwhip");

        assertLoggedProblem("Ignoring custom regex filter \"(unclosed\"");
        assertHidden(BICEPS_DROP);
    }

    @Test
    public void customRegexFiltersMatchNamesWithNonBreakingSpaces()
    {
        setCustomRegexFilters("^Biceps Btw received");

        assertHidden("Biceps Btw received a drop: Abyssal whip (1,500,000 coins).");
    }

    @Test
    public void alwaysIncludedPlayersWinOverCustomRegexFilters()
    {
        setAlwaysIncludedPlayers("Biceps Btw");
        setCustomRegexFilters("whip");

        assertShown(BICEPS_DROP);
        assertHidden(STORE_DROP);
    }

    // Leagues

    @Test
    public void hidesLeaguesBroadcasts()
    {
        when(config.filterLeaguesBroadcasts()).thenReturn(true);

        assertHidden("<img=22>Biceps Btw has unlocked the Tier 3 relic.");
        assertShown(BICEPS_DROP);
    }
}
