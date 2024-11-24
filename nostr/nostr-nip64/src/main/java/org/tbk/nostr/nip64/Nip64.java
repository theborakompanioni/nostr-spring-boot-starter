package org.tbk.nostr.nip64;

import chess.format.pgn.ParsedPgn;
import chess.format.pgn.Parser;
import chess.format.pgn.Pgn;
import com.google.common.annotations.VisibleForTesting;
import scala.util.Either;

import java.util.Optional;

public final class Nip64 {
    private Nip64() {
        throw new UnsupportedOperationException();
    }

    @VisibleForTesting
    static Pgn parsePgn(String pgn) {
        Either<String, ParsedPgn> result = Parser.full(pgn);
        ParsedPgn parsedPgn = result.getOrElse(() -> {
            throw new IllegalStateException("Could not parse pgn.");
        });
        return parsedPgn.toPgn();
    }

    @VisibleForTesting
    static Optional<Pgn> tryParsePgn(String pgn) {
        try {
            return Optional.of(parsePgn(pgn));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
