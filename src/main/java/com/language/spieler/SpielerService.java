package com.language.spieler;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import com.language.ancestor.Objekt;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class SpielerService extends Objekt
{
	/*	Angelegt am: 21.03.2022
	 * 	Erstellt von: Nihar
	 * 	Beschreibung:
	 * 	Zus�tzliche Methoden/Funktionen im Zusammenhang mit
	 * 	dem Spieler.
	 * 	
	 */
	
	//	Attribute
	public SpielerContext CONTEXT;
	
	/***************************************/
	/* CONSTRUCTOR */
	/***************************************/
	
	public SpielerService() { }
	
	/***************************************/
	/* LOADER */
	/***************************************/
	
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
	
	/***************************************/
	/* OBJEKT-ANWEISUNGEN */
	/***************************************/
	
	//	F�r das Wechseln der Sprache.
	public int of_swapLanguage(Spieler ps, String swapLanguage) 
	{
		int rc = -1;
		
		if(ps != null) 
		{
			ps.of_setDefaultLanguage(swapLanguage);
			rc = CONTEXT.of_savePlayer(ps);
		}
		
		return rc;
	}
	
	public void of_sendInteractiveMessage(Player p, String Chattext, String Hovertext, String CMD)
	{
		//	Interaktive Chat-Nachricht!
		TextComponent tc = new TextComponent();
		tc.setText(Chattext);
		tc.setBold(Boolean.valueOf(true));
		tc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + CMD));
		tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(Hovertext).create()));
		p.spigot().sendMessage(tc);
	}
}
