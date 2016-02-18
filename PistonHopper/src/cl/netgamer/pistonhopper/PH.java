package cl.netgamer.pistonhopper;

import java.util.logging.Logger;

import org.bukkit.plugin.java.JavaPlugin;

public final class PH extends JavaPlugin
{
	// properties
	private static Logger logger;
	
	// utility method
	public static void log(String msg)
	{
		logger.info(msg);
	}
	
	// plugin load
	public void onEnable()
	{
		// utility
		logger = getLogger();
		
		// try to read config file
		saveDefaultConfig();
		
		// register events
		new Events(this, getConfig().getStringList("whitelist"));
	}
}
