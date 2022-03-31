package com.language.boards;

import java.util.ArrayList;
import java.util.HashMap;

import com.language.sys.Sys;
import com.language.utils.Datei;
import com.language.ancestor.Objekt;
import com.language.objects.Text;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

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

	/* ************************* */
	/* CONSTRUCTOR */
	/* ************************* */
	
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
	public void of_load(String[] args)
	{
		String[] configKeys =  datei.of_getKeySectionsByKey(args[0]);
		
		if(configKeys != null) 
		{
			//	Sicherstellen, dass wir Keys haben bzw. Sectionen...
			if(configKeys.length > 0) 
			{
				for (String configKey : configKeys)
				{
					String localKey = args[0] + "." + configKey;
					String tmpMessage = datei.of_getString(localKey);

					if (tmpMessage != null)
					{
						//	Schon mal ein bisschen übersetzen :)
						messages.put(localKey, tmpMessage);
					}
				}
			}
		}
    }

	/* ************************* */
	/* OBJEKT-ANWEISUNGEN */
	/* ************************* */

	/**
	 * Is used to create predefined texts by using the
	 * object Text.
	 */
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

	/**
	 * Creates the messagesSounds.yml template.
	 */
	private void of_createTemplateFile() 
	{
		//	Settings:
		prefix = datei.of_getSetString("Settings.prefix", "&8[&aMyLanguage&fChat&8]&f:&7");
		prefix = prefix.replace("&", "§");
		ib_alwaysPrefix = datei.of_getSetBoolean("Settings.alwaysPrefix", true);
		
		//	Messages:
		datei.of_getSetString("General.noPermissions", "&cYou do not have permissions to do that!");
		datei.of_getSetString("General.errorMessage", "&fHey &a%p%&f an error occurred! Error: &c%errorMessage%");
		
		//	General:
		datei.of_getSetString("MyLanguageChat.languageSelected", "&fYou have selected the language: &a%language%");
		datei.of_getSetString("MyLanguageChat.languageNotFound", "&fThe language: &a%language%&f could not be found!");

		//	Sounds:
		datei.of_getSetString("Sounds.noPermissions", "block.sand.fall");
		datei.of_getSetString("Sounds.languageSelected", "entity.item.pickup");

		datei.of_save("MessageService.of_createTemplate();");
		
		//	Fehlende Texte hinzufügen...
		of_createTextTemplateFiles();
	}

	/**
	 * Is used to translate a message with player stats.
	 * @param p Player instance.
	 * @param message Message which should be translated with placeholder and color codes.
	 * @return A string which contains the input message with translated placeholder and color codes.
	 */
	public String of_translateMessageWithPlayerStats(@NotNull Player p, String message)
	{
		//	Generell übersetzen...
		message = of_translateMessage(message, true);
		
		//	Erst mal Spieler Inhalte übersetzen...
		message = message.replace("%p%", p.getName()).replace("%name%", p.getName());
		message = message.replace("%uuid%", p.getUniqueId().toString());
		message = message.replace("%displayname%", p.getDisplayName());
		
		return message;
	}

	/**
	 * Is used to translate a text with the default prefix and color codes.
	 * @param message String which contains replacements and the default prefix.
	 * @return A string which contains the input message with translated placeholder and color codes.
	 */
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

	/**
	 * Is used to get a replaced message without an automatic added prefix.
	 * @param message String which contains replacements.
	 * @param sendFromTextObject This is a useless boolean.
	 * @return A string which contains the translated/replaced input message.
	 */
	public String of_translateMessage(String message, boolean sendFromTextObject)
	{	
		//	Übersetzung:
		message = message.replace("&", "§");
		message = message.replace("%prefix%", prefix);
		
		return message;
	}

	/**
	 * This function is used to play a sound to a specific message when it's defined in the messageSounds.yml.
	 * If the specific sound-attribute to the message-key contains an empty result, the sound will not be played.
	 * @param p Player instance.
	 * @param msgKey Message-key.
	 */
	private void of_playSound4Player(Player p, String msgKey)
	{
		//	MessageKeyFragments
		String[] messageKeyFragments = msgKey.split("\\.", 2);

		//	MessageKeyFragments aufbrechen...
		if(messageKeyFragments.length == 2)
		{
			String playSound = messages.get("Sounds."+messageKeyFragments[1]);

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

	/**
	 * Plays a specific sound by the sound-key which is defined in the messagesSounds.yml
	 * @param p Player instance.
	 * @param soundKey Sound-key.
	 * @return 1 = Sound has been played. -1 = Sound not found for the sound-key. -2 = Cannot play the sound because it's defined so in the messagesSounds.yml
	 */
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

	/* ************************* */
	/* DEBUG CENTER */
	/* ************************* */
	
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

	/* ************************* */
	/* STANDARD NACHRICHTEN */
	/* ************************* */
	
	public void of_sendMsgHasNoPermissions(Player p)
	{
		of_getMessage(p, "General.noPermissions");
	}
	
	public int of_sendMsgThatAnErrorOccurred(Player p, String errorMessage) 
	{
		return of_getMessage(p, "General.errorMessage", new String[] {"%errorMessage%"}, new String[] {errorMessage});
	}
	
	public int of_sendMsgPlayerIsNotOnline(Player p) 
	{
		return of_getMessage(p, "General.playerIsNotOnline");
	}

	/* ************************* */
	/* GETTER */
	/* ************************* */

	/**
	 * Gets a messages by the message-key which is set in the messagesSounds.yml
	 * @param msgKey The message key for example: 'General.hasNoPermissions'
	 * @return A message which is defined for the message-key. This message also have translated color codes or placeholder.
	 */
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

	/**
	 * Is used to get a message by the message-key. After getting the message-key
	 * the message will be automatically replaced with the player stats.
	 * Specific: This function plays a sound for the player, if a sound is defined to the message-key.
	 * @param p Placer instance
	 * @param msgKey Message-key
	 * @return A message which is translated with the player stats.
	 */
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

	/**
	 * Extended function of_getMessage(Player, String) with a otherPlayer-instance.
	 * This is used to replace the placeholder: %otherPlayer% in the message.
	 * @param p First player instance.
	 * @param msgKey Message-key.
	 * @param d Second player instance.
	 * @return A message which is translated with the player stats of the first player.
	 * If this message contains the placeholder %otherPlayer% this placeholder will be replaced wiht the
	 * second player name.
	 */
	public int of_getMessage(Player p, String msgKey, Player d) 
	{
		String message = of_getMessage(msgKey);
		
		if(p != null) 
		{
			int rc = of_getMessage(p, msgKey);

			if(rc == 1)
			{
				//	Spieler 2 spezifische replacements:
				message = message.replace("%otherPlayer%", d.getName());

				p.sendMessage(message);
				of_playSound4Player(p, msgKey);
				return 1;
			}
		}

		return -1;
	}

	/**
	 * This function is an extended function of of_getMessage(String, String[], String[]).
	 * This extension is for the player specific stats. It's used to add dynamically placeholders and replacements
	 * to a specific message-key.
	 * @param p Player instance.
	 * @param msgKey Message-key.
	 * @param placeHolder An array which contains the placeholder for example: new String[] {"%currentDeaths%"};
	 * @param replacements An array which contains the replacement for example new String[] {ps.of_getDeathsAsString()};
	 * @return A message which is defined by the message-key. This message also has dynamic replaced placeholder with the defined replacements.
	 */
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

	/**
	 * This function is used to add dynamically placeholders and replacements to a specific message-key.
	 * @param msgKey Message-key.
	 * @param placeHolder An array which contains the placeholder for example: new String[] {"%currentPlayers%"};
	 * @param replacements An array which contains the replacement for example new String[] {Bukkit.getOnlinePlayers().size().toString()};
	 * @return A message which is defined by the message-key. This message also has dynamic replaced placeholder with the defined replacements.
	 */
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

	/**
	 * This function is equal to the function of_getMessage(String msgKey, String[] placeholder, String[] replacements).
	 * The different is that the placeholder and replacements are defined in the same array.
	 * @param msgKey Message-key.
	 * @param replacementAndPlaceholder An array which contains in the previous place the placeholder and in the next element the
	 *                                  replacement. For exmaple: new String[] {"%time%", new Date().getTime().toString()};
	 * @return A message which is defined by the message-key. This message also has dynamic replaced placeholder with the defined replacements.
	 */
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
					if(i + 1 != replacementAndPlaceholder.length)
					{
						message = message.replace(replacementAndPlaceholder[i], replacementAndPlaceholder[Integer.valueOf(i + 1)]);						
					}
				}
			}
		}
		
		return message;
	}

	/**
	 * Gets the message by message-key without a translation!
	 * @param msgKey Message-key.
	 * @return The message specified to the message-key.
	 */
	public String of_getMessageByMsgKey(String msgKey) 
	{
		return messages.get(msgKey);
	}

	/**
	 * Returns the amount of loaded messages/sounds.
	 * @return Amount
	 */
	public int of_getMessageCount() 
	{
		return messages.size();
	}
}
