package com.language.sys;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import com.language.main.main;
import org.bukkit.Bukkit;
import com.google.common.base.Splitter;
import com.language.utils.Datei;

/**
 * @Created 20.03.2021
 * @Recreated: 22.03.2022
 * @Author Nihar
 * @Description
 * This is the system-class of this plugin.
 * The system-class is used to check compatibility of the
 * current files in the plugins folder and this plugin version.
 * This class also contains useful methods or functions.
 */
public class Sys
{
	//	DebugMessages
	private static ArrayList<String> debugMessagesBuffer = new ArrayList<String>();

	//	Attribute der Systemklasse
	private static String paket;
	private static String programVersion;
	private static String version;
	private static String mainRootPath;
	
	private static boolean ib_hotfix;
	private static boolean ib_debug = true;
	private static int debugCounter;

	/* ************************************* */
	/* ANWEISUNGEN */
	/* ************************************* */

	/**
	 * This function checks the current plugin version with the given version from the
	 * plugins' folder (version.yml). If the version are not compatible the plugin cannot
	 * be used because it's outdated or way newer than the current files. This prevents that
	 * the current files will be destroyed by this plugin-version.
	 * @param paketName Plugin name
	 * @param versionNummer Version-number in the following format (example): 22.1.0.01
	 * @param fileRootPath The main-path for the plugin files (this should be the plugins' folder).
	 * @return TRUE = Compatible, FALSE = Not compatible - disabling the plugin.
	 */
	public static boolean of_isSystemVersionCompatible(String paketName, String versionNummer, String fileRootPath) 
	{
		boolean versionCompatible = false;
		
		//	Paket & Ordner-Hauptverzeichnis bestimmten.
		paket = paketName;
		version = versionNummer;
		programVersion = of_getPaket() + " v"+version;
		mainRootPath = fileRootPath + "//"+paketName+"//";
		
		//	Datei: version.yml erstellen oder Versions-Nummer überprüfen...
		Datei datei = new Datei(mainRootPath + "version.yml");
		
		//	Attribute der Systemversion einholen...
		String oldVersion = datei.of_getSetString("Version", of_getProgramVersion());
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
						of_sendWarningMessage("This plugin-version is newer than your system files. This can possible cause some problems.");
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

	/* ************************************* */
	/* ANWEISUNGEN DER KLASSE */
	/* ************************************* */

	/**
	 * This function sends stored debug-messages (while reloading the server)
	 * to the console after the reload is done.
	 */
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

	/**
	 * This function sends a message to the console with using the default plugin-header.
	 * @param message Message
	 */
	public static void of_sendMessage(String message) 
	{
		main.PLUGIN.getLogger().info(message);
	}

	/**
	 * This function sends a message to the console with using the extended default-plugin header.
	 * After the plugin-header a WARNING-prefix is used.
	 * @param message Message
	 */
	public static void of_sendWarningMessage(String message) 
	{
		String red = "\u001B[31m";
		String white = "\u001B[0m";
		
		main.PLUGIN.getLogger().info(red+"[WARNING]: "+white+message);
	}

	/* ************************************* */
	/* DEBUG CENTER */
	/* ************************************* */

	/**
	 * This function is used for printing messages only to the console.
	 * Every debug-messages gets a following number.
	 * @param message Message (this should be the invoke function or a description what currently happens).
	 */
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

	/* ************************************* */
	/* ERROR HANDLER */
	/* ************************************* */

	/**
	 * This function is used to send a default error message by the system-class.
	 * @param exception Exception (if no exception is given use NULL instead).
	 * @param systemArea The system area or invoker-object which calls this function.
	 * @param invoker The current function/method which is calling this function explicit.
	 * @param errorMessage The error message which will be displayed. <b>This message should be easy to understand for the server owner</b>.
	 */
	public static void of_sendErrorMessage(Exception exception, String systemArea, String invoker, String errorMessage) 
	{
		//	Farbcodes
		String red = "\u001B[31m";
		String white = "\u001B[0m";
		String yellow = "\u001B[33m";
		String blue = "\u001B[36m";
		
		Sys.of_sendMessage("=====================================");
		Sys.of_sendMessage(red+"[ERROR] "+yellow+Sys.of_getProgramVersion()+white);
		Sys.of_sendMessage(blue+"Hotfix: "+white+of_isHotfix());
		Sys.of_sendMessage(blue+"System area: "+white+systemArea);
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

	/* ************************************* */
	/* ADDER // SETTER // REMOVER */
	/* ************************************* */

	/**
	 * This function adds an element to the given string-array.
	 * @param myArray Array of type string.
	 * @param addValue Value which should be added to the given array.
	 * @return Array with the added element-value.
	 */
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

	/**
	 * This function removes an element from the given array.
	 * @param myArray Array of type string.
	 * @param removeValue The value which should be removed from the given array.
	 * @return Array with the removed element-value.
	 */
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

	/**
	 * This function enables the debug-mode. If the debug-mode is disabled the
	 * debug-messages will be stored in an arraylist.
	 * @param lb_bool Use the debug-mode.
	 */
	public static void of_setDebugMode(boolean lb_bool) 
	{
		if(lb_bool) 
		{
			of_sendDebugMessages2Console();
		}
		
		ib_debug = lb_bool;
	}

	/* ************************************* */
	/* GETTER */
	/* ************************************* */

	/**
	 * This function replaced all elements of an arraylist with the given replace value.
	 * @param list ArrayList of type string.
	 * @param search The value which should be replaced in the string-text.
	 * @param replace The replace text which should be replaced.
	 * @return An arraylist with the replaced values.
	 */
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

	/**
	 * This function is similar to the function of_getReplacedArrayList() but it is using an array-string, instead.
	 * @param myArray Array of type string.
	 * @param searchValue Search pattern.
	 * @param replaceValue Replace value.
	 * @return An array with the replaced values.
	 */
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

	/**
	 * This function gets the placeholder value of a replaced string by using the placeholder.
	 * @param playerHolder Placeholder example: '&8[&c%group%&8]'
	 * @param symbol Placeholder start pattern example: '%'
	 * @param searchString String which has already the replaced placeholder. For example: '&8[&cAdmin&8]'
	 * @return The placeholder value of the parameter 'searchString'. For this example is the result: 'Admin'
	 */
	public static String of_getStringWithoutPlaceholder(String playerHolder, String symbol, String searchString)
	{
		String[] placeHolderFragments = playerHolder.split(symbol, 3);
				
		if(placeHolderFragments.length == 3) 
		{
			searchString = searchString.replace(placeHolderFragments[0], "").replace(placeHolderFragments[2], "");
		}
		
		return searchString;
	}

	/**
	 * This function parsed a double to a money-string type with dots.
	 * @param money Money amount in double. For example: 10000
	 * @return A string which contains the double in money format (dots). For this example: '10.000'
	 */
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

	/**
	 * This function is used to normalize a string.
	 * From the input string the default minecraft colors will be removed.
	 * @param string String which should be normalized.
	 * @return Normalized string.
	 */
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

	/**
	 * This function rounds a double by the given places.
	 * @param value Double value. For example: 30.0291
	 * @param places Places value. For example: 2
	 * @return A double with places. For this example: 31.03
	 */
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

	/**
	 * This function parsed a string to an int.
	 * If the string value is not valid the default value -1
	 * will be returned.
	 * @param string2Integer String which contains a number.
	 * @return Int-number or default error-value: -1
	 */
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

	/**
	 * Returns a current timestamp in the given format.
	 * @param withDate Use date
	 * @param dateFormat Date format for example: dd.MM.yyyy
	 * @param hourFormat Hour format for example: HH:mm:ss
	 * @return A timestamp string with the given format.
	 */
	public static String of_getTimeStamp(boolean withDate, String dateFormat, String hourFormat) 
	{
		String timeStamp = new SimpleDateFormat(hourFormat).format(new Date()).toString();
		
		if(withDate) 
		{
			timeStamp = new SimpleDateFormat(dateFormat).format(new Date()).toString() + " " +  timeStamp;
		}
		
		return timeStamp;
	}

	/**
	 * Overload of function of_getTimeStamp(withDate, dateFormat, hourFormat);
	 * @param withDate Use date
	 * @return A timestamp string with the given format.
	 */
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
	
	public static String of_getProgramVersion()
	{
		return programVersion;
	}
	
	public static String of_getMainFilePath() 
	{
		return mainRootPath;
	}

	/* ************************************* */
	/* BOOLS */
	/* ************************************* */

	/**
	 * This function is used to check if a specific plugin is used on the server.
	 * @param pluginName Plugin name.
	 * @return TRUE = Server is using the given plugin. FALSE = Plugin not found.
	 */
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
