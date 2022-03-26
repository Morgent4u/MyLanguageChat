package com.language.cmds;

import java.util.HashMap;

import com.language.main.main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.language.objects.Text;
import com.language.spieler.Spieler;
import com.language.sys.Sys;

public class CMD_Language implements CommandExecutor
{
	/*	Angelegt am: 22.03.2021
	 * 	Erstellt von: Nihar
	 * 	Beschreibung:
	 * 	Mit dem Befehl ist es möglich eine Sprache
	 * 	auszuwählen oder die Hilfe zu öffnen.
	 * 
	 */
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String arg, String[] args)
	{
		if(cmd.getName().equalsIgnoreCase("Language")) 
		{
			if(sender instanceof Player) 
			{
				Spieler ps = main.SPIELERSERVICE.CONTEXT.of_getSpieler(sender.getName());
				
				if(ps != null) 
				{
					if(ps.of_hasDefaultPermissions()) 
					{
						if(args.length == 0) 
						{
							//	Admin Hilfe anzeigen...
							if(ps.of_hasSetupPermissions()) 
							{
								of_sendHelpPage(ps.of_getPlayer());
							}
							//	Standard Hilfe dem Spieler anzeigen...
							else 
							{
								of_sendHelpPage2Player(ps.of_getPlayer());
							}
							
							return false;
						}
						
						if(args.length == 1) 
						{
							if(args[0].equalsIgnoreCase("list")) 
							{
								//	Text für die Unterstützten Sprachen einholen...
								Text txt = new Text("txt_supported_languages", ps.of_getPlayer());
								txt.of_addReplacement("%p%", ps.of_getName());
								String[] messages = txt.of_getText();
								
								//	Text auf folgendes Format überprüfen...
								//  FORMAT=&7%countrycode% - &a%language%
								if(messages != null && messages.length > 0) 
								{
									Player p = ps.of_getPlayer();
									
									for(int i = 0; i< messages.length; i++) 
									{
										//	Nachricht in ein bestimmtes Format ausgeben...
										if(messages[i].startsWith("FORMAT=")) 
										{
											String replacedText = messages[i].replace("FORMAT=", "");
											
											//	Alle Sprachen durchlaufen...
											HashMap<String, String> languages = main.SETTINGS.of_getSupportedLanguagesWithFullNames();
											
											if(languages != null && languages.size() > 0) 
											{
												for(String countryCode : languages.keySet()) 
												{
													p.sendMessage(replacedText.replace("%countrycode%", countryCode.toUpperCase()).replace("%language%", languages.get(countryCode)));
												}
											}
										}
										//	Sound abspielen...
										else if(messages[i].startsWith("PLAYSOUND=")) 
										{
											String playSound = messages[i].replace("PLAYSOUND=", "").toLowerCase();
											p.playSound(p.getLocation(), playSound, 1, 1);	
										}
										//	Normale Nachricht
										else 
										{
											p.sendMessage(messages[i]);											
										}
									}
								}
								
								return false;
							}
							else if(args[0].equalsIgnoreCase("help")) 
							{
								of_sendHelpPage2Player(ps.of_getPlayer());
								return false;
							}
							else if(args[0].equalsIgnoreCase("enable") || args[0].equalsIgnoreCase("disable")) 
							{
								if(ps.of_hasSetupPermissions()) 
								{
									String status = "§aenabled";
									
									if(main.SETTINGS.of_isUsingLanguage()) 
									{
										main.SETTINGS.of_setPlugin(false);
										status = "§cdisabled";
									}
									else 
									{
										main.SETTINGS.of_setPlugin(true);
									}
									
									ps.of_getPlayer().sendMessage("§8[§aMyLanguage§fChat§8]§f: This pluing has been "+status+"§f.");
								}
								else 
								{
									main.MESSAGESERVICE.of_sendMsgHasNoPermissions(ps.of_getPlayer());
								}
								
								return false;
							}
							if(args[0].equalsIgnoreCase("translate-everything")) 
							{
								if(ps.of_hasSetupPermissions()) 
								{
									String status = "§aenabled";
									
									if(main.SETTINGS.of_isUsingTranslateEveryMessage2UserLanguage()) 
									{
										main.SETTINGS.of_setTranslateEveryMessage2DefaultUserLanguage(false);
										status = "§cdisabled";
									}
									else 
									{
										main.SETTINGS.of_setTranslateEveryMessage2DefaultUserLanguage(true);
									}
									
									ps.of_getPlayer().sendMessage("§8[§aMyLanguage§fChat§8]§f: Translate every message to the user language: "+status+"§f.");
								}
								else 
								{
									main.MESSAGESERVICE.of_sendMsgHasNoPermissions(ps.of_getPlayer());
								}
								
								return false;
							}
							//	Das nächste Argument ist hoffentlich, ein Ländercode....
							else 
							{
								boolean lb_languageFound = of_languageIsSupported(args[0]);
								
								if(lb_languageFound) 
								{
									main.SPIELERSERVICE.of_swapLanguage(ps, args[0].toLowerCase());
									main.MESSAGESERVICE.of_getMessage(ps.of_getPlayer(), "MyLanguageChat.languageSelected", new String[] {"%language%"}, new String[] {main.SETTINGS.of_getSupportedLanguagesWithFullNames().get(args[0].toLowerCase())});
								}
								else 
								{
									main.MESSAGESERVICE.of_getMessage(ps.of_getPlayer(), "MyLanguageChat.languageNotFound", new String[] {"%language%"}, new String[] {args[0]});
								}
							}
							
							return false;
						}
						
						if(args.length == 2) 
						{
							//	Standardsprache ändern...
							if(args[0].equalsIgnoreCase("default-language")) 
							{
								if(ps.of_hasSetupPermissions()) 
								{
									boolean lb_languageFound = of_languageIsSupported(args[1]);
									
									if(lb_languageFound) 
									{
										ps.of_getPlayer().sendMessage("§8[§aMyLanguage§fChat§8]§f: Default-language has been changed to:§a " + main.SETTINGS.of_getSupportedLanguagesWithFullNames().get(args[1].toLowerCase()));
										main.SETTINGS.of_setDefaultLanguage(args[1].toLowerCase());
									}
									else 
									{
										ps.of_getPlayer().sendMessage("§8[§aMyLanguage§fChat§8]§f: The language §a"+args[1]+"§f is not avaible or is not set in the 'settings.yml'!");
									}	
								}
								else 
								{
									main.MESSAGESERVICE.of_sendMsgHasNoPermissions(ps.of_getPlayer());
								}
								
								return false;
							}
							else if(args[0].equalsIgnoreCase("translate-symbole")) 
							{
								if(ps.of_hasSetupPermissions()) 
								{
									ps.of_getPlayer().sendMessage("§8[§aMyLanguage§fChat§8]§f: The translation-symbole has been changed to:§a " + args[1]);
									main.SETTINGS.of_setTranslationSymbole(args[1]);
								}
								else 
								{
									main.MESSAGESERVICE.of_sendMsgHasNoPermissions(ps.of_getPlayer());
								}
								
								return false;
							}
						}
					}
					else 
					{
						main.MESSAGESERVICE.of_sendMsgHasNoPermissions(ps.of_getPlayer());
					}
				}
			}
		}
		
		return false;
	}	
	
	/***************************/
	/*	ANWEISUNGEN  */
	/***************************/
	
	private void of_sendHelpPage(Player p) 
	{
		p.sendMessage("§7══════════════");
		p.sendMessage("");
		p.sendMessage("§8[§c§l"+Sys.of_getProgrammVersion()+"§8]");
		p.sendMessage("");
		p.sendMessage("§fHello§d "+p.getName()+"§f!");
		p.sendMessage("§fHere is a little help :)");
		p.sendMessage("");
		p.sendMessage("§9Commands:");
		p.sendMessage("§aShow the admin-help:");
		p.sendMessage("§c/Language");
		p.sendMessage("§aShow the user-help:");
		p.sendMessage("§c/Language help");
		p.sendMessage("§aEnable or disable the plugin:");
		p.sendMessage("§c/Language enable/disbale");
		p.sendMessage("§aSet the default-language:");
		p.sendMessage("§c/Language default-language <countrycode>");
		p.sendMessage("§aSet the translate symbole:");
		p.sendMessage("§c/Language translate-symbole <symbole>");
		p.sendMessage("§aTranslate every message into the users language:");
		p.sendMessage("§c/Language translate-everything");
		p.sendMessage("");
		p.sendMessage("§7══════════════");
	}
	
	//	Standard-Hilfetext!
	private void of_sendHelpPage2Player(Player p) 
	{
		//	Hilfetext an den Spieler übermitteln...
		Text txt = new Text("txt_cmdhelper4user", p);
		txt.of_addReplacement("%p%", p.getName());
		txt.of_sendTranslatedText2Player();
	}
	
	private boolean of_languageIsSupported(String language) 
	{
		String[] languages = main.SETTINGS.of_getSupportedLanguages();
		
		if(languages != null && languages.length > 0) 
		{
			for(int i = 0; i< languages.length; i++) 
			{
				//	Hat der Spieler ein Ländercode eingegeben?
				if(languages[i].equalsIgnoreCase(language)) 
				{
					return true;
				}
			}
		}
		
		return false;
	}
}
