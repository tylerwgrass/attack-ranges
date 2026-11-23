package com.attackranges.weapons;

public class Melee extends Weapon
{
	public Melee(Integer[] weaponData) { super(weaponData); }

	public Integer getSpecialAttackRange() { return this.getRange() + this.getLongRangeModifier(); }
}
