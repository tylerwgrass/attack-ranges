package com.attackranges.weapons;

public class PoweredStaff extends Weapon
{
	public PoweredStaff(int id)
	{
		super(id);
		range = 7;
	}

	public PoweredStaff(Integer[] weaponData)
	{
		super(weaponData);
	}


	public int getRange(String attackStyle)
	{
		return attackStyle.equals("Defensive") ? range + longRangeModifier : range;
	}
}
