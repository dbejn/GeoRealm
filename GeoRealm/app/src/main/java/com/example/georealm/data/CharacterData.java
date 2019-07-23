package com.example.georealm.data;

import android.graphics.Color;

public class CharacterData {

    private String character_name;
    private String character_class;
    private String character_subclass;
    private int character_level;

    public CharacterData() {


    }

    public CharacterData(String name, String classs, String subclass, int lvl) {

        character_name = name;
        character_class = classs;
        character_subclass = subclass;
        character_level = lvl;
    }

    public String getCharacter_name() {
        return character_name;
    }

    public String getCharacter_class() {
        return character_class;
    }

    public String getCharacter_subclass() {
        return character_subclass;
    }

    public int getCharacter_level() {
        return character_level;
    }
}
