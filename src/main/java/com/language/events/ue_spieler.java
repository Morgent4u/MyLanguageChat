package com.language.events;

import com.language.main.main;
import com.language.spieler.Spieler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * @Created 21.03.2022
 * @Author Niha
 * @Description
 * This class contains all events which are necessary for the player
 * and this plugin.
 */
public class ue_spieler implements Listener
{
	/**
	 * The AsyncPlayerChatEvent is used to interact with the player messages.
	 * @param event Event instance.
	 */
	@EventHandler
	public void ue_asyncPlayerChat4MyLanguageChat(AsyncPlayerChatEvent event)
	{
		//	Dürfen wir uebersetzen?
		if(main.SETTINGS.of_isUsingLanguage())
		{
			event.setCancelled(true);
			
			String playerName = event.getPlayer().getName();
			String message = event.getMessage();
			boolean translate = false;

			//	Wenn kein Symbol gefordert wird...
			if(!main.SETTINGS.of_isUsingChatSymbol())
			{
				translate = true;
			}

			//	Symbol ist gefordert!
			else if(event.getMessage().startsWith(main.SETTINGS.of_getChatTranslateSymbol()))
			{
				translate = true;
				message = message.replaceFirst(main.SETTINGS.of_getChatTranslateSymbol(), "");
			}
			
			main.TRANSLATION.of_translateMessageAndSend2AllPlayers(playerName, message, translate);
			main.PLUGIN.getLogger().info(playerName + ": " + message);
		}
	}

	/**
	 * This event is used to interact with the player while connecting to the server.
	 * @param event Event instance.
	 */
	@EventHandler
	public void ue_joinPlayer4MyLanguageChat(PlayerJoinEvent event)
	{
		int rc = main.SPIELERSERVICE.CONTEXT.of_loadPlayer(event.getPlayer());

		//	If the player is new and we can check the player settings language... lets check it :)!
		if(rc == 0 && main.SETTINGS.of_isUsingAutoSelectLanguage())
		{
			//	Run this task later because we need some seconds to identify the player settings language.
			new BukkitRunnable()
			{

				@Override
				public void run()
				{
					Spieler ps = main.SPIELERSERVICE.CONTEXT.of_getSpieler(event.getPlayer().getName());

					if(ps != null)
					{
						main.SPIELERSERVICE.of_swapLanguage(ps, main.SPIELERSERVICE.of_getPlayerSettingsLanguageByPlayer(ps));
					}
				}

			}.runTaskLater(main.PLUGIN, 20*3);
		}
	}
}
