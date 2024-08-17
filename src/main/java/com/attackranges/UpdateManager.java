package com.attackranges;

import com.google.inject.Inject;
import net.runelite.api.ChatMessageType;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;

public class UpdateManager
{
	@Inject
	private ChatMessageManager chatMessageManager;

	@Inject
	private ConfigManager configManager;

	private final String LAST_VERSION_SEEN_CONFIG_KEY = "last-version";
	private final String UPDATE_TEXT = "Attack Ranges has been updated to 1.2. Now supports drag protection and hotkey mode.";
	private final String LATEST_VERSION = "1.2";

	public void sendUpdateMessage()
	{
		if (hasLatestVersion())
		{
			return;
		}

		configManager.setConfiguration(AttackRangesConfig.ATTACK_RANGES_GROUP, LAST_VERSION_SEEN_CONFIG_KEY, LATEST_VERSION);

		chatMessageManager.queue(QueuedMessage.builder()
			.type(ChatMessageType.GAMEMESSAGE)
			.runeLiteFormattedMessage(UPDATE_TEXT)
			.build());
	}

	public boolean hasLatestVersion()
	{
		String lastVersionChecked = configManager.getConfiguration(AttackRangesConfig.ATTACK_RANGES_GROUP, LAST_VERSION_SEEN_CONFIG_KEY);
		return lastVersionChecked != null && lastVersionChecked.equals(LATEST_VERSION);
	}
}
