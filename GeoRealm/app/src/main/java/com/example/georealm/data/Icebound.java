package com.example.georealm.data;

import android.os.Parcel;
import android.os.Parcelable;

import static com.example.georealm.helper.Constants.ICEBOUND;
import static com.example.georealm.helper.Constants.SORCERER;

public class Icebound extends CharacterData {

    protected Icebound () {
        super();


    }

    public Icebound (String name) {
        super(name, SORCERER, ICEBOUND, 95, 120, 25, 50, 70, 55);


    }

    protected Icebound (Parcel source) {
        super(source);


    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);


    }

    @Override
    public void levelUp() {

        health = health + (int)(health * 0.05);// 5%
        resource = resource + (int)(resource * 0.05);// 5%
        attack_damage = attack_damage + (int)(attack_damage * 0.05);// 5%
        defence = defence + (int)(defence * 0.05);// 5%
        magic_damage = magic_damage + (int)(magic_damage * 0.05);// 5%
        magic_resistance = magic_resistance + (int)(magic_resistance * 0.05);// 5%
        character_level = character_level + 1;
        leveling_points = leveling_points + (character_level % 2);
    }

    @Override
    public int describeContents() {

        return 0;
    }

    public static final Parcelable.Creator<Icebound> CREATOR = new Parcelable.Creator<Icebound>() {

        @Override
        public Icebound createFromParcel(Parcel source) {

            return new Icebound(source);
        }

        @Override
        public Icebound[] newArray(int size) {

            return new Icebound[0];
        }
    };
}
