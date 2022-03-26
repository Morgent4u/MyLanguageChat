package com.language.boards;

import java.util.ArrayList;
import java.util.HashMap;

import com.language.sys.Sys;
import com.language.utils.Datei;
import com.language.ancestor.Objekt;
import com.language.objects.Text;
import org.bukkit.entity.Player;

public class MessageService extends Objekt
{
	/*	Angelegt am: 12.10.2021
	 * 	Erstellt von: Nihar
	 * 	Beschreibung:
	 * 	Mit dem MessageBoard k�nnen
	 * 	predefinedMessages erstellt werden,
	 * 	die anschlie�end als Datei hinterlegt werden
	 * 	zu bearbeiten.
	 * 	
	 */
	
	//	configKeys bzw. msgId - Nachricht
	private HashMap<String, String> messages = new HashMap<String, String>();
	private Datei datei;
	private String prefix;
	private boolean ib_alwaysPrefix;
	
	/***************************************/
	/* Constructor */
	/***************************************/
	
	public MessageService(String filePath) 
	{
		datei = new Datei(filePath);
	}
	
	@Override
	public int of_load() 
	{
		//	Sicherstellen, dass die Datei existiert.
		of_createTemplateFile();
		
		//	Sektionen bzw. Bereiche Laden...
		of_load(new String[] {"General"});
		of_load(new String[] {"MyLanguageChat"});
		of_load(new String[] {"Sounds"});
		
		return 1;
	}
	
	@Override
	public int of_load(String[] args) 
	{
		String[] configKeys =  datei.of_getKeySectionsByKey(args[0]);
		
		if(configKeys != null) 
		{
			//	Sicherstellen, dass wir Keys haben bzw. Sectionen...
			if(configKeys.length > 0) 
			{
				for(int i = 0; i < configKeys.length; i++) 
				{
					String lokalKey = args[0]+"."+configKeys[i];
					String tmpMessage = datei.of_getStringByKey(lokalKey);
					
					if(tmpMessage != null) 
					{
						//	Schon mal ein bisschen übersetzen :)
						messages.put(lokalKey, tmpMessage);
					}
				}
			}
		}
		
		return 1;
	}
	
	/***************************************/
	/* Objekt-Anweisungen */
	/***************************************/
	
	private void of_createTextTemplateFiles() 
	{
		//	Text-Invites
		Text txt = new Text("txt_cmdhelper4user");
		
		if(!txt.of_fileExists()) 
		{
			//	Template:
			ArrayList<String> texts = new ArrayList<String>();
			
			texts.add("§7══════════════");
			texts.add("");
			texts.add("§8[§c§lMyLanguageChat§8]");
			texts.add("");
			texts.add("§5Commands:");
			texts.add("§f/Language -§a Shows this help.");
			texts.add("§f/Language list§a - List of supported languages.");
			texts.add("§f/Language <language>§a - Select a supported language.");
			texts.add("");
			texts.add("§7══════════════");
			texts.add("PLAYSOUND=block.anvil.hit");
			
			txt.of_createTemplateFileViaText(texts);
			txt.of_save();
		}
		
		txt = new Text("txt_supported_languages");
		
		if(!txt.of_fileExists()) 
		{
			//	Template:
			ArrayList<String> texts = new ArrayList<String>();
			
			texts.add("§7══════════════");
			texts.add("");
			texts.add("§8[§c§lMyLanguageChat§8]");
			texts.add("");
			texts.add("§5Supported-Languages:");
			texts.add("FORMAT=&f%countrycode% &7- &a%language%");
			texts.add("");
			texts.add("§7══════════════");
			texts.add("PLAYSOUND=block.anvil.hit");
			
			txt.of_createTemplateFileViaText(texts);
			txt.of_save();
		}
	}
	
