package com.attackranges.weapons;

public class Dart extends Weapon {
	public Dart(int id) {
		super(id);
		range = 3;
	}

	public Dart(Integer[] weaponData) {
		super(weaponData);
	}
}
