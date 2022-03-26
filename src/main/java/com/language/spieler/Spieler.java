package com.language.spieler;

import com.language.main.main;
import org.bukkit.entity.Player;

import com.language.ancestor.Objekt;

/**
 * @Created 20.03.2022
 * @Author Nihar
 * @Description
 * This class is used to represent a player for
 * this plugin.
 */
public class Spieler extends Objekt
{
	//	Extra-Attribute:
	Player p;
	
	//	Default-Attribute:
	String name;
	String uuid;
	String defaultLanguage;

	/* ************************************* */
	/* CONSTRUCTOR */
	/* ************************************* */

	/**
	 * Constructor
	 * @param p Player instance.
	 */
	public Spieler(Player p)
	{
		this.p = p;
		this.name = p.getName();
		this.uuid = p.getUniqueId().toString();
	}

	/* ************************************* */
	/* SETTER // ADDER // REMOVER */
	/* ************************************* */
	
	public void of_setName(String name) 
	{
		this.name = name;
	}
	
	public void of_setUUID(String uuid) 
	{
		this.uuid = uuid;
	}
	
	public void of_setDefaultLanguage(String defaultLanguage) 
	{
		this.defaultLanguage = defaultLanguage;
		
		//	Sprache ggf. zum Translations-Objekt hinzufuegen...
		main.TRANSLATION.of_addLanguageAsCurrentLanguage(defaultLanguage);
	}

	/* ************************************* */
	/* GETTER */
	/* ************************************* */
	
	public Player of_getPlayer() 
	{
		return p;
	}
	
	public String of_getName() 
	{
		return name;
	}
	
	public String of_getUUID() 
	{
		return uuid;
	}
	
	public String of_getDefaultLanguage() 
	{
		return defaultLanguage;
	}

	/* ************************************* */
	/* BOOLS */
	/* ************************************* */
	
	public boolean of_hasDefaultPermissions() 
	{
		return p.hasPermission("mylanguagechat.default");
	}
	
	public boolean of_hasSetupPermissions() 
	{
		return p.hasPermission("mylanguagechat.setup");
	}
	
	public boolean of_hasChatColorPermissions() 
	{
		return p.hasPermission("mylanguagechat.chatcolor");
	}
}
