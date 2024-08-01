package com.attackranges.weapons;

public class Staff extends Weapon {
    public Staff(int id) {
        super(id);
        range = 10;
    }

    @Override
    public int getRange(String attackStyle) {
        // Ignore if user is not casting, staves always have a cast range of 10
        return attackStyle.equals("Casting") ? range : 0;
    }
}
