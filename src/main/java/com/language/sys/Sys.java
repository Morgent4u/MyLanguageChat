package com.language.sys;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import com.language.main.main;
import org.bukkit.Bukkit;
import com.google.common.base.Splitter;
import com.language.utils.Datei;

public class Sys
{
	/*	Angelegt am: 20.03.2021
	 * 	Erstellt von: Nihar
	 * 	Beschreibung:
	 * 	Systemklasse des Plugins.
	 * 	In dieser Klasse befinden sich grundlegende
	 * 	Funkionen/Methoden. Des Weiteren wird hier auch
	 * 	unter anderem die Plugin-Version überprüft.
	 * 
	 */
	
	//	DebugMessages
	private static ArrayList<String> debugMessagesBuffer = new ArrayList<String>();

	//	Attribute der Systemklasse
	private static String paket;
	private static String programmVersion;
	private static String version;
	private static String mainRootPath;
	
	private static boolean ib_hotfix;
	private static boolean ib_debug = true;
	private static int debugCounter;

	/* Start/Close des Plugins */
	/***************************************/
	
	public static boolean of_isSystemVersionCompatible(String paketName, String versionNummer, String fileRootPath) 
	{
		boolean versionCompatible = false;
		
		//	Paket & Ordner-Hauptverzeichnis bestimmten.
		paket = paketName;
		version = versionNummer;
		programmVersion = of_getPaket() + " v"+version;
		mainRootPath = fileRootPath + "//"+paketName+"//";
		
		//	Datei: version.yml erstellen oder Versions-Nummer überprüfen...
		Datei datei = new Datei(mainRootPath + "version.yml");
		
		//	Attribute der Systemversion einholen...
		String oldVersion = datei.of_getSetString("Version", of_getProgrammVersion());
		ib_debug = datei.of_getSetBoolean("DebugMessages", true);
		datei.of_save("Sys.of_isSystemVersionCompatible();");
		
		//	Beispiel:
		//	Current-Version: 22.1.[1].[01]
		//	Old-Version:	 21.1.[1].[22]
		//	Das Erste  [] => Hotfix-Version
		//	Das Zweite [] => Programmversions-Nummer 
		
		String[] currentSysVersion = version.split("\\.");
		String[] oldSysVersion = oldVersion.replace(of_getPaket(), "").replace(" v", "").split("\\.");
		
		//	Sicherstellen, dass es eine derzeitige System-Versionsnummer und vergangene System-Versionsnummer gibt.
		if(currentSysVersion.length == 4 && oldSysVersion.length == 4)
		{
			//	Die ersten 2-Stellen müssen identisch sein.
			//	Beispiel:
			//  22.1 equals 22.1
			//	Die Erstestelle: Jahrgang
			//	Die Zweitestelle: Release-Nummer
			
			if(currentSysVersion[0].equals(oldSysVersion[0]) && currentSysVersion[1].equals(oldSysVersion[1])) 
			{
				//	Sicherstellen, dass die aktuelle Programmversions-Nummer gleich oder neuer als die alte Programmversions-Nummer ist.
				//	Das ist die 3te Stelle im !Array!, siehe obriges Beispiel!
				int currentProgrammVersionNumber = Integer.parseInt(currentSysVersion[3]);
				int oldProgrammVersionNumber = Integer.parseInt(oldSysVersion[3]);
				
				//	Version ist Kompatibel?
				if(currentProgrammVersionNumber >= oldProgrammVersionNumber) 
				{
					versionCompatible = true;
					
					//	Hinweis-Meldung ausgeben, dass die Dateien veraltet sind.
					if(currentProgrammVersionNumber > oldProgrammVersionNumber) 
					{
						String red = "\u001B[31m";
						String white = "\u001B[0m";
						of_sendMessage(red+"This plugin-version is newer than your system files. This can possible cause some problems."+white);
					}
					
					//	Ist diese PL-Version eine Hotfix?
					if(currentSysVersion[2].equals("1")) 
					{
						ib_hotfix = true;
					}
				}
			}
		}
		
		//	Version ist nicht kompatibel.
		if(!versionCompatible) 
		{
			of_sendErrorMessage(null, "Sys", "Versionscheck", "This plugin-version does not match with the 'version.yml'. To continue with a not supported plugin-version, you can delete the 'version.yml' and reload the server.");
		}
		
		return versionCompatible;
	}

	/* Anweisungen der Klasse */
	/***************************************/
	
