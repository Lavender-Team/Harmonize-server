package kr.ac.chungbuk.harmonize.enums;

import java.util.HashMap;
import java.util.Map;

public enum Gender {
    MALE,       // 남성
    FEMALE,     // 여성
    OTHER;      // 그외

    private static final Map<String, Gender> values = new HashMap<>();
    static {
        for (Gender g : Gender.values()) {
            if (values.put(g.name(), g) != null) {
                throw new IllegalArgumentException("duplicate value: " + g.name());
            }
        }
    }

    // 입력된 문자열을 대문자로 변환한 후 Enum 값을 반환
    public static Gender fromString(String value) throws IllegalArgumentException {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("invalid gender string");
        }

        // 입력값을 대문자로 변환
        value = value.toUpperCase();

        Gender gender = values.get(value);
        if (gender == null) {
            throw new IllegalArgumentException("invalid gender string");
        }
        return gender;
    }

    // Enum 값을 문자열로 변환
    public static String toString(Gender gender) {
        if (gender == MALE) return "남성";
        if (gender == FEMALE) return "여성";
        if (gender == OTHER) return "기타";
        return "";
    }
}
