package com.example.georealm.data;

import android.graphics.Color;

public class CharacterCardData {

    private String character_name;
    private int character_class;
    private int character_subclass;
    private int character_level;

    public CharacterCardData() {


    }

    public CharacterCardData(String name, int classs, int subclass) {

        character_name = name;
        character_class = classs;
        character_subclass = subclass;
        character_level = 1;
    }

    public String getCharacter_name() {
        return character_name;
    }

    public int getCharacter_class() {
        return character_class;
    }

    public int getCharacter_subclass() {
        return character_subclass;
    }

    public int getCharacter_level() {
        return character_level;
    }
}
