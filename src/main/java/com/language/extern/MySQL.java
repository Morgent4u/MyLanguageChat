package com.language.extern;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.language.main.main;
import com.language.sys.Sys;

public class MySQL
{
	/*	Angelegt am: 03.02.2021
	 * 	Überarbeitet am: 28.11.2021 -> für dieses Projekt :)
	 * 	Erstellt von: Nihar
	 * 	Beschreibung:
	 * 	Verwaltung der DB-Anbindung.
	 * 	
	 */	
	
	Connection con;
	Statement stm = null;

	String white = "\u001B[0m";
	String green = "\u001B[32m";
	
	String instanzName;
	String connectionString;

	String server;
	String dbName;
	String userName;
	String password;
	
	//	Update-Stuff
	String updateKeyTable;
	String updateKeyColumn;
	String updateTableColumn4Key;

	/***************************************/
	/* CONSTRUCTOR // LOADER */
	/***************************************/
	
	public MySQL(String instanzName) 
	{
		this.instanzName = "[MySQL-"+instanzName+"]:";
	}
	
	/***************************************/
	/* OBJEKT - ANWEISUNGEN */
	/***************************************/
	
	//	Anhand aller Angaben ConnectionString bauen und Verbindung
	//	herstellen.
	public int of_createConnection() 
	{
		Sys.of_debug(instanzName + " Try to connect to the following database: " + dbName);
		
		//	Validierung
		String errorMessage = of_validate();
		
		if(errorMessage != null) 
		{
			Sys.of_sendErrorMessage(null, "MySQL", "of_createConnection(); #10", "Error while validating the settings. "+errorMessage);
			return -1;
		}
		
		//	Registrierung der JDBC-Driver Klasse
        try
        {
        	Class.forName("com.mysql.jdbc.Driver");
        }
        catch (ClassNotFoundException e)
        {
			Sys.of_sendErrorMessage(e, "MySQL", "of_createConnection(); #20", "Error while searching for the Class: com.mysql.jdbc.Driver");
            return -1;
        }
        
        //	Verbindungsaufbau
        try
        {
        	//	Connection-String bauen :)
        	connectionString = "jdbc:mysql://"+server+"/"+dbName+"?autoReconnect=true";
            con = DriverManager.getConnection(connectionString, userName, password);
        }
        catch (SQLException e)
        {
			Sys.of_sendErrorMessage(e, "MySQL", "of_createConnection(); #30", "Error while creating the DriverManager instance!");
        	return -1;
        }
        
        //	Verbindung zur Datenbank wurde hergestellt.
        if(of_isConnected()) 
        {
        	//	Debugs...
        	Sys.of_debug(instanzName+" Successfuly connected to database "+green + dbName + white);
        	Sys.of_debug(instanzName+" Searching for new database updates...");
        	
        	//	UPD-Service zum Aktualisieren der Datenbank.
        	UPDService updSrv = new UPDService();
        	
        	int rc = -1;
        	
        	if(updSrv.of_canRunUPD()) 
        	{
        		Sys.of_debug(instanzName+" The UPD-Service has found new database updates!");
        		
        		Sys.of_debug("=== UPD-Service: Start ===");
          		rc = updSrv.of_runUPD();
          		Sys.of_debug("=== UPD-Service: End ===");
          		
        		if(rc == 1)
        		{
        			Sys.of_debug(instanzName+"==== UPD-Conclusion ====");
            		Sys.of_debug(instanzName+" [UPD-Service]:");
            		Sys.of_debug(instanzName+" Executed-SQLs: " + updSrv.of_getExecutedSQLs());
            		Sys.of_debug(instanzName+" Error-SQLs: " + updSrv.of_getErrorSQLs());
            		Sys.of_debug(instanzName+" Skipped-SQLs: " + updSrv.of_getSkippedSQLs());
            		Sys.of_debug(instanzName+"========================");
        		}
        		//	Fehler beim Einspielen des UPDs!
        		else 
        		{
        			main.SQL.of_closeConnection();
        			main.SQL = null;
        			main.SETTINGS.of_setUseMySQL(false);
        			Sys.of_debug(instanzName+" There was an error by updating your database! Switch to the file-system (only for uptime)");
        		}
        	}
        	else 
        	{
        		Sys.of_debug(instanzName+ " No database updates found. You're database is fine :)");
        		rc = 1;
        	}
        	
    		return rc;
        }
	    
		return -1;
	}
	
	public String of_validate() 
	{
		if(server == null || server.isEmpty()) 
		{
			return "The server address need to be set in the settings!";
		}
		
		if(dbName == null ||dbName.isEmpty()) 
		{
			return "The database name need to be set in the settings!";
		}
		
		if(userName == null || userName.isEmpty()) 
		{
			return "The username or logId need to be set in the settings!";
		}
		
		if(password == null || password.isEmpty()) 
		{
			return "The password need to be set in the settings!";
		}
		
		if(updateKeyTable == null || updateKeyColumn == null || updateTableColumn4Key == null) 
		{
			return "This is a development error please contact the author. He has forgotten to set the 'updateKeyTable', 'updateKeyColumn' or 'updateTableColumn4Key'!";
		}
		
		return null;
	}
	
