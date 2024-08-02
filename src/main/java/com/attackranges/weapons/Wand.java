package com.attackranges.weapons;

public class Wand extends Weapon
{
	public Wand(int id)
	{
		super(id);
		range = 10;
	}

	@Override
	public int getRange(String attackStyle)
	{
		// Ignore if user is not casting, wands always have a cast range of 10
		return attackStyle.equals("Casting") ? range : 0;
	}
}
