package com.attackranges;

import java.util.ArrayList;
import java.util.List;
import net.runelite.api.Client;

public class ColosseumHandler
{
	private static final int COLOSSEUM_MODIFIER_SELECTION_INIT_SCRIPT_ID = 4931;

	private static final int MYOPIA_MODIFIER_SCRIPT_ID = 11;
	private static final int MYOPIA_LEVEL_VARBIT_ID = 9795;
	private static final int MODIFIER_SELECTED_VARBIT_ID = 9788;
	private static List<Integer> SELECTABLE_MODIFIERS = new ArrayList<>();

	public static boolean isSelectModifierScript(int scriptId)
	{
		return scriptId == COLOSSEUM_MODIFIER_SELECTION_INIT_SCRIPT_ID;
	}

	public static void setNextWaveModifierOptions(Object[] args)
	{
		SELECTABLE_MODIFIERS = List.of(
			(Integer) args[2],
			(Integer) args[3],
			(Integer) args[4]
		);
	}

	public static int getMyopiaRangeDeduction(Client client)
	{
		if (SELECTABLE_MODIFIERS.isEmpty())
		{
			return 0;
		}

		int selectedModifierIndex = client.getVarbitValue(MODIFIER_SELECTED_VARBIT_ID);
		if (selectedModifierIndex == 0)
		{
			return 0;
		}

		Integer selectedModifierId = SELECTABLE_MODIFIERS.get(selectedModifierIndex - 1);
		if (selectedModifierId != MYOPIA_MODIFIER_SCRIPT_ID)
		{
			return 0;
		}

		return getMyopiaLevel(client) * 2;
	}

	private static int getMyopiaLevel(Client client)
	{
		try
		{
			return client.getVarbitValue(MYOPIA_LEVEL_VARBIT_ID);
		}
		catch (Exception e)
		{
			return 1;
		}
	}
}