    public void of_closeConnection() 
    {
    	if(con != null) 
    	{
    		try
    		{
    			Sys.of_debug(instanzName+" Successfuly disconnected from database "+green + dbName + white);
        		con.close();
        		
        		if(con.isClosed()) 
        		{
        			con = null;
        		}
			}
    		catch (Exception e)  { }
    	}
    }
    
	/***************************************/
	/* RUN UPDATE */
	/***************************************/
	
	private boolean of_run_update(String sql_insert_update, boolean bool)
	{
        try
        {
        	if(stm == null || stm.isClosed())
        	{
        		stm = con.createStatement();
        	}
        	
			stm.executeUpdate(sql_insert_update);
			return true;
		}
        catch (SQLException e)
        { 
        	if(bool) 
        	{
    			Sys.of_sendErrorMessage(null, "MySQL", "of_run_update("+bool+");", sql_insert_update);
        	}
        }
		
		return false;
	}
	
	public boolean of_run_update(String sql_insert_update) 
	{
		return of_run_update(sql_insert_update, true);
	}
	
	public boolean of_run_update_supress(String sql_insert_update) 
	{
		return of_run_update(sql_insert_update, false);
	}	
	
	/***************************************/
	/* GET ONE/SINGLE ROW */
	/***************************************/
	
	private String of_getRowValue(String sql_select_query, String column_name, boolean bool) 
	{
    	ResultSet rs = null;
    	
        try
        {
        	if(stm == null || stm.isClosed())
        	{
        		stm = con.createStatement();
        	}
        	
            rs = stm.executeQuery(sql_select_query);
            rs.next();

            return rs.getString(column_name);
        }
        catch (SQLException e)
        {
        	if(bool) 
        	{
    			Sys.of_sendErrorMessage(null, "MySQL", "of_getRowValue("+bool+");", sql_select_query);
        	}
        }
        
		return null;
	}
	
    public String of_getRowValue(String sql_select_query, String column_name) 
    {
    	return of_getRowValue(sql_select_query, column_name, true);
    }
	
    public String of_getRowValue_suppress(String sql_select_query, String column_name) 
    {
    	return of_getRowValue(sql_select_query, column_name, false);
    }
    
	/***************************************/
	/* GET THE RESULTSET */
	/***************************************/
    
    private ResultSet of_getResultSet(String sql_select_query_rows, boolean bool, boolean result_next) 
    {
        ResultSet rs = null;
        
        try
        {
        	if(stm == null || stm.isClosed())
        	{
        		stm = con.createStatement();
        	}
        	
            rs = stm.executeQuery(sql_select_query_rows);            
            
            if(result_next) 
            {
                if(rs.next()) 
                { 
                	return rs;
                }
            }
            
            return rs;
        }
        catch (SQLException e)
        {
        	if(bool) 
        	{
    			Sys.of_sendErrorMessage(null, "MySQL", "of_getResultSet("+bool+");", sql_select_query_rows);
        	}
        }
        
        return null;
    }
    
    public ResultSet of_getResultSet(String sql_select_query_rows, boolean result_next)
    {
    	return of_getResultSet(sql_select_query_rows, true, result_next);
    }
    
    public ResultSet of_getResultSet_suppress(String sql_select_query_rows, boolean result_next)
    {
    	return of_getResultSet(sql_select_query_rows, false, result_next);
    }
    
    public int of_updateKey(String tableName) 
    {
    	//	Beide Strings m��ssen gesetzt worden sein!
    	if(!updateKeyTable.isEmpty() && !updateKeyColumn.isEmpty() && !updateTableColumn4Key.isEmpty()) 
    	{
    		String sqlSelect = "SELECT " + updateKeyColumn + " FROM " + updateKeyTable + " WHERE " + updateTableColumn4Key + " = '"+tableName+"';"; 
    		int key = Integer.valueOf(of_getRowValue(sqlSelect, updateKeyColumn, false));
    		
    		if(key == -1) 
    		{
    			Sys.of_sendErrorMessage(null, "MySQL", "of_updateKey("+tableName+");", "This is a SQL-problem might be the table entry doesn't exist! SQL: "+sqlSelect);
    			return -1;
    		}
    		
    		key++;
    		String sqlUpdate = "UPDATE "+updateKeyTable+" SET " + updateKeyColumn + " = " + key + " WHERE " + updateTableColumn4Key + " = '" + tableName + "';";
    		of_run_update(sqlUpdate);
    		return key;
    	}
    	
    	return -1;
    }
    
	/***************************************/
	/* SETTER // ADDER // REMOVER */
	/***************************************/
	
	public void of_setServer(String server) 
	{
		this.server = server;
	}
	
	public void of_setDbName(String database) 
	{
		this.dbName = database;
	}
	
	public void of_setUserName(String username) 
	{
		this.userName = username;
	}
	
	public void of_setPassword(String password) 
	{
		this.password = password;
	}
	
	public void of_setUpdateKeyTableAndColumns(String updateKeyTable, String updateKeyColumn, String updateTableColumn4Key) 
	{
		this.updateKeyTable = updateKeyTable;
		this.updateKeyColumn = updateKeyColumn;
		this.updateTableColumn4Key = updateTableColumn4Key;
	}
	
	/***************************************/
	/* BOOLS */
	/***************************************/
	
	public boolean of_isConnected() 
	{
		boolean lb_value = false;

		if(con != null) 
		{			
			try
			{
				lb_value = !con.isClosed();
			}
			catch (Exception e) { }
		}
		
		return lb_value;
	}
}
