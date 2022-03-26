package com.language.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bukkit.configuration.file.YamlConfiguration;
import com.language.sys.Sys;

public class Datei
{
	/*	Angelegt am: 11.10.2021
	 * 	Erstellt von: Nihar
	 * 	Beschreibung:
	 * 	Mit dem Objekt: Datei ist
	 * 	es m�lich, schnell Dateien
	 * 	zu erstellen und ggf. noch �nderungen
	 * 	vorzunehmen.
	 * 	
	 */
	
	private File file;
	private YamlConfiguration cfg;
	private boolean ib_autoCreateMode;
	
	/***************************************/
	/* CONSTRUCTOR */
	/***************************************/
	public Datei() { }
	
	public Datei(String absolutePath) 
	{
		//	Korrektur, falls beim Absoluten FilePath etwas falsch angegeben wird!
		absolutePath = absolutePath.replace("\\", "//");
		
		//	Wenn die Datei nicht auf .yml endet, wird dies hinzugef�gt.
		if(!absolutePath.contains(".yml")) 
		{
			absolutePath += ".yml";  
		}
		
		file = new File(absolutePath);
		cfg = new YamlConfiguration().loadConfiguration(file);
	}
	
	public Datei(File file)
	{
		this.file = file;
		this.cfg = new YamlConfiguration().loadConfiguration(file);
	}
	
	/***************************************/
	/* DECONSTRUCTOR // SAVE */
	/***************************************/
	
	public void of_unload() 
	{
		if(file != null) 
		{
			//	Beim Entladen, alle Dateien entfernen,
			//	die keinen Inhalt haben. Wir wollen ja
			//	keinen Datenm�ll erzeugen :)!
			
			if(file.length() == 0) 
			{
				file.delete();
			}
		}
	}
	
	public int of_save(String invoker) 
	{
		//	RC:
		//	 1: OK
		//	-1: Fehler
		
		try
		{
			cfg.save(file);
			return 1;
		}
		catch (Exception e)
		{
			Sys.of_sendErrorMessage(e, "Datei", "of_save(String)", "Error while saving the file!");
		}
		
		return -1;
	}

	/* GETSET-Anweisungen */
	/***************************************/

	//	Mit dieser Funktion gibt es beim Setzen von einem String, auch einen String als R�ckgabewert! 
	public String of_getSetString(String configKey, String defaultValue) 
	{
		String tmpValue = null;
		
		if(cfg.isSet(configKey)) 
		{
			tmpValue = cfg.getString(configKey);
			
			if(tmpValue == null) 
			{
				tmpValue = defaultValue;
			}
		}
		else 
		{
			cfg.set(configKey, defaultValue);
			tmpValue = defaultValue;
		}
		
		return tmpValue;
	}
	
	//	Mit dieser Funktion gibt es beim Setzen von einem String, auch einen String als R�ckgabewert! 
	public int of_getSetInt(String configKey, int defaultValue) 
	{
		int tmpValue = -1;
		
		if(cfg.isSet(configKey)) 
		{
			tmpValue = Sys.of_getString2Int(cfg.getString(configKey));
			
			if(tmpValue == -1) 
			{
				tmpValue = defaultValue;
			}
		}
		else 
		{
			cfg.set(configKey, defaultValue);
			tmpValue = defaultValue;
		}
		
		return tmpValue;
	}
	
	//	Mit dieser Funktion gibt es beim Setzen von einem Boolean, auch einen Boolean als R�ckgabewert! 
	public boolean of_getSetBoolean(String configKey, boolean defaultBool) 
	{
		boolean tmpValue = false;
		
		if(cfg.isSet(configKey)) 
		{
			tmpValue = cfg.getBoolean(configKey);
		}
		else 
		{
			cfg.set(configKey, defaultBool);
			tmpValue = defaultBool;
		}
		
		return tmpValue;
	}
	
	public String[] of_getSetStringArrayList(String configKey, ArrayList<String> arrayList) 
	{
		String[] tmp = null;
		
		//	Existiert der Pfad bereits?
		if(cfg.isSet(configKey)) 
		{
			//	Laden...
			tmp = of_getStringArrayByKey(configKey);
		}
		//	Neu anlegen...
		else 
		{
			cfg.set(configKey, arrayList);
		}
		
		//	Fehler beim Laden?
		if(tmp == null) 
		{
			//	Default geht zur�ck :)
			return arrayList.toArray(new String[0]);
		}
		
		return tmp;
	}
	
	public String[] of_getSetStringArray(String configKey, String[] array) 
	{
		//	�berladung von of_getSetStringArray...
		ArrayList<String> tmpList = new ArrayList<>();
		
		if(array != null && array.length > 0) 
		{
			//	String-Array zu einer ArrayList konvertieren.
			Collections.addAll(tmpList, array);
			
			return of_getSetStringArrayList(configKey, tmpList);
		}
		
		return null;
	}

	/***************************************/
	/* SET-Anweisungen */
	/***************************************/
	
