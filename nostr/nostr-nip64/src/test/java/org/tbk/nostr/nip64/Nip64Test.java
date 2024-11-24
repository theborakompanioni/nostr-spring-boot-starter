
package org.tbk.nostr.nip64;

import chess.format.pgn.Pgn;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class Nip64Test {
    @Test
    void testParse0Invalid0() {
        Pgn pgn = Nip64.parsePgn("invalid_pgn");

        assertThat(pgn.toString(), is(""));
    }

    @Test
    void testParse0Invalid1() {
        Exception e = Assertions.assertThrows(IllegalStateException.class, () -> Nip64.parsePgn("["));

        assertThat(e.getMessage(), is("Could not parse pgn."));
    }

    @Test
    void testParse0Invalid2() {
        Pgn pgn = Nip64.parsePgn("""
                [Result "1/2-1/2"]
                                
                 *""");

        assertThat(pgn.toString(), is("""
                [Result "1/2-1/2"]
                                
                 1/2-1/2"""));
    }

    /**
     * From <a href="https://github.com/mliebelt/pgn-spec-commented/blob/main/pgn-specification.md#8-parsing-games">PGN Specification: Parsing Games</a>:
     * > A PGN database file is a sequential collection of zero or more PGN games. An empty file is a valid, although somewhat uninformative, PGN database.
     */
    @Test
    void testParse0Empty() {
        Pgn pgn = Nip64.parsePgn("");

        assertThat(pgn.toString(), is(""));
    }

    @Test
    void testParse0() {
        Pgn pgn0 = Nip64.parsePgn("*");

        assertThat(pgn0.toString(), is("""
                [Result "*"]
                                
                 *"""));

        Pgn pgn1 = Nip64.parsePgn("""
                [Result "*"]
                """);

        assertThat(pgn1.toString(), is("""
                [Result "*"]
                                
                 *"""));
    }

    @Test
    void testParse1() {
        Pgn pgn = Nip64.parsePgn("1. e4 *");

        assertThat(pgn.toString(), is("""
                [Result "*"]
                                
                1. e4 *"""));
    }

    @Test
    void testParse1WithoutResult() {
        Pgn pgn = Nip64.parsePgn("1. e4");

        assertThat(pgn.toString(), is("1. e4"));
    }

    @Test
    void testParse2() {
        Pgn pgn = Nip64.parsePgn("""
                [White "Fischer, Robert J."]
                [Black "Spassky, Boris V."]
                                
                1. e4 e5 2. Nf3 Nc6 3. Bb5 {This opening is called the Ruy Lopez.} *
                """);

        assertThat(pgn.toString(), is("""
                [White "Fischer, Robert J."]
                [Black "Spassky, Boris V."]
                [Result "*"]
                                
                1. e4 e5 2. Nf3 Nc6 3. Bb5 { This opening is called the Ruy Lopez. } *"""));
    }

    @Test
    void testParse3() {
        Pgn pgn = Nip64.parsePgn("""
                [Event "F/S Return Match"]
                [Site "Belgrade, Serbia JUG"]
                [Date "1992.11.04"]
                [Round "29"]
                [White "Fischer, Robert J."]
                [Black "Spassky, Boris V."]
                [Result "1/2-1/2"]
                                
                1. e4 e5 2. Nf3 Nc6 3. Bb5 {This opening is called the Ruy Lopez.} 3... a6
                4. Ba4 Nf6 5. O-O Be7 6. Re1 b5 7. Bb3 d6 8. c3 O-O 9. h3 Nb8 10. d4 Nbd7
                11. c4 c6 12. cxb5 axb5 13. Nc3 Bb7 14. Bg5 b4 15. Nb1 h6 16. Bh4 c5 17. dxe5
                Nxe4 18. Bxe7 Qxe7 19. exd6 Qf6 20. Nbd2 Nxd6 21. Nc4 Nxc4 22. Bxc4 Nb6
                23. Ne5 Rae8 24. Bxf7+ Rxf7 25. Nxf7 Rxe1+ 26. Qxe1 Kxf7 27. Qe3 Qg5 28. Qxg5
                hxg5 29. b3 Ke6 30. a3 Kd6 31. axb4 cxb4 32. Ra5 Nd5 33. f3 Bc8 34. Kf2 Bf5
                35. Ra7 g6 36. Ra6+ Kc5 37. Ke1 Nf4 38. g3 Nxh3 39. Kd2 Kb5 40. Rd6 Kc5 41. Ra6
                Nf2 42. g4 Bd3 43. Re6 1/2-1/2
                """);

        assertThat(pgn.toString(), is("""
                [Event "F/S Return Match"]
                [Site "Belgrade, Serbia JUG"]
                [Date "1992.11.04"]
                [Round "29"]
                [White "Fischer, Robert J."]
                [Black "Spassky, Boris V."]
                [Result "1/2-1/2"]
                                
                1. e4 e5 2. Nf3 Nc6 3. Bb5 { This opening is called the Ruy Lopez. } 3... a6 4. Ba4 Nf6 5. O-O Be7 6. Re1 b5 7. Bb3 d6 8. c3 O-O 9. h3 Nb8 10. d4 Nbd7 11. c4 c6 12. cxb5 axb5 13. Nc3 Bb7 14. Bg5 b4 15. Nb1 h6 16. Bh4 c5 17. dxe5 Nxe4 18. Bxe7 Qxe7 19. exd6 Qf6 20. Nbd2 Nxd6 21. Nc4 Nxc4 22. Bxc4 Nb6 23. Ne5 Rae8 24. Bxf7+ Rxf7 25. Nxf7 Rxe1+ 26. Qxe1 Kxf7 27. Qe3 Qg5 28. Qxg5 hxg5 29. b3 Ke6 30. a3 Kd6 31. axb4 cxb4 32. Ra5 Nd5 33. f3 Bc8 34. Kf2 Bf5 35. Ra7 g6 36. Ra6+ Kc5 37. Ke1 Nf4 38. g3 Nxh3 39. Kd2 Kb5 40. Rd6 Kc5 41. Ra6 Nf2 42. g4 Bd3 43. Re6 1/2-1/2"""));
    }

    @Test
    void testParse4CanCurrentlyOnlyParseSingleGames() {
        Pgn pgn = Nip64.parsePgn("""
                [Event "Hourly HyperBullet Arena"]
                [Site "https://lichess.org/wxx4GldJ"]
                [Date "2017.04.01"]
                [White "T_LUKE"]
                [Black "decidement"]
                [Result "1-0"]
                [UTCDate "2017.04.01"]
                [UTCTime "11:56:14"]
                [WhiteElo "2047"]
                [BlackElo "1984"]
                [WhiteRatingDiff "+10"]
                [BlackRatingDiff "-7"]
                [Variant "Standard"]
                [TimeControl "30+0"]
                [ECO "B00"]
                [Termination "Abandoned"]
                                
                1. e4 1-0
                                
                                
                [Event "Hourly HyperBullet Arena"]
                [Site "https://lichess.org/rospUdSk"]
                [Date "2017.04.01"]
                [White "Bastel"]
                [Black "oslochess"]
                [Result "1-0"]
                [UTCDate "2017.04.01"]
                [UTCTime "11:55:56"]
                [WhiteElo "2212"]
                [BlackElo "2000"]
                [WhiteRatingDiff "+6"]
                [BlackRatingDiff "-4"]
                [Variant "Standard"]
                [TimeControl "30+0"]
                [ECO "A01"]
                [Termination "Normal"]
                                
                1. b3 d5 2. Bb2 c6 3. Nc3 Bf5 4. d4 Nf6 5. e3 Nbd7 6. f4 Bg6 7. Nf3 Bh5 8. Bd3 e6 9. O-O Be7 10. Qe1 O-O 11. Ne5 Bg6 12. Nxg6 hxg6 13. e4 dxe4 14. Nxe4 Nxe4 15. Bxe4 Nf6 16. c4 Bd6 17. Bc2 Qc7 18. f5 Be7 19. fxe6 fxe6 20. Qxe6+ Kh8 21. Qh3+ Kg8 22. Bxg6 Qd7 23. Qe3 Bd6 24. Bf5 Qe7 25. Be6+ Kh8 26. Qh3+ Nh7 27. Bf5 Rf6 28. Qxh7# 1-0
                """);

        assertThat(pgn.toString(), is("""
                [Event "Hourly HyperBullet Arena"]
                [Site "https://lichess.org/wxx4GldJ"]
                [Date "2017.04.01"]
                [White "T_LUKE"]
                [Black "decidement"]
                [Result "1-0"]
                [UTCDate "2017.04.01"]
                [UTCTime "11:56:14"]
                [WhiteElo "2047"]
                [BlackElo "1984"]
                [WhiteRatingDiff "+10"]
                [BlackRatingDiff "-7"]
                [Variant "Standard"]
                [TimeControl "30+0"]
                [ECO "B00"]
                [Termination "Abandoned"]
                                
                1. e4 1-0"""));
    }
}