	private void of_createTemplateFile() 
	{
		//	AUTO-CREATE MODE :^)
		datei.of_setAutoCreateMode(true);
		
		//	Settings:
		prefix = datei.of_getSetString("Settings.prefix", "&8[&aMyLanguage&fChat&8]&f:&7");
		prefix = prefix.replace("&", "§");
		ib_alwaysPrefix = datei.of_getSetBoolean("Settings.alwaysPrefix", true);
		
		//	Messages:
		datei.of_set("General.noPermissions", "&cYou do not have permissions to do that!");
		datei.of_set("General.errorMessage", "&fHey &a%p%&f an error occurred! Error: &c%errorMessage%");
		
		//	General:
		datei.of_set("MyLanguageChat.languageSelected", "&fYou have selected the language: &a%language%");
		datei.of_set("MyLanguageChat.languageNotFound", "&fThe language: &a%language%&f could not be found!");

		//	Sounds:
		datei.of_set("Sounds.noPermissions", "block.sand.fall");
		datei.of_set("Sounds.languageSelected", "entity.item.pickup");

		//	Ende des AutoCreateMode
		datei.of_setAutoCreateMode(false);
		datei.of_save("MessageService.of_createTemplate();");
		
		//	Fehlende Texte hinzufügen...
		of_createTextTemplateFiles();
	}
	
	public String of_translateMessageWithPlayerStats(Player p, String message) 
	{
		//	Generell übersetzen...
		message = of_translateMessage(message, true);
		
		//	Erst mal Spieler Inhalte übersetzen...
		message = message.replace("%p%", p.getName()).replace("%name%", p.getName());
		message = message.replace("%uuid%", p.getUniqueId().toString());
		message = message.replace("%displayname%", p.getDisplayName());
		
		return message;
	}
	
	public String of_translateMessage(String message) 
	{	
		//	Übersetzung:
		message = message.replace("&", "§");
		message = message.replace("%prefix%", prefix);
		
		//	Prefix setzen.
		if(ib_alwaysPrefix) 
		{
			message = prefix + " " + message;
		}
		
		return message;
	}
	
	public String of_translateMessage(String message, boolean sendFromTextObject) 
	{	
		//	Übersetzung:
		message = message.replace("&", "§");
		message = message.replace("%prefix%", prefix);
		
		return message;
	}
	
	/***************************************/
	/* DebugCenter */
	/***************************************/
	
	@Override
	public void of_sendDebugDetailInformation() 
	{
		Sys.of_sendMessage("Messages Count: "+of_getMessageCount());

		if(of_getMessageCount() > 0) 
		{
			Sys.of_sendMessage("Msg-Key: | Message:");
			
			for(String key : messages.keySet()) 
			{
				Sys.of_sendMessage(key + ": "+messages.get(key));
			}
		}
	}
	
	/*************************************************/
	/* STANDARD NACHRICHTEN // HIER IST GENERAL-TEIL */
	/*************************************************/
	
	public int of_sendMsgHasNoPermissions(Player p) 
	{
		return of_getMessage(p, "General.noPermissions");
	}
	
	public int of_sendMsgThatAnErrorOccurred(Player p, String errorMessage) 
	{
		return of_getMessage(p, "General.errorMessage", new String[] {"%errorMessage%"}, new String[] {errorMessage});
	}
	
	public int of_sendMsgPlayerIsNotOnline(Player p) 
	{
		return of_getMessage(p, "General.playerIsNotOnline");
	}
	
	/***************************************/
	/* GETTER */
	/***************************************/
	
	//	Hauptüberladung YEAH!
	private String of_getMessage(String msgKey) 
	{
		String message = messages.get(msgKey);
		
		//	Nachricht existiert noch nicht, also wird diese angelegt.
		//	Es wird anschlie�end der alternativeText verwendet.
		
		if(message == null) 
		{
			message = "%prefix% &fMissing MessageKey! MsgKey="+msgKey;
			datei.of_set(msgKey, message.replace("§", "&"));
			datei.of_save("MessageService.of_getMessage(String);");
		}
		
		message = of_translateMessage(message);
		
		return message;
	}
	
	public int of_getMessage(Player p, String msgKey) 
	{
		String message = of_getMessage(msgKey);
		
		if(p != null) 
		{
			//	Erst mal Spieler Inhalte übersetzen...
			message = message.replace("%p%", p.getName()).replace("%name%", p.getName());
			message = message.replace("%uuid%", p.getUniqueId().toString());
			message = message.replace("%displayname%", p.getDisplayName());
			
			p.sendMessage(message);
			of_playSound4Player(p, msgKey);
			return 1;
		}

		return -1;
	}
	
