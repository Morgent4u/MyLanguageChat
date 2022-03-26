package com.language.objects;

import java.util.ArrayList;
import java.util.HashMap;

import com.language.main.main;
import org.bukkit.entity.Player;

import com.language.ancestor.Objekt;
import com.language.sys.Sys;
import com.language.utils.Datei;
import org.jetbrains.annotations.NotNull;

/**
 * @Created 11.10.2021
 * @Author Nihar
 * @Description
 * This object allows sending predefined text-blocks
 * (in a file) to a players chat.
 */
public class Text extends Objekt
{
	//	Placeholder, Replacement
	private HashMap<String, String> replacements = new HashMap<String, String>();
	Player ps;
	Datei datei;

	/* ************************* */
	/* CONSTRUCTOR */
	/* ************************* */

	/**
	 * Constructor
	 * @param ps Player instance.
	 */
	public Text(Player ps)
	{
		this.ps = ps;
	}

	/**
	 * Constructor
	 * @param fileName Filename of the file.
	 * @param ps Player instance.
	 */
	public Text(@NotNull String fileName, Player ps)
	{
		this.ps = ps;
		datei = new Datei(Sys.of_getMainFilePath()+"texts//"+fileName.toLowerCase());
	}

	/**
	 * Constructor
	 * @param fileName Filename
	 */
	public Text(@NotNull String fileName)
	{
		datei = new Datei(Sys.of_getMainFilePath()+"texts//"+fileName.toLowerCase());
	}

	/* ************************* */
	/* OBJEKT - ANWEISUNGEN */
	/* ************************* */

	/**
	 * Sends the text-block to the player.
	 * While sending the text-block to the player
	 * this function uses specified parameters which
	 * can be used to play a sound or send a hover-text message
	 * to the player.
	 */
	public void of_sendTranslatedText2Player() 
	{
		//	Wenn die Datei noch nicht existiert,
		//	wird diese angelegt!
		if(!datei.of_fileExists()) 
		{
			of_createTemplateFile();
		}

		//	Okay, Text einlesen und übersetzen...
		String[] text = of_getText();
		
		if(text != null) 
		{
			for (String s : text)
			{
				if (s.startsWith("%hover%"))
				{
					String key = s;
					key = key.replace("%hover%", "");
					String[] array_key = key.split(";");

					if (array_key.length == 4)
					{
						String displayText = of_getTranslatedText(array_key[1]);
						String hoverText = of_getTranslatedText(array_key[2]);
						String cmd = array_key[3];
						cmd = cmd.replace("%p%", ps.getName());

						//	Befehls Parameter werden mit den Replacements ersetzt!
						cmd = of_getTranslatedText(cmd);

						main.SPIELERSERVICE.of_sendInteractiveMessage(ps, displayText, hoverText, cmd);
					}
					else
					{
						of_sendErrorMessage(null, "Text.of_translatedText2Player();", "Error while reading file: " + datei.of_getFile().getName() + ". The %hover% parameter is not correct!");
					}
				}
				//	Schauen ob es einen Sound zum abspielen gibt...
				else if (s.toLowerCase().contains("sound="))
				{
					String[] playSoundsFrags = s.split("=");

					if (playSoundsFrags.length == 2)
					{
						//	Sound ermitteln und abspielen.
						String playSound = playSoundsFrags[1];

						if (!playSound.isEmpty())
						{
							ps.playSound(ps.getLocation(), playSound, 1, 1);
						}
					}
				}
				else
				{
					ps.sendMessage(of_getTranslatedText(s));
				}
			}
		}
	}

	/**
	 * By using this function you can define a full predefined text which will be
	 * used as a template for the text-file.
	 * @param texts ArrayList which contains the predefined-messages (for the text-block).
	 */
	public void of_createTemplateFileViaText(ArrayList<String> texts) 
	{
		//	Wenn die Datei nicht existiert...
		if(of_fileExists())
		{
			texts = Sys.of_getReplacedArrayList(texts, "§", "&");
			datei.of_set("Text", texts);
			datei.of_save("Text.of_createTemplateFileViaText(ArrayList<String>);");
		}
	}

