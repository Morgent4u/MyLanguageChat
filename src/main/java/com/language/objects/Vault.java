package com.language.objects;

import com.language.main.main;
import org.bukkit.plugin.RegisteredServiceProvider;
import com.language.ancestor.Objekt;
import net.milkbowl.vault.permission.Permission;

/**
 * @Created 18.10.2021
 * @Author Nihar
 * @Description
 * This class is used to represent vault-objects.
 * In this case the PERMISSIONS-Object is used from the plugin vault.
 */

public class Vault extends Objekt
{
	public Permission PERMISSIONS;

	/* ************************************* */
	/* LOADER */
	/* ************************************* */

	@Override
	public int of_load()
	{
		try
		{
			//	VAULT-API Permissions-Klasse registrieren zum Plugin!
	        RegisteredServiceProvider<Permission> registerClassPermission = main.PLUGIN.getServer().getServicesManager().getRegistration(Permission.class);
			assert registerClassPermission != null;
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
