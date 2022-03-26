package com.language.objects;

import com.language.main.main;
import org.bukkit.plugin.RegisteredServiceProvider;
import com.language.ancestor.Objekt;
import net.milkbowl.vault.permission.Permission;

public class Vault extends Objekt
{
	/*	Angelegt am: 18.10.2021
	 * 	Erstellt von: Nihar
	 * 	Beschreibung:
	 * 	Diese Klasse wird bent�igt um Chatgruppen
	 * 	im Chat anzuzeigen. Dazu z�hlt z.B. PermissionsEx oder
	 * 	luckyPerms.
	 * 
	 */
	
	/***************************************/
	/* Constructor // Loader */
	/***************************************/
	
	public Permission PERMISSIONS;
	
	@Override
	public int of_load()
	{
		try
		{
			//	VAULT-API Permissions-Klasse registrieren zum Plugin!
	        RegisteredServiceProvider<Permission> registerClassPermission = main.PLUGIN.getServer().getServicesManager().getRegistration(Permission.class);
	        PERMISSIONS = registerClassPermission.getProvider();
	        return 1;
		}
		catch (Exception e)
		{
			of_sendErrorMessage(e, "of_load();", "Error while registering the vault-service to this plugin.");
		}
		
		return -1;
	}
}