	public static void of_sendDebugMessages2Console() 
	{
		if(!debugMessagesBuffer.isEmpty()) 
		{
			for(String debugMessage : debugMessagesBuffer) 
			{
				of_sendMessage(debugMessage);
			}
			
			debugMessagesBuffer.clear();
		}
	}
	
	public static void of_sendMessage(String message) 
	{
		main.PLUGIN.getLogger().info(message);
	}
	
	public static void of_sendWarningMessage(String message) 
	{
		String red = "\u001B[31m";
		String white = "\u001B[0m";
		
		main.PLUGIN.getLogger().info(red+"[WARNING]: "+white+message);
	}

	/* DebugCenter */
	/***************************************/
	
	public static void of_debug(String message) 
	{
		//	Debug-Nachricht ausgeben...
		if(of_isDebugModeEnabled()) 
		{
			debugCounter++;
			message = "DEBUG[#"+debugCounter+"]: " + message;
			
			of_sendMessage(message);
		}
		else 
		{
			debugMessagesBuffer.add(message);
		}
	}

	/* ErrorHandler */
	/***************************************/
	
	public static void of_sendErrorMessage(Exception exception, String systemArea, String invoker, String errorMessage) 
	{
		//	Farbcodes
		String red = "\u001B[31m";
		String white = "\u001B[0m";
		String yellow = "\u001B[33m";
		String blue = "\u001B[36m";
		
		Sys.of_sendMessage("=====================================");
		Sys.of_sendMessage(red+"[ERROR] "+yellow+Sys.of_getProgrammVersion()+white);
		Sys.of_sendMessage(blue+"Hotfix: "+white+of_isHotfix());
		Sys.of_sendMessage(blue+"Systemarea: "+white+systemArea);
		Sys.of_sendMessage(blue+"Invoker: "+white+invoker);
		Sys.of_sendMessage(blue+"Error message:");
		Sys.of_sendMessage(red+errorMessage);
		Sys.of_sendMessage("Time: "+new SimpleDateFormat("HH:mm:ss").format(new Date()).toString());
		Sys.of_sendMessage("=====================================");
		
		if(exception != null) 
		{
			Sys.of_sendMessage("[Auto-generated exception]:");
			Sys.of_sendMessage(exception.getMessage());
		}
	}
	
	/***************************************/
	/* ADDER // SETTER // REMOVER */
	/***************************************/
	
	//	Zu einem String-Array einen Wert hinzufügen.
	public static String[] of_addArrayValue(String[] myArray, String addValue) 
	{
		if(myArray != null) 
		{
			int size = myArray.length;
			String[] tmpArray = new String[size+1];

			//	ArrayCopy :)
			System.arraycopy(myArray, 0, tmpArray, 0, size);
			
			tmpArray[size] = addValue;
			
			return tmpArray;
		}
		
		return new String[] {addValue};
	}
	
	//	Von einem String-Array einen Wert entfernen.
	public static String[] of_removeArrayValue(String[] myArray, String removeValue) 
	{
		if(myArray != null && myArray.length > 0) 
		{
			String[] newArray = new String[ myArray.length - 1];

			//	Sicherstellen, dass wir min. 2 Einträge haben und nur 1x entfernen!
			if(newArray.length != 0)
			{
				for(int i = 0; i < myArray.length; i++)
				{
					if(!myArray[i].equals(removeValue))
					{
						newArray[i] = myArray[i];
					}
				}

				return newArray;
			}
		}
		
		return new String[0];
	}
	
	public static void of_setDebugMode(boolean lb_bool) 
	{
		if(lb_bool) 
		{
			of_sendDebugMessages2Console();
		}
		
		ib_debug = lb_bool;
	}

	/* GETTER */
	/***************************************/
	
	public static ArrayList<String> of_getReplacedArrayList(ArrayList<String> list, String search, String replace) 
	{
		ArrayList<String> tmpList = new ArrayList<String>();
		
		if(list != null) 
		{
			for(String value : list) 
			{
				tmpList.add(value.replace(search, replace));
			}			
		}

		return tmpList;
	}
	
	public static String[] of_getReplacedArrayString(String[] myArray, String searchValue, String replaceValue) 
	{
		if(myArray != null) 
		{
			for(int i = 0; i < myArray.length; i++) 
			{
				myArray[i] = myArray[i].replace(searchValue, replaceValue);
			}
		}
		
		return myArray;
	}
	
	public static String of_getStringWithoutPlaceholder(String playerHolder, String symbole, String searchString) 
	{
		String[] placeHolderFragments = playerHolder.split(symbole, 3);
				
		if(placeHolderFragments.length == 3) 
		{
			searchString = searchString.replace(placeHolderFragments[0], "").replace(placeHolderFragments[2], "");
		}
		
		return searchString;
	}
	
