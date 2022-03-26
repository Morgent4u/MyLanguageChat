package com.language.spieler;

import java.util.Collection;
import java.util.HashMap;

import com.language.main.main;
import org.bukkit.entity.Player;
import com.language.sys.Sys;
import com.language.utils.Datei;

public class SpielerContext
{
	/*	Angelegt am: 21.03.2022
	 * 	Erstellt von: Nihar
	 * 	Beschreibung:
	 * 	Hier wird f�r jeden Spieler eine Instanz erstellt.
	 * 	Zus�tzlich wird hier zwischen DB und FileSystem unterschieden.
	 * 	
	 */
	
	//	Attribute:
	private HashMap<String, Spieler> players = new HashMap<String, Spieler>();
	
	/***************************************/
	/* CONSTRUCTOR */
	/***************************************/
	
	public SpielerContext() { }
	
	/***************************************/
	/* LOADER */
	/***************************************/
	
	public void of_loadPlayer(Player p) 
	{
		if(!players.containsKey(p.getName())) 
		{
			Spieler ps = new Spieler(p);
			String uuid = p.getUniqueId().toString();
			String defaultLanguage = null;
			int dbId = -1;
			
			if(main.SETTINGS.of_isUsingMySQL()) 
			{
				//	Gibt es den Spieler bereits?
				String sqlStatement = "SELECT user FROM mlc_user WHERE uuid = '"+uuid+"';";
				dbId = Sys.of_getString2Int(main.SQL.of_getRowValue_suppress(sqlStatement, "user"));
				
				if(dbId == -1) 
				{
					//	Neuen Nutzer anlegen...
					dbId = of_createNewPlayerEntry2Database(p);
				}
				
				//	Spieler-Inhalte laden...
				if(dbId != -1) 
				{
					String sqlSelect = "SELECT defaultLanguage FROM mlc_user WHERE user = "+dbId+";";
					defaultLanguage = main.SQL.of_getRowValue_suppress(sqlSelect, "defaultLanguage");
				}
				//	DB-System auf File-System umstellen...
				else 
				{
					//	Verbindung beenden...
					main.SETTINGS.of_setUseMySQL(false);
					main.SQL.of_closeConnection();
					
					//	Switch zum FileSystem
					of_loadPlayer(p);
				}
			}
			//	File-System:
			else 
			{
				//	Spieler-Datei laden/erstellen
				Datei userdata = new Datei(Sys.of_getMainFilePath()+"userdata//"+uuid+".yml");
				userdata.of_set("Name", p.getName());
				
				//	Standard-Sprache ermitteln oder setzen...
				defaultLanguage = userdata.of_getSetString("Language", main.SETTINGS.of_getDefaultLanguage());
				
				//	Interne SpielerID vergeben...
				dbId = players.size() + 1;
				userdata.of_save("SpielerContext.of_loadPlayer(Player);");
			}
			
			//	Zur Spieler-Instanz alle Attribute setzen.
			if(defaultLanguage == null) 
			{
				defaultLanguage = main.SETTINGS.of_getDefaultLanguage();
			}
			
			ps.of_setDefaultLanguage(defaultLanguage);
			ps.of_setTargetId(dbId);
			players.put(p.getName(), ps);
		}
	}
	
	/***************************************/
	/* OBJEKT-ANWEISUNGEN */
	/***************************************/
	
	//	Neuen Datenbank-Spieler anlegen...
	private int of_createNewPlayerEntry2Database(Player p) 
	{
		int dbId = main.SQL.of_updateKey("mlc_user");
		
		if(dbId > 0) 
		{
			String sqlInsert = "INSERT INTO mlc_user( user, name, uuid, defaultLanguage ) VALUES( "+dbId+", '"+p.getName()+"', '"+p.getUniqueId().toString()+"', '"+main.SETTINGS.of_getDefaultLanguage().toLowerCase()+"' );";
			boolean bool = main.SQL.of_run_update(sqlInsert);
			
			if(bool) 
			{
				return dbId;
			}
		}
		
		return -1;
	}
	
	public int of_savePlayer(Spieler ps) 
	{
		if(ps != null) 
		{
			//	Datenbank oder FileSystem?
			if(main.SETTINGS.of_isUsingMySQL()) 
			{
				//	Update-Statement:
				String sqlUpdate = "UPDATE mlc_user SET name = '"+ps.of_getName()+"', defaultLanguage = '"+ps.of_getDefaultLanguage()+"' WHERE mlc_user.user = " + ps.of_getTargetId() + ";";
				boolean bool = main.SQL.of_run_update_supress(sqlUpdate);
				
				//	Bei einem Fehler, wechsel zum FileSystem!
				if(!bool) 
				{
					//	Verbindung beenden...
					main.SETTINGS.of_setUseMySQL(false);
					main.SQL.of_closeConnection();
					
					//	Switch zum FileSystem
					return of_savePlayer(ps);
				}
				
				return 1;
			}
			//	FileSystem
			else 
			{
				//	Spieler-Datei laden/erstellen
				Datei userdata = new Datei(Sys.of_getMainFilePath()+"userdata//"+ps.of_getUUID()+".yml");
				
				userdata.of_set("Name", ps.of_getName());
				userdata.of_set("Language", ps.of_getDefaultLanguage());
				userdata.of_save("SpielerContext.of_savePlayer(String);");
				return 1;
			}
		}
		
		return -1;
	}
	
	public int of_savePlayer(String playerName) 
	{
		return of_savePlayer(players.get(playerName));
	}
	
	/***************************************/
	/* GETTER */
	/***************************************/
	
	public Collection<Spieler> of_getAllSpieler() 
	{
		return players.values();
	}
	
	public Spieler of_getSpieler(String playerName) 
	{
		return players.get(playerName);
	}
}
