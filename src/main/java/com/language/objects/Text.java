package com.language.objects;

import java.util.ArrayList;
import java.util.HashMap;

import com.language.main.main;
import org.bukkit.entity.Player;

import com.language.ancestor.Objekt;
import com.language.sys.Sys;
import com.language.utils.Datei;

public class Text extends Objekt
{
	/*	Angelegt am: 11.10.2021
	 * 	Erstellt von: Nihar
	 * 	Beschreibung:
	 * 	Mit dem Text-Objekt ist es möglich
	 * 	vordefinierte Texte an den Player
	 *	zu übermitteln.
	 * 	
	 */
	
	//	Placeholder, Replacement
	private HashMap<String, String> replacements = new HashMap<String, String>();
	Player ps;
	Datei datei;
	
	/***************************************/
	/* Constructor */
	/***************************************/
	
	public Text(Player ps)
	{
		this.ps = ps;
	}
	 
	public Text(String fileName, Player ps)
	{
		this.ps = ps;
		datei = new Datei(Sys.of_getMainFilePath()+"texts//"+fileName.toLowerCase());
	}
	
	public Text(String fileName)
	{
		datei = new Datei(Sys.of_getMainFilePath()+"texts//"+fileName.toLowerCase());
	}
	
	/***************************************/
	/* Objekt-Anweisungen */
	/***************************************/
	
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
			for(int i = 0; i < text.length; i++) 
			{
				if(text[i].startsWith("%hover%")) 
				{
					String key = text[i];
					key = key.replace("%hover%", "");
					String[] array_key = key.split(";");
					
					if(array_key.length == 4) 
					{
						String displayText = of_getTranslatedText(array_key[1]);
						String hovertext = of_getTranslatedText(array_key[2]);
						String cmd = array_key[3];
						cmd = cmd.replace("%p%", ps.getName());
						
						//	Befehls Parameter werden mit den Replacements ersetzt!
						cmd = of_getTranslatedText(cmd);
						
						main.SPIELERSERVICE.of_sendInteractiveMessage(ps, displayText, hovertext, cmd);
					}
					else 
					{
						of_sendErrorMessage(null, "Text.of_translatedText2Player();", "Error while reading file: "+datei.of_getFile().getName()+". The %hover% parameter is not correct!");
					}
				}
				//	Schauen ob es einen Sound zum abspielen gibt...
				else if(text[i].toLowerCase().contains("sound="))
				{
					String[] playSoundsFrags = text[i].split("=");
					
					if(playSoundsFrags.length == 2) 
					{
						//	Sound ermitteln und abspielen.
						String playSound = playSoundsFrags[1];
						
						if(!playSound.isEmpty()) 
						{
							ps.playSound(ps.getLocation(), playSound, 1, 1);
						}
					}
				}
				else 
				{
					ps.sendMessage(of_getTranslatedText(text[i]));
				}
			}
		}
	}
	
	public void of_createTemplateFileViaText(ArrayList<String> texts) 
	{
		//	Wenn die Datei nicht existiert...
		if(!of_fileExists()) 
		{
			texts = Sys.of_getReplacedArrayList(texts, "§", "&");
			datei.of_set("Text", texts);
			datei.of_save("Text.of_createTemplateFileViaText(ArrayList<String>);");
		}
	}
	
	public void of_createTemplateFile() 
	{	
		if(!of_fileExists()) 
		{
			ArrayList<String> texts = new ArrayList<String>();
			
			texts.add("&7══════════════");
			texts.add("");
			texts.add("&8[&c"+Sys.of_getProgrammVersion()+"&8]");
			texts.add("");
			texts.add("&fTemplate file!");
			texts.add("");
			texts.add("&7══════════════");
			
			datei.of_set("Text", texts);
			datei.of_save("Text.of_createTemplateFile();");
		}
	}
	
	/***************************************/
	/* ADDER // SETTER // REMOVER */
	/***************************************/
	
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
	
	public void of_addReplacement(String palceHolder, String replacement) 
	{
		replacements.put(palceHolder,  replacement);
	}
	
	public void of_setPlayer(Player p) 
	{
		this.ps = p;
	}
	
	/***************************************/
	/* GETTER */
	/***************************************/
	
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
	
	/***************************************/
	/* BOOLS */
	/***************************************/
	
	public boolean of_fileExists() 
	{
		return datei.of_fileExists();
	}
}