	//	Integer in 100, 1000 etc. Schritte unterteilen.
	public static String of_getInt2MoneyString(double money) 
	{
		StringBuilder tmp = new StringBuilder();
		Iterable<String> splitStr = Splitter.fixedLength(3).split(new StringBuilder(""+money).reverse().toString());
		
		for(String key : splitStr) 
		{
			if(!tmp.toString().equals(""))
			{
				tmp.append(".").append(key);
			}
			else 
			{
				tmp.append(key);
			}
		}
		
		return new StringBuilder(tmp.toString()).reverse().toString();
	}
	
	public static String of_getNormalizedString(String string) 
	{
		//	Regex?
		// '§'
		string = string.replace("§a", "");
		string = string.replace("§b", "");
		string = string.replace("§c", "");
		string = string.replace("§d", "");
		string = string.replace("§e", "");
		string = string.replace("§f", "");
		string = string.replace("§0", "");
		string = string.replace("§1", "");
		string = string.replace("§2", "");
		string = string.replace("§3", "");
		string = string.replace("§4", "");
		string = string.replace("§5", "");
		string = string.replace("§6", "");
		string = string.replace("§7", "");
		string = string.replace("§8", "");
		string = string.replace("§9", "");
		
		//	'&'
		string = string.replace("&a", "");
		string = string.replace("&b", "");
		string = string.replace("&c", "");
		string = string.replace("&d", "");
		string = string.replace("&e", "");
		string = string.replace("&f", "");
		string = string.replace("&0", "");
		string = string.replace("&1", "");
		string = string.replace("&2", "");
		string = string.replace("&3", "");
		string = string.replace("&4", "");
		string = string.replace("&5", "");
		string = string.replace("&6", "");
		string = string.replace("&7", "");
		string = string.replace("&8", "");
		string = string.replace("&9", "");
		
		//	'AE'
		string = string.replace("ä", "ae");
		string = string.replace("ü", "ue");
		string = string.replace("ö", "oe");
		string = string.replace("Ä", "AE");
		string = string.replace("Ü", "UE");
		string = string.replace("Ö", "OE");
		
		//	'§'
		string = string.replace(")", "");
		string = string.replace("(", "");
		string = string.replace("]", "");
		string = string.replace("[", "");
		
		string = string.replace("§", "");
		string = string.replace("&", "");
		string = string.replace(" ", "");

		return string;
	}
	
	//	Runden
	public static double of_getRoundedDouble(double value, int places)
	{
	    if (places < 0) 
	    {
	    	return -1;
	    }

	    long factor = (long) Math.pow(10, places);
	    value = value * factor;
	    long tmp = Math.round(value);
	    
	    return (double) tmp / factor;
	}
	
	public static int of_getString2Int(String string2Integer) 
	{
		int rc = -1;
		
		try
		{
			rc = Integer.parseInt(string2Integer);
		}
		catch (Exception ignore) { }
		
		return rc;
	}
	
	public static String of_getTimeStamp(boolean withDate, String dateFormat, String hourFormat) 
	{
		String timeStamp = new SimpleDateFormat(hourFormat).format(new Date()).toString();
		
		if(withDate) 
		{
			timeStamp = new SimpleDateFormat(dateFormat).format(new Date()).toString() + " " +  timeStamp;
		}
		
		return timeStamp;
	}
	
	public static String of_getTimeStamp(boolean withDate) 
	{
		return of_getTimeStamp(withDate, "dd.MM.yyyy", "HH:mm:ss");
	}
	
	public static String of_getPaket() 
	{
		return paket;
	}
	
	public static String of_getVersion() 
	{
		return version;
	}
	
	public static String of_getProgrammVersion()
	{
		return programmVersion;
	}
	
	public static String of_getMainFilePath() 
	{
		return mainRootPath;
	}

	/* BOOLS */
	/***************************************/
	
	public static boolean of_check4SpecificPluginOnServer(String pluginName) 
	{
		if(Bukkit.getPluginManager().getPlugin(pluginName) != null) 
		{
			return true;
		}
		
		Sys.of_sendWarningMessage("The plugin '"+pluginName+"' couldn't be found on this server. "+pluginName+"-Function has been disabled for this runtime/uptime only!");
		return false;
	}
	
	public static boolean of_isHotfix() 
	{
		return ib_hotfix;
	}
	
	public static boolean of_isDebugModeEnabled() 
	{
		return ib_debug;
	}
}
