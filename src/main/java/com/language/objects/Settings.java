package com.language.objects;

import java.util.HashMap;

import com.language.ancestor.Objekt;
import com.language.extern.MySQL;
import com.language.main.main;
import com.language.sys.Sys;
import com.language.utils.Datei;

public class Settings extends Objekt
{
	//	Attribute:
	HashMap<String, String> supportedLanguages = new HashMap<String, String>();
	Datei datei;
	
	String defaultLangauge;
	String chatDesign;
	String noSeparateChat_defaultLanguage;
	String chatTranslateSymbole;
	
	boolean ib_useLanguage;
	boolean ib_useVault;
	boolean ib_usePlaceholderAPI;
	boolean ib_useOwnWebservice;
	boolean ib_useSeparateChat;
	boolean ib_useMySQL;
	boolean ib_useChatTranslateSymbole;
	boolean ib_translateEveryMessage2DefaultLanguage;
	
	/***************************************/
	/* CONSTRUCTOR */
	/***************************************/
	
	public Settings(String directoryPath) 
	{
		datei = new Datei(directoryPath+"settings.yml");
	}
	
	/***************************************/
	/* LOADER // UNLOADER */
	/***************************************/
	
	@Override
	public int of_load() 
	{
		//	RC:
		//	 1: OK
		//	 0: Nicht aktivieren.
		//	-1: Fehler
		
		int rc = -1;
		String sectionKey = Sys.of_getPaket();
		
		//	Settings:
		ib_useLanguage = datei.of_getSetBoolean(sectionKey + ".Enabled", true);
		
		if(ib_useLanguage) 
		{
			//	Settings:
			defaultLangauge = datei.of_getSetString(sectionKey + ".Settings.DefaultLanguage", "EN").toLowerCase();
			ib_useVault = datei.of_getSetBoolean(sectionKey + ".Settings.UseVault", true);
			ib_usePlaceholderAPI = datei.of_getSetBoolean(sectionKey + ".Settings.UsePlaceholderAPI", false);
			ib_useOwnWebservice = datei.of_getSetBoolean(sectionKey + ".Settings.OwnWebservice", false);
			
			//	Chat:
			chatDesign = datei.of_getSetString(sectionKey + ".Chat.Design", "&8[&c%group%&8]&a %p%&7: &f%message%").replace("&", "�");			
			ib_useSeparateChat = !datei.of_getSetBoolean(sectionKey + ".Chat.GlobalTranslatedChat.Use", false);
			noSeparateChat_defaultLanguage = datei.of_getSetString(sectionKey+".Chat.GlobalTranslatedChat.DefaultLanguage", "EN").toLowerCase();
			
			//	Translations:
			chatTranslateSymbole = datei.of_getSetString(sectionKey + ".Translation.OnlyTranslateBySymbole", "none");
			ib_useChatTranslateSymbole = !chatTranslateSymbole.equals("none");
			ib_translateEveryMessage2DefaultLanguage = datei.of_getSetBoolean(sectionKey+".Translation.TranslateEveryMessage2UserLanguage", false);
			String[] supportedLanguages = new String[] {"EN - English", "NL - Netherlands", "FI - Finnland", "FR - France", "DE - Germany", "GR - Greece", "HU - Hungary", "IE - Ireland", "IT - Italy", "JP - Japan", "PL - Poland", "PT - Portugal", "RU - Russia", "ES - Spain", "SE - Sweden", "TR - Turkey", "UA - Ukraine"};
			supportedLanguages = datei.of_getSetStringArray(sectionKey + ".Translation.Languages", supportedLanguages);
			
			//	L�nderk�rzel bzw. L�ndercode und L�nderbezeichnung zusammen in auf eine HashMap werfen...
			of_normalizeLanguageArray4HashMapEntries(supportedLanguages);
			
			//	MySQL-Attribute einlesen:
			ib_useMySQL = datei.of_getSetBoolean(sectionKey+".MySQL.Use", false);
			String hostName = datei.of_getSetString(sectionKey + ".MySQL.Host", "localhost");
			String database = datei.of_getSetString(sectionKey + ".MySQL.Database", "database");
			String username = datei.of_getSetString(sectionKey + ".MySQL.Username", "user");
			String password = datei.of_getSetString(sectionKey + ".MySQL.Password", "pwd");
			
			datei.of_save("Settings.of_load();");

			//	Wenn MySQL verwendet werden soll, Instanz an der Main-Klasse initialisieren.
			if(ib_useMySQL) 
			{
				//	SQL-Instanz erzeugen!
				main.SQL = new MySQL("Main");
				
				//	Attribute zur DB setzen und Verbindung herstellen!
				main.SQL.of_setServer(hostName);
				main.SQL.of_setDbName(database);
				main.SQL.of_setUserName(username);
				main.SQL.of_setPassword(password);
				main.SQL.of_setUpdateKeyTableAndColumns("mlc_keys", "lastkey", "tablename");
				
				//	Verbindung zur DB herstellen und ggf. UPDSrv ansto�en!
				rc = main.SQL.of_createConnection();
				
				//	DB-Zugriff bzw. Verbindung in die Settings.yml schreiben...
				if(rc == 1) 
				{
					datei.of_set(sectionKey + ".MySQL.Status", Sys.of_getTimeStamp(true) + " - Connected.");
				}
				else 
				{
					datei.of_set(sectionKey + ".MySQL.Status", Sys.of_getTimeStamp(true) + " - No connection.");
				}
				
				datei.of_save("Settings.of_load();");
			}
			else 
			{
				rc = 1;
			}
		}
		else 
		{
			rc = 0;
		}
		
		return rc;
	}
	
