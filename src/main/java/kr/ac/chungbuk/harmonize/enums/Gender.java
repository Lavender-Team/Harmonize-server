package kr.ac.chungbuk.harmonize.enums;

import java.util.HashMap;
import java.util.Map;

public enum Gender {
    MALE,       // 남성
    FEMALE,     // 여성
    ETC;        // 그외


    private static final Map<String, Gender> values = new HashMap<>();
    static {
        for (Gender g : Gender.values()) {
            if (values.put(g.name(), g) != null) {
                throw new IllegalArgumentException("duplicate value: " + g.name());
            }
        }
    }

    public static Gender fromString(String value) throws IllegalArgumentException {
        Gender genre = values.get(value);
        if (values.get(value) == null)
            throw new IllegalArgumentException("invalid genre string");
        else
            return genre;
    }

    public static String toString(Gender gender) {
        if (gender == MALE) return "남성";
        if (gender == FEMALE) return "여성";
        if (gender == ETC) return "기타";
        return "";
    }
}