	public int of_getMessage(Player p, String msgKey, Player d) 
	{
		String message = of_getMessage(msgKey);
		
		if(p != null) 
		{
			//	Erst mal Spieler Inhalte übersetzen...
			message = message.replace("%p%", p.getName()).replace("%name%", p.getName());
			message = message.replace("%uuid%", p.getUniqueId().toString());
			message = message.replace("%displayname%", p.getDisplayName());
			
			//	Spieler 2 spezifische replacements:
			message = message.replace("%otherPlayer%", d.getName());
			
			p.sendMessage(message);
			of_playSound4Player(p, msgKey);
			return 1;
		}

		return -1;
	}
	
	public int of_getMessage(Player p, String msgKey, String[] placeHolder, String[] replacements) 
	{
		//	In of_getMessage werden noch schnell die replacements durchlaufen.... on the fly :^)
		String message = of_getMessage(msgKey, placeHolder, replacements);
		
		if(p != null) 
		{
			//	Erst mal Spieler Inhalte übersetzen...
			message = message.replace("%p%", p.getName()).replace("%name%", p.getName());
			message = message.replace("%uuid%", p.getUniqueId().toString());
			message = message.replace("%displayname%", p.getDisplayName());
			p.sendMessage(message);
			of_playSound4Player(p, msgKey);
			return 1;
		}

		return -1;
	}
	
	private String of_getMessage(String msgKey, String[] placeHolder, String[] replacements) 
	{
		String message = of_getMessage(msgKey);
		
		//	Sicherstellen, dass die Placeholder und die Replacements korrekt angegeben wurden.
		if(placeHolder != null && replacements != null) 
		{
			if(placeHolder.length == replacements.length) 
			{
				//	Nun übersetzen 1 zu 1 :)
				for(int i = 0; i < placeHolder.length; i++) 
				{
					message = message.replace(placeHolder[i], replacements[i]);
				}
			}
		}
		
		return message;
	}
	
	public String of_getMessage(String msgKey, String[] replacementAndPlaceholder) 
	{
		//  of_getMessage(String msgKey, String[] placeHolder, String[] replacements)
		//	Diese Methode ist der obrigen sehr ähnlich, jedoch wird hier nur 1x Array benötigt, was das
		//	programmieren an einigen Stellen vereinfacht :)
		
		String message = of_getMessage(msgKey);
		
		if(replacementAndPlaceholder != null) 
		{
			if(replacementAndPlaceholder.length > 0) 
			{
				//	Übersetzungsprozess der Replacements...
				for(int i = 0; i < replacementAndPlaceholder.length; i++) 
				{
					if(Integer.valueOf(i + 1) != replacementAndPlaceholder.length) 
					{
						message = message.replace(replacementAndPlaceholder[i], replacementAndPlaceholder[Integer.valueOf(i + 1)]);						
					}
				}
			}
		}
		
		return message;
	}
	
	public String of_getMessageByMsgKey(String msgKey) 
	{
		return messages.get(msgKey);
	}
	
	private void of_playSound4Player(Player p, String msgKey) 
	{
		//	MessageKeyFragments
		String[] messagKeyFragments = msgKey.split("\\.", 2);
		
		//	MessageKeyFragments aufbrechen...
		if(messagKeyFragments != null && messagKeyFragments.length == 2) 
		{
			String playSound = messages.get("Sounds."+messagKeyFragments[1]);
			
			if(playSound != null) 
			{
				//	Wenn Empty, sound nicht abspielen!
				if(!playSound.isEmpty()) 
				{
					playSound = playSound.toLowerCase();
					p.playSound(p.getLocation(), playSound, 1, 1);	
				}	
			}
		}
	}
	
	public int of_playSound4PlayerBySoundKey(Player p, String soundKey) 
	{
		//	RC:
		//	 1: OK
		//	-1: Sound nicht gefunden.
		//	-2: Nicht abspielen!
		
		String playSound = messages.get(soundKey);
		
		if(playSound != null) 
		{
			//	Wenn Empty, sound nicht abspielen!
			if(!playSound.isEmpty()) 
			{
				playSound = playSound.toLowerCase();
				p.playSound(p.getLocation(), playSound, 1, 1);
				return 1;
			}
			
			return -2;
		}

		return -1;
	}
	
	public int of_getMessageCount() 
	{
		return messages.size();
	}
	
	public int of_getParameterCount() 
	{
		return messages.size();
	}
}
