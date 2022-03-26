package com.language.ancestor;

import com.language.sys.Sys;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Objekt
{
	/*	Angelegt am: 20.03.2022
	 * 	Erstellt von: Nihar
	 * 	Beschreibung:
	 * 	Diese Klasse dient als Objekt-Ahne.
	 * 	Mithilfe dieser Klasse werden Objekte erstellt.
	 * 	
	 */
	
	//	Attribute:
	private int objektId;
	private int objektTargetId;

	private String objektInfo;
	
	//	Flags
	private boolean ib_errorFlag;
	private boolean ib_autoSave;
	
	/***************************************/
	/* LOADER */
	/***************************************/
	
	public int of_load()
	{
		//	Wird ggf. vom Erben �berschrieben...
		Sys.of_sendMessage(of_getObjektName() + "of_load(); is not overriden!");
		return -1;
	}
	
	public int of_load(String[] args)
	{
		//	Wird ggf. vom Erben  �berschrieben...
		Sys.of_sendMessage(of_getObjektName() + "of_load(String[] args); is not overriden!");
		return -1;
	}
	
	/***************************************/
	/* DECONSTRUCTOR */
	/***************************************/
	
	public void of_unload() 
	{
		//	Wird ggf. vom Erben �berschrieben.
	}
	
	/***************************************/
	/* OBJEKT-ANWEISUNGEN */
	/***************************************/
	
	public String of_validate() 
	{
		Sys.of_sendMessage(of_getObjektName() + "of_validate; is not overriden!");
		return "";
	}
	
	public int of_save(String invoker) 
	{
		//	RC:
		//	 1: OK
		//	-1: Fehler
		
		//	Wird ggf. vom Erben �berschrieben...
		return -1;
	}
	
	public int of_save() 
	{
		return of_save(of_getObjektName());
	}
	
	/***************************************/
	/* DEBUG-CENTER */
	/***************************************/
	
	public void of_sendDebugInformation(String invoker) 
	{
		if(Sys.of_isDebugModeEnabled()) 
		{			
			//	Farbcodes
			String white = "\u001B[0m";
			String green = "\u001B[32m";
			String yellow = "\u001B[33m";
			String blue = "\u001B[36m";
			
			Sys.of_sendMessage("======================================");
			Sys.of_sendMessage(green+"[DEBUG] "+Sys.of_getPaket()+white+", Object: "+yellow+of_getObjektName()+white);
			Sys.of_sendMessage(blue+"Invoker: "+white+invoker);
			Sys.of_sendMessage(white+"ObjectId: "+of_getObjektId());
			Sys.of_sendMessage(white+"ObjectTargetId: "+of_getTargetId());
			Sys.of_sendMessage(white+"ObjectInfoAttribute: "+of_getInfo());
			Sys.of_sendMessage(white+"HasAnError: " + of_hasAnError());
			Sys.of_sendMessage(white+"AutoSaving: " + of_isAutoSaveEnabled());
			Sys.of_sendMessage(yellow+"[Specific object-debug]:"+white);
			of_sendDebugDetailInformation();
			Sys.of_sendMessage("Time: "+new SimpleDateFormat("HH:mm:ss").format(new Date()).toString());
			Sys.of_sendMessage("=====================================");	
		}
	}
	
	public void of_sendDebugDetailInformation()
	{
		//	Wird vom Erben mit Informationen gef�llt...
		Sys.of_sendMessage(of_getObjektName()+".of_sendDebugDetailInformation(); is not overriden!");
	}
	
	/***************************************/
	/* ERROR-HANDLER */
	/***************************************/
	
	public void of_sendErrorMessage(Exception exception, String invoker, String errorMessage) 
	{
		//	Farbcodes
		String red = "\u001B[31m";
		String white = "\u001B[0m";
		String yellow = "\u001B[33m";
		String blue = "\u001B[36m";
		
		ib_errorFlag = true;
		
		Sys.of_sendMessage("=====================================");
		Sys.of_sendMessage(red+"[ERROR] "+Sys.of_getPaket()+white+", Object: "+yellow+of_getObjektName()+white);
		Sys.of_sendMessage(blue+"Invoker: "+white+invoker);
		Sys.of_sendMessage(white+"ObjectId: "+of_getObjektId());
		Sys.of_sendMessage(white+"ObjectTargetId: "+of_getTargetId());
		Sys.of_sendMessage(white+"ObjectInfoAttribute: "+of_getInfo());
		Sys.of_sendMessage(white+"HasAnError: " + of_hasAnError());
		Sys.of_sendMessage(white+"AutoSaving: " + of_isAutoSaveEnabled());
		Sys.of_sendMessage(yellow+"[Specific object-debug]:"+white);
		of_sendDebugDetailInformation();
		Sys.of_sendMessage(blue+"Error:"+white);
		Sys.of_sendMessage(red+errorMessage+white);
		Sys.of_sendMessage("Time: "+new SimpleDateFormat("HH:mm:ss").format(new Date()).toString()+white);
		Sys.of_sendMessage("=====================================");
		
		if(exception != null) 
		{
			Sys.of_sendMessage("[Auto-generated exception]:");
			Sys.of_sendMessage(exception.getMessage());
		}
	}
	
	/***************************************/
	/* SETTER */
	/***************************************/
	
	public void of_setTargetId(int targetId) 
	{
		this.objektTargetId = targetId;
	}
	
	public void of_setAutoSave(boolean bool) 
	{
		ib_autoSave = bool;
	}

	public void of_setInfo(String info) 
	{
		this.objektInfo = info;
	}

	/***************************************/
	/* GETTER */
	/***************************************/
	
	public String of_getObjektName() 
	{
		return getClass().getName();
	}
	
	public String of_getInfo() 
	{
		return objektInfo;
	}
	
	public int of_getObjektId() 
	{
		return objektId;
	}
	
	public int of_getTargetId() 
	{
		return objektTargetId;
	}
	
	/***************************************/
	/* BOOLS */
	/***************************************/
	
	public boolean of_hasAnError() 
	{
		return ib_errorFlag;
	}
	
	public boolean of_isAutoSaveEnabled() 
	{
		return ib_autoSave;
	}
}