	/***************************************/
	/* OBJEKT-ANWEISUNGEN */
	/***************************************/
	
	public void of_normalizeLanguageArray4HashMapEntries(String[] languageArray) 
	{
		if(languageArray != null && languageArray.length > 0) 
		{
			for(int i = 0; i < languageArray.length; i++) 
			{
				String[] splittedFragments = languageArray[i].split("-");
				
				//	Sicherstellen, dass alle Angaben passen...
				if(splittedFragments != null && splittedFragments.length == 2) 
				{
					supportedLanguages.put(splittedFragments[0].toLowerCase().trim(), splittedFragments[1].trim());
				}
				//	ggf. hat der Anwender was am Design nicht verstanden, des wegen dennoch das L�nderk�rzel ermitteln...
				else if(splittedFragments != null && splittedFragments.length == 1) 
				{
					supportedLanguages.put(splittedFragments[0].toLowerCase().trim(), "No countryname");
				}
				else 
				{
					Sys.of_sendErrorMessage(null, "Settings", "of_normalizeLanguageArray4HashMapEntries(String-Array)", "There was an error by parsing the line '"+i+"' in the 'settings.yml' for the section 'Languages'.");
					break;
				}
			}
		}
	}
	
	/***************************************/
	/* DEBUG-CENTER */
	/***************************************/
	
	@Override
	public void of_sendDebugDetailInformation() 
	{
		Sys.of_sendMessage("MyLanguageChat-Enabled: "+of_isUsingLanguage());
		Sys.of_sendMessage("DefaultLanguage: "+of_getDefaultLanguage());
		Sys.of_sendMessage("UseVault-Enabled: "+of_isUsingVault());
		Sys.of_sendMessage("UsePlaceholderAPI-Enabled: "+ of_isUsingPlaceholderAPI());
		Sys.of_sendMessage("OwnWebservice-Enabled: "+of_isUsingOwnWebservice());
		Sys.of_sendMessage("Chat-Design: "+of_getChatDesign().replace("�", "&"));
		Sys.of_sendMessage("OnlyTranslateBySymbole: "+of_isUsingChatSymbole());
		Sys.of_sendMessage("OnlyTranslateBySymbole-Symbole: "+of_getChatTranslateSymbole());
		Sys.of_sendMessage("GlobalTranslatedChat: "+!of_isUsingSeperateChats());
		Sys.of_sendMessage("GlobalTranslatedChat-DefaultLanguage: "+of_getDefaultLanguage4NoSperatechat());
		Sys.of_sendMessage("TranslateEveryMessage2UserLanguage: "+of_isUsingTranslateEveryMessage2UserLanguage());
		Sys.of_sendMessage("MySQL-Enabled: "+of_isUsingMySQL());
		
		if(main.SQL != null) 
		{
			Sys.of_sendMessage("MySQL-Connected: "+main.SQL.of_isConnected());
		}
	}
	