	/**
	 * This function creates a default-predefined text-block by the object.
	 */
	public void of_createTemplateFile() 
	{	
		if(of_fileExists())
		{
			ArrayList<String> texts = new ArrayList<String>();
			
			texts.add("&7══════════════");
			texts.add("");
			texts.add("&8[&c"+Sys.of_getProgramVersion()+"&8]");
			texts.add("");
			texts.add("&fTemplate file!");
			texts.add("");
			texts.add("&7══════════════");
			
			datei.of_set("Text", texts);
			datei.of_save("Text.of_createTemplateFile();");
		}
	}

	/* ************************* */
	/* ADDER // SETTER // REMOVER */
	/* ************************* */

	/**
	 * This function allows you to replace the replacement values and keep the placeholder.
	 * @param replaceValuesKeepThePlaceHolder A string-array which contains only the replacements for the placeholders.
	 * @return 1 = SUCCESS, -1 = Error
	 */
	public int of_setReplacementReplaceValues(String[] replaceValuesKeepThePlaceHolder) 
	{
		//	Durchlaufen und löschen darf man nicht. :)
		//	Deswegen wird die ganze Liste ersetzt!
		
		//	Die Anzahl der bisherigen Replacements muss die Anzahl der neuen Placeholder sein!
		if(replacements.size() == replaceValuesKeepThePlaceHolder.length) 
		{
			HashMap<String, String> tmpReplace = new HashMap<>();
			int index = 0;
			
			for(String key : replacements.keySet()) 
			{
				tmpReplace.put(key, replaceValuesKeepThePlaceHolder[index]);
				index++;
			}
			
			//	Austausch...
			replacements = tmpReplace;
			
			return 1;
		}
		
		return -1;
	}

	/**
	 * This function adds a replacement-parameter which will be automatically replaced
	 * by sending the text-block to the player.
	 * @param palceHolder Placeholder for example: %country%
	 * @param replacement Replacement value for example: germany
	 */
	public void of_addReplacement(String palceHolder, String replacement) 
	{
		replacements.put(palceHolder,  replacement);
	}
	
	public void of_setPlayer(Player p) 
	{
		this.ps = p;
	}

	/* ************************* */
	/* GETTER */
	/* ************************* */

	/**
	 * This function is using the MESSAGESERVICE and returns a string
	 * with the translated input text. It also replaces the input text with own
	 * placeholder and replacements.
	 * @param text A string with a message.
	 * @return A string with replaced color codes and own placeholder, replacements from this object.
	 */
	public String of_getTranslatedText(String text) 
	{
		String message = main.MESSAGESERVICE.of_translateMessage(text, true);
		
		message = message.replace("%p%", ps.getName());
		
		//	Eigene Replacements durchlaufen...
		for(String placeholder : replacements.keySet()) 
		{
			message = message.replace(placeholder, replacements.get(placeholder));
		}
		
		return message;
	}

	/**
	 * This function returns the text-block as a string-array.
	 * @return String-array with the text-block messages.
	 */
	public String[] of_getText()
	{
		//	Wenn die Datei noch nicht existiert,
		//	wird diese angelegt!
		if(!datei.of_fileExists()) 
		{
			of_createTemplateFile();
		}

		//	Okay, Text einlesen und übersetzen...
		String[] text = datei.of_getStringArrayByKey("Text");
		
		if(text != null) 
		{
			if(text.length > 0) 
			{
				//	Schnell jede Zeile übersetzen...
				for(int i = 0; i < text.length; i++) 
				{
					text[i] = of_getTranslatedText(text[i]);
				}
				
				return text;
			}
		}
		
		return null;
	}

	/* ************************* */
	/* BOOLS */
	/* ************************* */
	
	public boolean of_fileExists() 
	{
		return datei.of_fileExists();
	}
}
