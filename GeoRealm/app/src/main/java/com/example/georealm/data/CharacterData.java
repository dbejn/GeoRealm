package com.example.georealm.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public abstract class CharacterData implements Parcelable {

    protected String character_name;
    protected int character_class;
    protected int character_subclass;
    protected int character_level;
    protected int leveling_points;
    protected int gems_collected;
    protected int gems_spent;
    protected int monsters_slain;
    protected int duels_fought;
    protected int duels_won;
    protected int experience;
    protected int score;
    protected int health;
    protected int resource;
    protected int attack_damage;
    protected int defence;
    protected int magic_damage;
    protected int magic_resistance;
    protected List<Integer> skills;
    protected List<Integer> items;

    protected CharacterData () {


    }

    protected CharacterData (String name, int classs, int subclass,
                          int hp, int res, int atk, int def, int mag, int m_res) {

        character_name = name;
        character_class = classs;
        character_subclass = subclass;
        character_level = 1;
        leveling_points = 1;
        gems_collected = 0;
        gems_spent = 0;
        monsters_slain = 0;
        duels_fought = 0;
        duels_won = 0;
        experience = 0;
        score = 0;
        health = hp;
        resource = res;
        attack_damage = atk;
        defence = def;
        magic_damage = mag;
        magic_resistance = m_res;
        skills = new ArrayList<>();
        items = new ArrayList<>();
        items.add(0, 0);// health pots
        items.add(1, 0);// mana pots
        items.add(2, 0);// stamina pots
    }

    protected CharacterData (Parcel source) {

        character_name = source.readString();
        character_class = source.readInt();
        character_subclass = source.readInt();
        character_level = source.readInt();
        leveling_points = source.readInt();
        gems_collected = source.readInt();
        gems_spent = source.readInt();
        monsters_slain = source.readInt();
        duels_fought = source.readInt();
        duels_won = source.readInt();
        experience = source.readInt();
        score = source.readInt();
        health = source.readInt();
        resource = source.readInt();
        attack_damage = source.readInt();
        defence = source.readInt();
        magic_damage = source.readInt();
        magic_resistance = source.readInt();
        skills = source.readArrayList(null);
        items = source.readArrayList(null);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeString(character_name);
        dest.writeInt(character_class);
        dest.writeInt(character_subclass);
        dest.writeInt(character_level);
        dest.writeInt(leveling_points);
        dest.writeInt(gems_collected);
        dest.writeInt(gems_spent);
        dest.writeInt(monsters_slain);
        dest.writeInt(duels_fought);
        dest.writeInt(duels_won);
        dest.writeInt(experience);
        dest.writeInt(score);
        dest.writeInt(health);
        dest.writeInt(resource);
        dest.writeInt(attack_damage);
        dest.writeInt(defence);
        dest.writeInt(magic_damage);
        dest.writeInt(magic_resistance);
        dest.writeList(skills);
        dest.writeList(items);
    }

    abstract public void levelUp ();

    public String getCharacter_name() {
        return character_name;
    }

    public void setCharacter_name(String character_name) {
        this.character_name = character_name;
    }

    public int getCharacter_class() {
        return character_class;
    }

    public void setCharacter_class(int character_class) {
        this.character_class = character_class;
    }

    public int getCharacter_subclass() {
        return character_subclass;
    }

    public void setCharacter_subclass(int character_subclass) {
        this.character_subclass = character_subclass;
    }

    public int getCharacter_level() {
        return character_level;
    }

    public void setCharacter_level(int character_level) {
        this.character_level = character_level;
    }

    public int getLeveling_points() {
        return leveling_points;
    }

    public void setLeveling_points(int leveling_points) {
        this.leveling_points = leveling_points;
    }

    public int getGems_collected() {
        return gems_collected;
    }

    public void setGems_collected(int gems_collected) {
        this.gems_collected = gems_collected;
    }

    public int getGems_spent() {
        return gems_spent;
    }

    public void setGems_spent(int gems_spent) {
        this.gems_spent = gems_spent;
    }

    public int getMonsters_slain() {
        return monsters_slain;
    }

    public void setMonsters_slain(int monsters_slain) {
        this.monsters_slain = monsters_slain;
    }

    public int getDuels_fought() {
        return duels_fought;
    }

    public void setDuels_fought(int duels_fought) {
        this.duels_fought = duels_fought;
    }

    public int getDuels_won() {
        return duels_won;
    }

    public void setDuels_won(int duels_won) {
        this.duels_won = duels_won;
    }

    public int getExperience() {
        return experience;
    }

    public void setExperience(int experience) {
        this.experience = experience;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public int getResource() {
        return resource;
    }

    public void setResource(int resource) {
        this.resource = resource;
    }

    public int getAttack_damage() {
        return attack_damage;
    }

    public void setAttack_damage(int attack_damage) {
        this.attack_damage = attack_damage;
    }

    public int getDefence() {
        return defence;
    }

    public void setDefence(int defence) {
        this.defence = defence;
    }

    public int getMagic_damage() {
        return magic_damage;
    }

    public void setMagic_damage(int magic_damage) {
        this.magic_damage = magic_damage;
    }

    public int getMagic_resistance() {
        return magic_resistance;
    }

    public void setMagic_resistance(int magic_resistance) {
        this.magic_resistance = magic_resistance;
    }

    public List<Integer> getSkills() {
        return skills;
    }

    public void setSkills(List<Integer> skills) {
        this.skills = skills;
    }

    public List<Integer> getItems() {
        return items;
    }

    public void setItems(List<Integer> items) {
        this.items = items;
    }
}
