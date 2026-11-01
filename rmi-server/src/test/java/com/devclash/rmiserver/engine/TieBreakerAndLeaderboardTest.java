package com.devclash.rmiserver.engine;

import com.devclash.common.dto.PlayerScoreDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TieBreakerAndLeaderboardTest {

    @Test
    @DisplayName("Tie-break test 1: Equal score broken by total correct answers")
    void testTieBreakByCorrectAnswers() {
        PlayerScoreDTO p1 = new PlayerScoreDTO("p1", "Alice", 1000, 1, 1);
        p1.setCorrectAnswers(3);
        p1.setCumulativeAnswerTimeMs(6000);

        PlayerScoreDTO p2 = new PlayerScoreDTO("p2", "Bob", 1000, 1, 1);
        p2.setCorrectAnswers(2);
        p2.setCumulativeAnswerTimeMs(4000); // Bob was faster, but Alice had more correct answers!

        List<PlayerScoreDTO> list = new ArrayList<>(List.of(p2, p1));
        Collections.sort(list);

        assertEquals("Alice", list.get(0).getNickname(), "Alice must rank #1 due to higher correct answers count");
        assertEquals("Bob", list.get(1).getNickname(), "Bob must rank #2");
    }

    @Test
    @DisplayName("Tie-break test 2: Equal score and equal correct answers broken by cumulative answer time")
    void testTieBreakByCumulativeTime() {
        PlayerScoreDTO p1 = new PlayerScoreDTO("p1", "Alice", 1000, 1, 1);
        p1.setCorrectAnswers(2);
        p1.setCumulativeAnswerTimeMs(5500);

        PlayerScoreDTO p2 = new PlayerScoreDTO("p2", "Bob", 1000, 1, 1);
        p2.setCorrectAnswers(2);
        p2.setCumulativeAnswerTimeMs(3200); // Bob is faster!

        List<PlayerScoreDTO> list = new ArrayList<>(List.of(p1, p2));
        Collections.sort(list);

        assertEquals("Bob", list.get(0).getNickname(), "Bob must rank #1 due to faster cumulative response time");
        assertEquals("Alice", list.get(1).getNickname(), "Alice must rank #2");
    }

    @Test
    @DisplayName("Rank movement calculation test")
    void testRankMovementCalculation() {
        PlayerScoreDTO p = new PlayerScoreDTO("p1", "Fighter", 500, 3, 1);
        p.setPreviousRank(3);
        int newRank = 1;
        p.setRank(newRank);
        p.setRankChange(p.getPreviousRank() - newRank);

        assertEquals(2, p.getRankChange(), "Moving from #3 to #1 must be a +2 rank change");

        // Player moving from #1 to #2
        PlayerScoreDTO pDown = new PlayerScoreDTO("p2", "Fighter2", 400, 1, 0);
        pDown.setPreviousRank(1);
        int newRankDown = 2;
        pDown.setRank(newRankDown);
        pDown.setRankChange(pDown.getPreviousRank() - newRankDown);

        assertEquals(-1, pDown.getRankChange(), "Moving from #1 to #2 must be a -1 rank change");
    }
}
