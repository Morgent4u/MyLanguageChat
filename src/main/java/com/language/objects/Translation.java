package com.language.objects;

import java.util.Collection;

import com.language.main.main;
import org.bukkit.entity.Player;
import com.language.ancestor.Objekt;
import com.language.spieler.Spieler;
import com.language.sys.Sys;

import me.clip.placeholderapi.PlaceholderAPI;

public class Translation extends Objekt
{
	/*	Angelegt am: 21.03.2022
	 * 	Erstellt von: Nihar
	 * 	Beschreibung:
	 * 	Mechanik f�r das �bersetzen von Chat-Nachrichten.
	 * 	
	 */
	
	//	Attribute:
	String[] currentLanguages = new String[0];
	String chatFormat;
	
	/***************************************/
	/* CONSTRUCOTR */
	/***************************************/
	
	public Translation(String chatFormat) 
	{
		this.chatFormat = chatFormat;
	}
	
	/***************************************/
	/* OBJEKT-ANWEISUNGEN */
	/***************************************/
	
	public void of_translateMessageAndSend2AllPlayers(String playerName, String message, boolean useTranslation) 
	{
		Spieler ps = main.SPIELERSERVICE.CONTEXT.of_getSpieler(playerName);
		
		if(ps != null) 
		{
			Player p = ps.of_getPlayer();
			
			//	ChatFormat verwenden:
			String chatFormat = main.SETTINGS.of_getChatDesign();
			
			//	Wenn die PlaceholderAPI verwendet wird...
			if(main.SETTINGS.of_isUsingPlaceholderAPI())
			{
				chatFormat = PlaceholderAPI.setPlaceholders(p, chatFormat);
			}
			//	Schauen ob ggf. VAULT verwendet werden soll...
			else if(main.SETTINGS.of_isUsingVault()) 
			{
				try
				{
					chatFormat = chatFormat.replace("%group%", main.VAULT.PERMISSIONS.getPrimaryGroup(p));			
				}
				catch (Exception e) 
				{
					Sys.of_sendWarningMessage("There was an error by using vault. Please disable vault in the 'settings.yml' or use a permissionssystem.");
					chatFormat = main.SETTINGS.of_getChatDesign();
				}
			}
			
			//	Eigene Parameter bzw. placeholder...
			chatFormat = chatFormat.replace("%p%", p.getName()).replace("%displayname%", p.getDisplayName());

			//	Farbcodes?
			if(ps.of_hasChatColorPermissions()) 
			{
				message = message.replace("&", "�");
			}
			
			//	Spieler ermitteln...
			Collection<Spieler> players = main.SPIELERSERVICE.CONTEXT.of_getAllSpieler();
			
			if(useTranslation) 
			{
				//	Gibt es mehrere Sprachen?
				if(main.SETTINGS.of_isUsingSeperateChats()) 
				{
					//	Sprache �bersetzen und in einem Array erhalten.
					String[] translatedTexts = of_translateTextIntoAllSupportedLanguages(message, ps.of_getDefaultLanguage());
					
					if(translatedTexts != null) 
					{					
						for(int i = 0; i < translatedTexts.length; i++) 
						{
							for(Spieler ds : players) 
							{
								if(ds.of_getDefaultLanguage().equals(currentLanguages[i])) 
								{
									ds.of_getPlayer().sendMessage(chatFormat.replace("%message%", translatedTexts[i]));
								}
							}
						}
					}
				}
				//	Keine Separaten Chats, ein Einheitlicher Chat in einer Sprache!
				else 
				{
					String translatedText = main.WEBSERVICE.of_getTranslatedTextByAutoSource(message, main.SETTINGS.of_getDefaultLanguage4NoSperatechat());
					translatedText = chatFormat.replace("%message%", translatedText);
					
					for(Spieler ds : players) 
					{
						ds.of_getPlayer().sendMessage(translatedText);
					}
				}	
			}
			//	Normale Ausgabe des Textes, nicht �bersetzen (z.B. nur beim �bersetzen via. Chatsymbole)
			else 
			{
				for(Spieler ds : players) 
				{
					message = chatFormat.replace("%message%", message);
					ds.of_getPlayer().sendMessage(message);
				}
			}
		}
	}
	
	public String[] of_translateTextIntoAllSupportedLanguages(String translateText, String sourceLanguage) 
	{
		if(currentLanguages != null) 
		{
			//	�bersetzungs-Array erstellen...
			String[] translatedTexts = new String[currentLanguages.length];
			
			for(int i = 0; i < currentLanguages.length; i++) 
			{
				if(main.SETTINGS.of_isUsingTranslateEveryMessage2UserLanguage()) 
				{
					//	Jede Sprache automatisch in die derzeitige Sprache �bersetzen...
					translatedTexts[i] = main.WEBSERVICE.of_getTranslatedTextByAutoSource(translateText, currentLanguages[i]);	
				}
				//	Wenn die �bersetzungssprache gleich die Ausgangssprache ist, sparen wir uns eine WebService-Anfrage...
				else if(currentLanguages[i].equals(sourceLanguage)) 
				{
					translatedTexts[i] = translateText;
				}
				//	In Sprache �bersetzen...
				else 
				{
					translatedTexts[i] = main.WEBSERVICE.of_getTranslatedTextBySpecificSource(translateText, sourceLanguage, currentLanguages[i]);	
				}
			}
			
			return translatedTexts;
		}
		
		return null;
	}
	
	/***************************************/
	/* ADDER // REMOVER */
	/***************************************/
	
	public void of_addLanguageAsCurrentLanguage(String language) 
	{
		boolean bool = false;

		for (String currentLanguage : currentLanguages)
		{
			if (currentLanguage.equals(language))
			{
				bool = true;
				break;
			}
		}
		
		if(!bool) 
		{
			currentLanguages = Sys.of_addArrayValue(currentLanguages, language);
		}
	}
	
	//	Man kann nicht das Array durchlaufen und anschlie�end werte rauswerfen...
	public void of_removeLanguageFromCurrentLanguages(String language) 
	{
		currentLanguages = Sys.of_removeArrayValue(currentLanguages, language);
	}
}