	public void of_set(String configKey, Object object) 
	{
		if(cfg != null) 
		{
			if(object == null) 
			{
				Sys.of_sendErrorMessage(null, "Datei", "of_set(String, Object);", "The config-section-path is not valid! "+configKey);
				return;
			}
			
			if(ib_autoCreateMode && cfg.isSet(configKey)) 
			{
				return;
			}
			
			cfg.set(configKey, object);
		}
	}
	
	public void of_deleteRecrusive(File directory) 
	{
		String[] fileNames = directory.list();
		
		if(fileNames != null) 
		{
			for(String fileName : fileNames) 
			{
				of_deleteRecrusive(new File(directory.getPath(), fileName));
			}
		}
		
		directory.delete();
	}
	
	public void of_delete() 
	{
		if(file != null) 
		{
			file.delete();
		}
	}
	
	/***************************************/
	/* DebugCenter */
	/***************************************/
	
	public void of_sendDebugDetailInformation()
	{
		//	Ausgabe bzgl. File
		boolean containsFile = ( file != null );
		System.out.println("File: "+containsFile);
		
		if(containsFile) 
		{
			System.out.println("FileName: "+file.getName());
			System.out.println("FilePath: "+file.getAbsolutePath());
		}
		
		//	Ausgabe bzgl. CFG
		boolean containsCfg = ( cfg != null );
		System.out.println("Cfg: "+containsCfg);
		System.out.println("AutoCreateMode: "+of_isAutoCreateModeEnabled());
	}
	
	/***************************************/
	/* SETTER // ADDER */
	/***************************************/
	
	public void of_setFile(File file) 
	{
		this.file = file;
	}

	public void of_setConfig(YamlConfiguration cfg) 
	{
		this.cfg = cfg;
	}
	
	public void of_setAutoCreateMode(boolean bool) 
	{
		ib_autoCreateMode = bool;
	}
	
	public void of_removeKeySection(String key) 
	{
		cfg.set(key, "");
	}
	
	/***************************************/
	/* GETTER */
	/***************************************/
	
	public String[] of_getStringArrayByKey(String configKey) 
	{
		if(cfg != null) 
		{
			try 
			{
				List<String> values = cfg.getStringList(configKey);
				
				//	Sicherstellen, dass die Liste nicht leer ist.
				if(!values.isEmpty())
				{
					return values.toArray(new String[0]);
				}
			}
			catch (Exception ignored) { }
		}
		
		return null;
	}
	
	public String[] of_getKeySectionsByKey(String configKey) 
	{
		String[] keys = null;
		
		if(cfg != null) 
		{
			try
			{
				keys = cfg.getConfigurationSection(configKey).getKeys(false).toArray(new String[0]);
			}
			catch (Exception ignored) { }
		}
		
		return keys;
	}
	
	public String of_getStringByKey(String configKey) 
	{
		String value = null;
		
		if(cfg != null) 
		{
			try
			{
				value = cfg.getString(configKey);
			}
			catch (Exception ignored) { }
		}
		
		return value;
	}
	
	public int of_getNextKey() 
	{
		int key = of_getIntByKey("KeyCount");
		
		if(key == -1) 
		{
			key = 0; 
		}
		
		key++;
		
		of_set("KeyCount", key);
		of_save("Datei.of_getNextKey();");
		
		return key;
	}
	
	public int of_getIntByKey(String configKey) 
	{
		int value = -1;
		
		if(cfg != null) 
		{
			try
			{
				value = cfg.getInt(configKey);
			}
			catch (Exception ignored) { }
		}
		
		return value;
	}
	
	public long of_getLongByKey(String configKey) 
	{
		long value = -1;
		
		if(cfg != null) 
		{
			try
			{
				value = cfg.getLong(configKey);
			}
			catch (Exception ignored) { }
		}
		
		return value;
	}
	
	public double of_getDoubleByKey(String configKey) 
	{
		double value = -1;
		
		if(cfg != null) 
		{
			try
			{
				value = cfg.getDouble(configKey);
			}
			catch (Exception ignored) { }
		}
		
		return value;
	}
	
	public boolean of_getBooleanByKey(String configKey) 
	{
		boolean value = false;
		
		if(cfg != null) 
		{
			try
			{
				value = cfg.getBoolean(configKey);
			}
			catch (Exception ignored) { }
		}
		
		return value;
	}
	
	public File of_getFile() 
	{
		return file;
	}
	
	public File[] of_getFiles() 
	{
		return file.listFiles();
	}
	
	public YamlConfiguration of_getConfig() 
	{
		return cfg;
	}
	
	/***************************************/
	/* BOOLS */
	/***************************************/
	
	public boolean of_fileExists()
	{	
		if(file != null) 
		{	
			if(file.length() == 0) 
			{
				file.delete();
				
				return false;
			}
			
			return file.exists();
		}
		
		return false;
	}
	
	public boolean of_isAutoCreateModeEnabled() 
	{
		return ib_autoCreateMode;
	}
}
