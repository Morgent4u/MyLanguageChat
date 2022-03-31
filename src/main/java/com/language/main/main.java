package com.language.main;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import com.language.boards.MessageService;
import com.language.cmds.CMD_Language;
import com.language.events.ue_spieler;
import com.language.extern.MySQL;
import com.language.objects.Settings;
import com.language.objects.Translation;
import com.language.objects.Vault;
import com.language.objects.Webservice;
import com.language.spieler.SpielerService;
import com.language.sys.Sys;

/**
 * @Created 22.03.2022
 * @Author Nihar
 * @Description
 * This class is an extended version of the JavaPlugin and
 * contains for this plugin important functions and instances.
 */
public class main extends JavaPlugin
{
	//	Instanz-Variabeln des Systems/Plugins.
	public static Plugin PLUGIN;
	
	public static MySQL SQL;
	public static Vault VAULT;
	public static Webservice WEBSERVICE;
	public static Translation TRANSLATION;
	public static SpielerService SPIELERSERVICE;
	public static MessageService MESSAGESERVICE;
	public static Settings SETTINGS;

	/* ************************* */
	/* ENABLE */
	/* ************************* */

	/**
	 * This function is called when the plugin is getting enabled.
	 * This function contains a version check which is used to enable the plugin if
	 * the 'version.yml' and the plugin-version are compatible.
	 * <b>The plugin can be disabled when the connection to the webservice is not given!</>
	 */
	@Override
	public void onEnable() 
	{
		//	Initialisierungen:
		PLUGIN = this;
		
		//	Überprüfen ob die Versions-Nummer stimmt...
		boolean lb_continue = Sys.of_isSystemVersionCompatible(PLUGIN.getName(), "22.1.1.01", "plugins");
		
		if(lb_continue) 
		{
			//	Settings-Variabel initialisieren für den Zugriff z.B. für die Überprüfung des Webservices!
			SETTINGS = new Settings(Sys.of_getMainFilePath());
			
			Sys.of_debug("[WebService]: Send request to webservice...");
			
			//	WebService initialisieren...
			WEBSERVICE = new Webservice();
			int rc = WEBSERVICE.of_checkConnection4WebService();
			
			if(rc != 1) 
			{
				Sys.of_debug("[WebService]: An error occurred while requesting the webservice!");
				WEBSERVICE = null;
			}
			else 
			{
				Sys.of_debug("[WebService]: Successfully connected to the webservice!");
			}
			
			//	Wenn die Verbindung zum Webservice erfolgreich war!
			if(rc == 1) 
			{
				//	Settings laden...
				rc = SETTINGS.of_load();
				
				if(rc == 1) 
				{
					//	Event und Befehle anmelden...
					Bukkit.getPluginManager().registerEvents(new ue_spieler(), this);
					getCommand("Language").setExecutor(new CMD_Language());
					
					//	Als letztes die Instanz-Variabeln der Services, Objekte initialisieren.
					of_initSystemServices();
					
					//	Statusbericht an die Konsole:
					of_printStatusReport2Console();	
				}
				//	Plugin nicht laden oder Fehler bei der DB-Verbindung.
				else 
				{
					Sys.of_sendWarningMessage("System has been disabled by 'settings.yml' or no database connection.");
					Bukkit.getPluginManager().disablePlugin(this);
				}
			}
			//	Keine Verbindung zum Webservice!
			else 
			{
				Sys.of_sendErrorMessage(null, "Main", "onEnable();", "Cannot connect to the webservice!");
				Sys.of_sendWarningMessage("System has been disabled! There is no connection to the webservice!");
				Bukkit.getPluginManager().disablePlugin(this);
			}
		}
	}

	/* ************************* */
	/* DISABLE */
	/* ************************* */

	@Override
	public void onDisable() 
	{
		//	Spielerdaten speichern...
		if(SPIELERSERVICE != null)
		{
			SPIELERSERVICE.of_unload();
		}

		if(SETTINGS != null && SETTINGS.of_isUsingMySQL() && SQL != null && SQL.of_isConnected()) 
		{
			SQL.of_closeConnection();
		}
		
		if(WEBSERVICE != null) 
		{
			WEBSERVICE.of_unload();
		}

		//	Ende.
		Sys.of_sendMessage("Thanks for using this plugin!");
	}

	/* ************************* */
	/* OBJEKT-ANWEISUNGEN */
	/* ************************* */