	//	Debug bzgl. den geladenenen Sprachen ausgeben...
	public void of_sendDebug4Languages() 
	{
		Sys.of_debug("Loaded languages with countrycode ("+supportedLanguages.size()+"):");
		
		for(String countryCode : supportedLanguages.keySet()) 
		{
			Sys.of_debug("Countrycode: " + countryCode.toUpperCase() + " | Language: " + supportedLanguages.get(countryCode));
		}
	}
	
	/***************************************/
	/* SETTER */
	/***************************************/
	
	public void of_setUseMySQL(boolean bool) 
	{
		ib_useMySQL = bool;
	}
	
	public void of_setUseVault(boolean bool) 
	{
		ib_useVault = bool;
	}
	
	public void of_setPlugin(boolean bool) 
	{
		ib_useLanguage = bool;
	}
	
	public void of_setDefaultLanguage(String defaultLanguage) 
	{
		this.defaultLangauge = defaultLanguage;
	}
	
	public void of_setTranslationSymbole(String symbole) 
	{
		chatTranslateSymbole = symbole;
		ib_useChatTranslateSymbole = ( symbole.toLowerCase().equals("none") ) ? false : true;
	}
	
	public void of_setTranslateEveryMessage2DefaultUserLanguage(boolean bool) 
	{
		ib_translateEveryMessage2DefaultLanguage = bool;
	}
	
	/***************************************/
	/* GETTER */
	/***************************************/
	
	public Datei of_getSettingsFile() 
	{
		return datei;
	}
	
	public HashMap<String, String> of_getSupportedLanguagesWithFullNames()
	{
		return supportedLanguages;
	}
	
	public String[] of_getSupportedLanguages() 
	{
		return supportedLanguages.keySet().toArray(new String[0]);
	}
	
	public String of_getChatTranslateSymbole() 
	{
		return chatTranslateSymbole;
	}
	
	public String of_getDefaultLanguage4NoSperatechat() 
	{
		return noSeparateChat_defaultLanguage;
	}
	
	public String of_getChatDesign() 
	{
		return chatDesign;
	}
	
	public String of_getDefaultLanguage() 
	{
		return defaultLangauge;
	}
	
	/***************************************/
	/* BOOLS */
	/***************************************/
	
	public boolean of_isUsingMySQL() 
	{
		return ib_useMySQL;
	}
	
	public boolean of_isUsingSeperateChats() 
	{
		return ib_useSeparateChat;
	}
	
	public boolean of_isUsingOwnWebservice() 
	{
		return ib_useOwnWebservice;
	}
	
	public boolean of_isUsingVault() 
	{
		return ib_useVault;
	}
	
	public boolean of_isUsingLanguage() 
	{
		return ib_useLanguage;
	}
	
	public boolean of_isUsingChatSymbole() 
	{
		return ib_useChatTranslateSymbole;
	}
	
	public boolean of_isUsingTranslateEveryMessage2UserLanguage() 
	{
		return ib_translateEveryMessage2DefaultLanguage;
	}
	
	public boolean of_isUsingPlaceholderAPI() 
	{
		return ib_usePlaceholderAPI;
	}
}
