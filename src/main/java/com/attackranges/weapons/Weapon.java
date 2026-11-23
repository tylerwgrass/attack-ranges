package com.attackranges.weapons;

import lombok.Data;

@Data
public class Weapon {
	int id;
	int range;
	int longRangeModifier = 2;

	public Weapon(int id) {
		this.id = id;
	}

	public Weapon(Integer[] weaponData) {
		this.id = weaponData[0];
		this.range = weaponData[1];
		if (weaponData.length == 3) {
			this.longRangeModifier = weaponData[2];
		}
	}

	public int getRange(String attackStyle) {
		return attackStyle.equals("Longrange") ? range + longRangeModifier : range;
	}
}