	/**
	 * This function initialize objects which are required for this plugin!
	 */
	private static void of_initSystemServices() 
	{			
		//	Das Objekt für die Übersetzungen initialisieren...
		TRANSLATION = new Translation(SETTINGS.of_getChatDesign());
		
		//	SpielerService initialisieren...
		SPIELERSERVICE = new SpielerService();
		SPIELERSERVICE.of_load();
		
		//	Message-Service initlaisieren:
		MESSAGESERVICE = new MessageService(Sys.of_getMainFilePath()+"others//messagesSounds.yml");
		MESSAGESERVICE.of_load();
		
		//	Im Anschluss schauen, ob noch andere Komponenten gefordert sind und ob diese zur Verfügung stehen.
		of_checkExternComponents();
	}

	/**
	 * This function checks if external components are registered.
	 * For example: if the SETTINGS-object is using the PlaceholderAPI this function
	 * checks if the plugin is on the server.
	 */
	private static void of_checkExternComponents() 
	{
		//	Schauen ob die PlaceholderAPI verwendet wird...
		if(SETTINGS.of_isUsingPlaceholderAPI()) 
		{
			Sys.of_debug("Search for PlaceholderAPI on this server...");
			
			//	Überprüfen ob die API auf dem Server ist...
			if(Sys.of_check4SpecificPluginOnServer("PlaceholderAPI")) 
			{
				Sys.of_debug("...search complete! This server is using PlaceholderAPI (including Vault) :)");
				SETTINGS.of_setUseVault(false);
			}
			else 
			{
				Sys.of_debug("...search complete! PlaceholderAPI is not on this server :/");
				Sys.of_debug("Now we looking for: Vault (ignore settings.yml)");
				SETTINGS.of_setUseVault(true);
			}
		}
		
		//	Vault-Objekt initialisieren, wenn VAULT vorhanden/gewünscht :)
		if(SETTINGS.of_isUsingVault()) 
		{
			Sys.of_debug("Search for Vault on this server...");
			
			//	Überprüfen ob Vault vorhanden ist...
			if(Sys.of_check4SpecificPluginOnServer("Vault")) 
			{
				Sys.of_debug("...search complete! This server is using Vault :)");
				VAULT = new Vault();
				VAULT.of_load();
			}
			else 
			{
				Sys.of_debug("...search complete! Vault is not on this server :/");
				SETTINGS.of_setUseVault(false);
			}
		}
	}

	/* ************************* */
	/* SONSTIGES */
	/* ************************* */

	/**
	 * This function sends a status report to the console after
	 * successfully loading all objects or required methods for the plugin.
	 */
	private static void of_printStatusReport2Console() 
	{
		//	Farbcodes
		String red = "\u001B[31m";
		String white = "\u001B[0m";
		String green = "\u001B[32m";
		String yellow = "\u001B[33m";
		String purple = "\u001B[35m";
		String blue = "\u001B[36m";
		
		Sys.of_sendMessage("┏╋━━━━━━━━◥◣◆◢◤━━━━━━━━╋");
		if(Sys.of_isHotfix()) 
		{
			Sys.of_sendMessage(red+"[Hotfix: "+green+Sys.of_getPaket()+" "+yellow+"v"+Sys.of_getVersion()+red+"]"+white);
		}
		else 
		{
			Sys.of_sendMessage(red+"["+green+Sys.of_getPaket()+" "+yellow+"v"+Sys.of_getVersion()+red+"]"+white);
		}
		Sys.of_sendMessage("Developed by:");
		Sys.of_sendMessage("»"+purple+" Nihar"+white);
		Sys.of_sendMessage(blue+"▶ Settings:"+white);
		SETTINGS.of_sendDebugDetailInformation();
		Sys.of_sendMessage(blue+"▶ Webservice:"+white);
		Sys.of_sendMessage("Connection: successfully");
		Sys.of_sendMessage(blue+"▶ Message/Sound service:"+white);
		Sys.of_sendMessage("Loaded messages/sounds: " + MESSAGESERVICE.of_getMessageCount());
		Sys.of_sendMessage("┗╋━━━━━━━━◥◣◆◢◤━━━━━━━━╋┛");
		
		//	Debug bzgl. den Sprachen ausgeben...
		SETTINGS.of_sendDebug4Languages();
	}
}
