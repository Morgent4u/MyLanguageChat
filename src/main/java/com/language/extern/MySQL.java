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

	/* ************************* */
	/* CONSTRUCTOR */
	/* ************************* */

	/**
	 * Constructor of the transaction-object.
	 * @param instanzName Instance name for example: 'MAIN' or 'FAST SQL'
	 *                    This is used to get a difference between multiple transaction-object instances.
	 */
	public MySQL(String instanzName) 
	{
		this.instanzName = "[MySQL-"+instanzName+"]:";
	}

	/* ************************* */
	/* OBJEKT-ANWEISUNGEN */
	/* ************************* */
	
	//	Anhand aller Angaben ConnectionString bauen und Verbindung
	//	herstellen.

	/**
	 * Creates a database connection to the defined database in the settings.
	 * @return 1 = SUCCESS, -1 = ERROR - An error will be displayed in the console!
	 */
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

	/**
	 * Validation process for this transfer object.
	 * This function checks if all necessary attributes has been set.
	 * @return EMPTY-String = SUCCESS, NO-EMPTY-String = Error - A message with the error message.
	 */
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

	/**
	 * This function close the database connection.
	 */
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
    		catch (Exception ignored)  { }
    	}
    }

	/* ************************* */
	/* RUN UPDATE */
	/* ************************* */

	/**
	 * This function executes a sql-statement. For example:
	 * 'UPDATE user SET user.name = 'Test' WHERE user.userId = 1;'
	 * @param sql_insert_update The sql-statement which will be executed.
	 * @param bool Displays an error message.
	 * @return TRUE = SUCCESS, FALSE = ERROR - Error message only appears when the parameter 'bool' is true!
	 */
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
    			Sys.of_sendErrorMessage(null, "MySQL", "of_run_update("+ true +");", sql_insert_update);
        	}
        }
		
		return false;
	}

	/**
	 * Overload function of of_run_update();
	 * This function is using by default display error message.
	 * @param sql_insert_update The sql-statement which will be executed.
	 * @return TRUE = SUCCESS, FALSE = ERROR - <b>Message will be displayed in the console!<b/>
	 */
	public boolean of_run_update(String sql_insert_update) 
	{
		return of_run_update(sql_insert_update, true);
	}

	/**
	 * Overload function of of_run_update();
	 * This function is using by default don't display error message.
	 * @param sql_insert_update The sql-statement which will be executed.
	 * @return TRUE = SUCCESS, FALSE = ERROR - <b>No Message will be displayed in the console!<b/>
	 */
	public boolean of_run_update_suppress(String sql_insert_update)
	{
		return of_run_update(sql_insert_update, false);
	}

	/* ************************* */
	/* GET ONE ROW VALUE */
	/* ************************* */

	/**
	 * This function is used to retrieve a one value/column from the database.
	 * @param sql_select_query SQL-statement which will be executed.
	 * @param column_name Column name from which the value should be from.
	 * @param bool Display an error message into the console.
	 * @return Value from the column which is specified in the sql-statement and the parameter column_name.
	 */
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
    			Sys.of_sendErrorMessage(null, "MySQL", "of_getRowValue("+ true +");", sql_select_query);
        	}
        }
        
		return null;
	}

	/**
	 * Overload for of_getRowValue(String sqlStatement, String column_name, boolean displayErrorMessage)
	 * <b>This function sends by default an error message into the console!<b/>
	 * @param sql_select_query SQL-Statement which will be executed.
	 * @param column_name Column name from which the value should be from.
	 * @return Value from the column which is specified in the sql-statement and the parameter column_name.
	 */
    public String of_getRowValue(String sql_select_query, String column_name) 
    {
    	return of_getRowValue(sql_select_query, column_name, true);
    }

	/**
	 * Overload for of_getRowValue(String sqlStatement, String column_name, boolean displayErrorMessage)
	 * <b>This function sends by default no error message into the console!<b/>
	 * @param sql_select_query SQL-Statement which will be executed.
	 * @param column_name Column name from which the value should be from.
	 * @return Value from the column which is specified in the sql-statement and the parameter column_name.
	 */
    public String of_getRowValue_suppress(String sql_select_query, String column_name) 
    {
    	return of_getRowValue(sql_select_query, column_name, false);
    }

	/* ************************* */
	/* GET A RESULT SET */
	/* ************************* */

	/**
	 * This function returns a ResultSet. This can be used for execute a multirow select-statement.
	 * For example:
	 * ResultSet result = MySQL.of_getResultSet("SELECT * FROM user;", false, false);
	 * @param sql_select_query_rows SQL-Statement which will be executed.
	 * @param bool Display error message
	 * @param result_next This should be FALSE when you're using a multirow SELECT-Statement. This can be TRUE
	 *                    if you know that your result will be one row, and you want to get data from more COLUMN-Data than one!
	 * @return Returns a ResultSet for external using!
	 */
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
    			Sys.of_sendErrorMessage(null, "MySQL", "of_getResultSet("+ true +");", sql_select_query_rows);
        	}
        }
        
        return null;
    }

	/**
	 * Overload: This function returns a ResultSet. This can be used for execute a multirow select-statement.
	 * For example:
	 * ResultSet result = MySQL.of_getResultSet("SELECT * FROM user;", false, false);
	 * <b>This function sends by default an error message to the console!</b>
	 * @param sql_select_query_rows SQL-Statement which will be executed.
	 * @param result_next This should be FALSE when you're using a multirow SELECT-Statement. This can be TRUE
	 *                    if you know that your result will be one row, and you want to get data from more COLUMN-Data than one!
	 * @return Returns a ResultSet for external using!
	 */
    public ResultSet of_getResultSet(String sql_select_query_rows, boolean result_next)
    {
    	return of_getResultSet(sql_select_query_rows, true, result_next);
    }

	/**
	 * Overload: This function returns a ResultSet. This can be used for execute a multirow select-statement.
	 * For example:
	 * ResultSet result = MySQL.of_getResultSet("SELECT * FROM user;", false, false);
	 * <b>This function sends by default no error message to the console!</b>
	 * @param sql_select_query_rows SQL-Statement which will be executed.
	 * @param result_next This should be FALSE when you're using a multirow SELECT-Statement. This can be TRUE
	 *                    if you know that your result will be one row, and you want to get data from more COLUMN-Data than one!
	 * @return Returns a ResultSet for external using!
	 */
    public ResultSet of_getResultSet_suppress(String sql_select_query_rows, boolean result_next)
    {
    	return of_getResultSet(sql_select_query_rows, false, result_next);
    }

	/**
	 * This function is used to get a primary-key for a table by using an external key-column-control table.
	 * For this function your code must have already executed following function: of_setUpdateKeyTableAndColumns(String, String, String);
	 * @param tableName Table name from which table you want to get the primary-key.
	 * @return Primary-key id.
	 */
    public int of_updateKey(String tableName) 
    {
    	//	Beide Strings m��ssen gesetzt worden sein!
    	if(!updateKeyTable.isEmpty() && !updateKeyColumn.isEmpty() && !updateTableColumn4Key.isEmpty()) 
    	{
    		String sqlSelect = "SELECT " + updateKeyColumn + " FROM " + updateKeyTable + " WHERE " + updateTableColumn4Key + " = '"+tableName+"';";
			int key = -1;

			try
			{
				key = Integer.parseInt(of_getRowValue(sqlSelect, updateKeyColumn, false));
			}
			catch (Exception ignored) { }

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

	/* ****************************** */
	/* SETTER // REMOVER // ADDER */
	/* ****************************** */
	
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

	/**
	 * <b>This function needs to be executed after initialising this object!<b/>
	 * @param updateKeyTable Name of the table which controls the primary-keys for example: '%_keys'
	 * @param updateKeyColumn The column which contains the key for example: 'lastkey'
	 * @param updateTableColumn4Key The column which contains the table name for example: 'tablename'
	 */
	public void of_setUpdateKeyTableAndColumns(String updateKeyTable, String updateKeyColumn, String updateTableColumn4Key) 
	{
		this.updateKeyTable = updateKeyTable;
		this.updateKeyColumn = updateKeyColumn;
		this.updateTableColumn4Key = updateTableColumn4Key;
	}

	/* ****************************** */
	/* BOOLS */
	/* ****************************** */

	/**
	 * This function checks if this transaction-object is still connected to the database.
	 * @return TRUE = Connected, FALSE = No connection to the database!
	 */
	public boolean of_isConnected() 
	{
		boolean lb_value = false;

		if(con != null) 
		{			
			try
			{
				lb_value = !con.isClosed();
			}
			catch (Exception ignored) { }
		}
		
		return lb_value;
	}
}
