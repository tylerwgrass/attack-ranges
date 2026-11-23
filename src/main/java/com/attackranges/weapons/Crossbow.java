package com.attackranges.weapons;

public class Crossbow extends Weapon {
	public Crossbow(int id) {
		super(id);
		range = 7;
	}

	public Crossbow(Integer[] weaponData) {
		super(weaponData);
	}
}
