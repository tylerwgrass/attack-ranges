package com.attackranges.weapons;

import org.apache.commons.lang3.tuple.Triple;

import java.util.List;

public class Crossbow extends Weapon {
    public Crossbow(int id) {
        super(id);
        range = 7;
    }

    public Crossbow(List<Integer> weaponData) {
        super(weaponData);
    }
}
