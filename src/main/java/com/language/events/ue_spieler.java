package com.language.events;

import com.language.main.main;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class ue_spieler implements Listener
{
	/*	Angelegt am: 21.03.2022
	 * 	Erstellt von: Nihar
	 * 	Beschreibung:
	 * 	Alle Events im Zusammenhang mit dem �bersetzen.
	 * 	(Hier sollte nur das ChatEvent sein :D)
	 * 	
	 */
	
	@EventHandler
	public void ue_asyncPlayerChat4MyLanguageChat(AsyncPlayerChatEvent e) 
	{
		//	D�rfen wir �bersetzen?
		if(main.SETTINGS.of_isUsingLanguage())
		{
			e.setCancelled(true);
			
			String playerName = e.getPlayer().getName();
			String message = e.getMessage();
			boolean translate = false;
			
			//	Wenn kein Symbol gefordert wird...
			if(!main.SETTINGS.of_isUsingChatSymbole())
			{
				translate = true;
			}
			//	Symbol ist gefordert!
			else if(e.getMessage().startsWith(main.SETTINGS.of_getChatTranslateSymbole()))
			{
				translate = true;
				message = message.replaceFirst(main.SETTINGS.of_getChatTranslateSymbole(), "");
			}
			
			main.TRANSLATION.of_translateMessageAndSend2AllPlayers(playerName, message, translate);
			main.PLUGIN.getLogger().info(playerName + ": " + message);
		}
	}
	
	@EventHandler
	public void ue_joinPlayer4MyLanguageChat(PlayerJoinEvent e) 
	{
		//	Spieler zum System anmelden...
		main.SPIELERSERVICE.CONTEXT.of_loadPlayer(e.getPlayer());
	}
}
