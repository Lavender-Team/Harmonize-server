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

        values.put("가요", KPOP);
        values.put("팝송", POP);
        values.put("발라드", BALLADE);
        values.put("랩/힙합", RAP);
        values.put("댄스", DANCE);
        values.put("일본곡", JPOP);
        values.put("R&B", RNB);
        values.put("포크/블루스", FOLK);
        values.put("록/메탈", ROCK);
        values.put("OST", OST);
        values.put("인디음악", INDIE);
        values.put("인디뮤직", INDIE);
        values.put("트로트", TROT);
        values.put("어린이곡", KID);
    }

    private static final Map<Genre, String> names = new HashMap<>();
    static {
        names.put(KPOP, "가요");
        names.put(POP, "팝송");
        names.put(BALLADE, "발라드");
        names.put(RAP, "랩/힙합");
        names.put(DANCE, "댄스");
        names.put(JPOP, "일본곡");
        names.put(RNB, "R&B");
        names.put(FOLK, "포크/블루스");
        names.put(ROCK, "록/메탈");
        names.put(OST, "OST");
        names.put(INDIE, "인디뮤직");
        names.put(TROT, "트로트");
        names.put(KID, "어린이곡");
    }

    public static Genre fromString(String value) throws IllegalArgumentException {
        Genre genre = values.get(value);
        if (values.get(value) == null)
            throw new IllegalArgumentException("invalid genre string");
        else
            return genre;
    }

    public static String toString(Genre genre) {
        return names.get(genre);
    }
}
