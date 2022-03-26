package com.language.spieler;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import com.language.ancestor.Objekt;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

/**
 * @Created 21.03.2022
 * @Author Nihar
 * @Description
 * This class contains useful methods or functions
 * in reference to the object: Spieler.
 *
 */
public class SpielerService extends Objekt
{
	//	Attribute
	public SpielerContext CONTEXT;

	/* ************************************* */
	/* CONSTRUCTOR */
	/* ************************************* */
	
	public SpielerService() { }

	/* ************************************* */
	/* LOADER */
	/* ************************************* */
	
	@Override
	public int of_load() 
	{
		CONTEXT = new SpielerContext();
		
		for(Player p : Bukkit.getOnlinePlayers()) 
		{
			CONTEXT.of_loadPlayer(p);
		}
		
		return 1;
	}

	/* ************************************* */
	/* OBJEKT-ANWEISUNGEN */
	/* ************************************* */

	/**
	 * This function changes the language of a player.
	 * @param ps Player instance.
	 * @param swapLanguage Country code of the language for example: 'EN'
	 */
	public void of_swapLanguage(Spieler ps, String swapLanguage)
	{
		int rc = -1;
		
		if(ps != null) 
		{
			ps.of_setDefaultLanguage(swapLanguage);
			rc = CONTEXT.of_savePlayer(ps);
		}

	}

	/**
	 * This function sends an interactive-chat message to the player.
	 * @param p Player instance.
	 * @param Chattext The text which will be displayed in the chat.
	 * @param Hovertext The text which will be displayed if the cursor hovers over it.
	 * @param CMD The command which will be executed by the player if the user clicks on the message.
	 */
	public void of_sendInteractiveMessage(Player p, String Chattext, String Hovertext, String CMD)
	{
		//	Interaktive Chat-Nachricht!
		TextComponent tc = new TextComponent();
		tc.setText(Chattext);
		tc.setBold(true);
		tc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + CMD));
		tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(Hovertext).create()));
		p.spigot().sendMessage(tc);
	}
}
