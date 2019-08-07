package com.example.georealm.helper;

public class Constants {

    // CLASSES & SUBCLASSES
    public static final int SWORDSMAN = 0;
    public static final int BERSERKER = 1;
    public static final int PALADIN = 2;

    public static final int SORCERER = 3;
    public static final int PYROMANCER = 4;
    public static final int ICEBOUND = 5;

    public static final int ROGUE = 6;
    public static final int ASSASSIN = 7;
    public static final int SHADOW = 8;

    // CONSTANTS
    public static final float DEFAULT_ZOOM = 16.0f;
    public static final float MIN_ZOOM = 14.5f;
    public static final String CREATE_MARKERS_URL = "https://us-central1-georealm-elfak.cloudfunctions.net/createMarkers";

    // ICEBOUND SKILLS
    public static final String[] ICEBOUND_SKILL_NAMES = {
            "skill 0",
            "skill 1",
            "skill 2",
            "skill 3",
            "skill 4",
            "skill 5",
            "skill 6"
    };
    public static final String[] ICEBOUND_SKILL_DESCRIPTIONS = {
            "description 0",
            "description 1",
            "description 2",
            "description 3",
            "description 4",
            "description 5",
            "description 6"
    };
    public static final String[] ICEBOUND_SKILL_STATS = {
            "stat 00\nstat01",
            "stat 10\nstat11\nstat12",
            "stat 20\nstat21",
            "stat 30\nstat31\nstat32",
            "stat 40\nstat41\nstat42",
            "stat 50",
            "stat 60\nstat61",
    };
}
