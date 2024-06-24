package kr.ac.chungbuk.harmonize.enums;

import java.util.HashMap;
import java.util.Map;

public enum Genre {
    KPOP,       // 가요
    POP,        // 팝송
    BALLADE,    // 발라드
    RAP,        // 랩/힙합
    DANCE,      // 댄스
    JPOP,       // 일본곡
    RNB,        // R&B
    FOLK,       // 포크/블루스
    ROCK,       // 록/메탈
    OST,        // OST
    INDIE,      // 인디뮤직
    TROT,       // 트로트
    KID;         // 어린이곡


    private static final Map<String, Genre> values = new HashMap<>();
    static {
        for (Genre g : Genre.values()) {
            if (values.put(g.name(), g) != null) {
                throw new IllegalArgumentException("duplicate value: " + g.name());
            }
        }
    }

    public static Genre fromString(String value) throws IllegalArgumentException {
        Genre genre = values.get(value);
        if (values.get(value) == null)
            throw new IllegalArgumentException("invalid genre string");
        else
            return genre;
    }
}
