package com.language.extern;

import com.language.ancestor.Objekt;
import com.language.main.main;
import com.language.sys.Sys;

/**
 * @Created 20.03.2022
 * @Author Nihar
 * @Description
 * With the upd-service the database will be automatically updated for
 * this plugin version.
 */
public class UPDService extends Objekt
{
	//	Attribute:
	String lastUPDVersion;
	
	int executedSQLs;
	int errorSQLs;
	int skippedSQLs;
	
	int highestUPDVersionNumber;
	int currentPluginVersionNumber;

	/* ****************************** */
	/* CONSTRUCTOR */
	/* ****************************** */

	public UPDService() { }

	/* ****************************** */
	/* OBJECT-ANWEISUNGEN */
	/* ****************************** */

	/**
	 * This function starts updating the database.
	 * @return 1 = SUCCESS, -1 Error
	 */
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

	/**
	 * This function executes the sql-statements which are defined in of_runUPD();
	 * If a version doesn't match with the current-plugin version, the statement will be skipped.
	 * @param version Plugin-Version (DB-Version) which is required for running this sql-statement.
	 * @param sqlStatement SQl-Statement
	 * @return 1 = SUCCESS, 0 = SKIPPED, -1 = ERROR
	 */
	private int of_run_statement(String version, String sqlStatement)
	{
		//	RC:
		//	 1: OK
		//	 0: Skip
		//	-1: Error
		
		String[] stmtVersion = version.split("\\.");
		
		if(stmtVersion.length == 4)
		{
			//	Nummer innerhalb des Grenzbereichs (im besten Fall)
			int versionNumber = Integer.parseInt(stmtVersion[3]);
			
			//	Schauen ob die Nummer im Grenzbereich ist, wenn JA Statement ausf�hren...
			if(highestUPDVersionNumber < versionNumber && versionNumber <= currentPluginVersionNumber) 
			{
				if(main.SQL.of_run_update_suppress(sqlStatement))
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

	/**
	 * This function sets the current plugin-version in the db-version table.
	 */
	public void of_updateVersionNumber2Database()
	{
		//	Wenn die UPD-Version 21.1.0.00 ist, braucht die Aktualisierung nicht erfolgen...
		if(lastUPDVersion.equals("22.1.0.00")) 
		{
			return;
		}
		
		String sqlStatement = "UPDATE mlc_dbversion SET dbversion = '"+Sys.of_getVersion()+"';";
		boolean bool = main.SQL.of_run_update_suppress(sqlStatement);
		
		if(bool) 
		{
			Sys.of_debug("UPD-Success");
			executedSQLs++;
		}
		else 
		{
			Sys.of_debug("UPD-Error");
			errorSQLs++;
		}

	}

	/* ****************************** */
	/* GETTER */
	/* ****************************** */

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

	/* ****************************** */
	/* BOOLS */
	/* ****************************** */

	/**
	 * This function checks if the UPD-Service needs to update the database!
	 * @return TRUE = AN UPDATE is needed!, FALSE = NO UPDATE is needed!
	 */
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
		if(pluginVersion[0] != null && pluginVersion.length == 4 && updVersion.length == 4)
		{
			//	Die ersten beiden Ziffern m�ssen identisch sein!
			if(pluginVersion[0].equals(updVersion[0]) && pluginVersion[1].equals(updVersion[1])) 
			{
				//	Grenzbereich ermitteln:
				currentPluginVersionNumber = Integer.parseInt(pluginVersion[3]);
				highestUPDVersionNumber = Integer.parseInt(updVersion[3]);
			}
		}
		
		return true;
	}
}
