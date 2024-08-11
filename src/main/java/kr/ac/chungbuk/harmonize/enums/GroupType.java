package kr.ac.chungbuk.harmonize.enums;

import java.util.HashMap;
import java.util.Map;

public enum GroupType {
    SOLO,
    GROUP;

    private static final Map<String, GroupType> values = new HashMap<>();
    static {
        for (GroupType g : GroupType.values()) {
            if (values.put(g.name(), g) != null) {
                throw new IllegalArgumentException("duplicate value: " + g.name());
            }
        }
    }

    public static GroupType fromString(String value) throws IllegalArgumentException {
        GroupType groupType = values.get(value);
        if (values.get(value) == null)
            throw new IllegalArgumentException("invalid genre string");
        else
            return groupType;
    }

    public static String toString(GroupType groupType) {
        if (groupType == SOLO) return "솔로";
        if (groupType == GROUP) return "그룹";
        return "";
    }
}
