package com.language.extern;

import com.language.ancestor.Objekt;
import com.language.main.main;
import com.language.sys.Sys;

public class UPDService extends Objekt
{
	/*	Angelegt am: 20.03.2022
	 * 	Erstellt von: Nihar
	 * 	Beschreibung:
	 * 	Mit dem UPD-Service wird die Datenbank
	 * 	auf die Version des Plugins angehoben.
	 * 	Dabei werden alle SQL-Statements in dieser Klasse
	 * 	gesammelt und ggf. ausgef�hrt, wenn die DB-Version noch
	 * 	nicht soweit ist.
	 * 	
	 */	
	
	//	Attribute:
	
	String lastUPDVersion;
	
	int executedSQLs;
	int errorSQLs;
	int skippedSQLs;
	
	int highestUPDVersionNumber;
	int currentPluginVersionNumber;
	
	/***************************************/
	/* CONSTRUCTOR */
	/***************************************/
	
	public UPDService() {}
	
	/***************************************/
	/* SQL-Statements */
	/***************************************/
	
	public int of_runUPD() 
	{
		//	DB-Version aktualisieren.
		of_updateVersionNumber2Database();
		
		String sqlStatement = "DROP TABLE IF EXISTS mlc_user;";
		int rc = of_run_statement("22.1.0.01", sqlStatement);
		
		if(rc > -1) 
		{
			sqlStatement = "DROP TABLE IF EXISTS mlc_keys;";
			rc = of_run_statement("22.1.0.01", sqlStatement);
			
			if(rc > -1) 
			{
				sqlStatement = "CREATE TABLE mlc_user( user INT NOT NULL PRIMARY KEY, name VARCHAR(32) NOT NULL, uuid VARCHAR(42) NOT NULL, defaultLanguage VARCHAR(4) NOT NULL );";
				rc = of_run_statement("22.1.0.01", sqlStatement);
				
				if(rc > -1) 
				{
					sqlStatement = "CREATE TABLE mlc_keys ( tablename VARCHAR(40) NOT NULL, lastkey INT DEFAULT 0 );";
					rc = of_run_statement("22.1.0.01", sqlStatement);
					
					if(rc > -1) 
					{
						sqlStatement = "INSERT INTO mlc_keys ( tablename ) VALUES( 'mlc_user' );";
						rc = of_run_statement("22.1.0.01", sqlStatement);
						
						if(rc > -1) 
						{
							sqlStatement = "CREATE TABLE mlc_dbversion( dbversion VARCHAR(20) NOT NULL );";
							rc = of_run_statement("22.1.0.01", sqlStatement);
							
							if(rc > -1) 
							{
								//	DB-Version eintragen!
								sqlStatement = "INSERT INTO mlc_dbversion( dbversion ) VALUES( '"+Sys.of_getVersion()+"' );";
								rc = of_run_statement("22.1.0.01", sqlStatement);
								
								if(rc > -1) 
								{
									/*
									sqlStatement = "UPDATE mlc_dbversion SET dbversion = '"+Sys.of_getVersion()+"';";
									rc = of_run_statement("22.1.0.02", sqlStatement);
									*/
									
									return 1;
								}
							}							
						}
					}
				}
			}
		}
		
		return -1;
	}
	
	public int of_run_statement(String version, String sqlStatement) 
	{
		//	RC:
		//	 1: OK
		//	 0: Skip
		//	-1: Error
		
		String[] stmtVersion = version.split("\\.");
		
		if(stmtVersion != null && stmtVersion.length == 4) 
		{
			//	Nummer innerhalb des Grenzbereichs (im besten Fall)
			int versionNumber = Integer.valueOf(stmtVersion[3]);
			
			//	Schauen ob die Nummer im Grenzbereich ist, wenn JA Statement ausf�hren...
			if(highestUPDVersionNumber < versionNumber && versionNumber <= currentPluginVersionNumber) 
			{
				if(main.SQL.of_run_update_supress(sqlStatement))
				{
					Sys.of_debug("UPD-Success");
					executedSQLs++;
					return 1;
				}
				
				Sys.of_debug("UPD-Error");
				errorSQLs++;
				return -1;
			}
		}
		
		skippedSQLs++;
		return 0;
	}
	
	public int of_updateVersionNumber2Database() 
	{
		//	Wenn die UPD-Version 21.1.0.00 ist, braucht die Aktualisierung nicht erfolgen...
		if(lastUPDVersion.equals("22.1.0.00")) 
		{
			return 1;
		}
		
		String sqlStatement = "UPDATE mlc_dbversion SET dbversion = '"+Sys.of_getVersion()+"';";
		boolean bool = main.SQL.of_run_update_supress(sqlStatement);
		
		if(bool) 
		{
			Sys.of_debug("UPD-Success");
			executedSQLs++;
		}
		else 
		{
			Sys.of_debug("UPD-Error");
			errorSQLs++;
			return -1;
		}
		
		return 1;
	}
	
	/***************************************/
	/* GETTER */
	/***************************************/
	
	public int of_getExecutedSQLs() 
	{
		return executedSQLs;
	}
	
	public int of_getSkippedSQLs() 
	{
		return skippedSQLs;
	}
	
	public int of_getErrorSQLs() 
	{
		return errorSQLs;
	}
	
	/***************************************/
	/* BOOLS */
	/***************************************/
	
	public boolean of_canRunUPD() 
	{
		String sqlSelect = "SELECT dbversion FROM mlc_dbversion;";
		String result = main.SQL.of_getRowValue_suppress(sqlSelect, "dbversion");
		
		if(result != null) 
		{
			//	Wenn die DB-Version gleich mit der PL-Verison ist, gibt es kein UPD zum einspielen!
			if(result.equals(Sys.of_getVersion())) 
			{
				return false;
			}
			//	Letzte UPD-Version...
			else 
			{
				lastUPDVersion = result;
			}
		}
		//	Grund-Version...
		else 
		{
			lastUPDVersion = "22.1.0.00";
		}
		
		//	Den Grenzbereihc ermitteln, von welcher Versionsnummer bis zur welcher Versionsnummer m�ssen die UPDs eingespielt werden?
		String[] pluginVersion = Sys.of_getVersion().split("\\.");
		String[] updVersion = lastUPDVersion.split("\\.");
		
		//	Sicherstellen, dass alle Angaben korrekt sind.
		if(pluginVersion[0] != null && updVersion != null && pluginVersion.length == 4 && updVersion.length == 4) 
		{
			//	Die ersten beiden Ziffern m�ssen identisch sein!
			if(pluginVersion[0].equals(updVersion[0]) && pluginVersion[1].equals(updVersion[1])) 
			{
				//	Grenzbereich ermitteln:
				currentPluginVersionNumber = Integer.valueOf(pluginVersion[3]);
				highestUPDVersionNumber = Integer.valueOf(updVersion[3]);
			}
		}
		
		return true;
	}
}
