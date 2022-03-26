package com.language.objects;

import java.util.Collection;

import com.language.main.main;
import org.bukkit.entity.Player;
import com.language.ancestor.Objekt;
import com.language.spieler.Spieler;
import com.language.sys.Sys;

import me.clip.placeholderapi.PlaceholderAPI;

/**
 * @Created 26.03.2022
 * @Author Nihar
 * @Description
 * This object is used to translate the messages to the
 * user language. It also stores the supported languages.
 */
public class Translation extends Objekt
{
	//	Attribute:
	String[] currentLanguages = new String[0];
	String chatFormat;

	/* ************************* */
	/* CONSTRUCTOR */
	/* ************************* */

	/**
	 * Constructor
	 * @param chatFormat A string with a predefined chat-format.
	 *                   For example: '&8[&c%group%&8]&a %p%&f:&7 %message%'
	 */
	public Translation(String chatFormat) 
	{
		this.chatFormat = chatFormat;
	}

	/* ************************* */
	/* OBJEKT-ANWEISUNGEN */
	/* ************************* */

	/**
	 * This function translates the user message into all supported languages and
	 * sends it to the specific language of other players.
	 * @param playerName Player name of the player which sends the text-message.
	 * @param message Text-message
	 * @param useTranslation Translate the text-message.
	 */
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
				if(main.SETTINGS.of_isUsingSeparateChats())
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
					String translatedText = main.WEBSERVICE.of_getTranslatedTextByAutoSource(message, main.SETTINGS.of_getDefaultLanguage4NoSeparateChats());
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

	/**
	 * This function translates the text-message into all supported languages.
	 * @param translateText Text-message
	 * @param sourceLanguage The language in which the parameter 'translateText' has been written.
	 *                       For example: sourceLanguage = 'EN'
	 *                       Translate text: 'Hello how are you?'
	 * @return A string array which contains the translated messages, in the order of the supported languages array.
	 */
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

	/* ***************************** */
	/* SETTER // ADDER // REMOVER */
	/* ***************************** */

	/**
	 * Add a language as current supported language.
	 * @param language Language country code. For example: 'EN'
	 */
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

	/**
	 * This function removes a language or country code from the supported languages.
	 * @param language Language country code. For example: 'EN'
	 */
	public void of_removeLanguageFromCurrentLanguages(String language) 
	{
		currentLanguages = Sys.of_removeArrayValue(currentLanguages, language);
	}

	/* ************************* */
	/* BOOLS */
	/* ************************* */

	/**
	 * Is used to check if the language is supported or does not exist.
	 * @param language The country code for example: DE or EN
	 * @return TRUE = For this country code is a language defined. FALSE = For this country code is no language defined!
	 */
	public boolean of_languageIsSupported(String language)
	{
		String[] languages = main.SETTINGS.of_getSupportedLanguages();

		if(languages != null && languages.length > 0)
		{
			for (String s : languages)
			{
				//	Hat der Spieler ein Ländercode eingegeben?
				if (s.equalsIgnoreCase(language))
				{
					return true;
				}
			}
		}

		return false;
	}
}
