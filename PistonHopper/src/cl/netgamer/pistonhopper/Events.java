package cl.netgamer.pistonhopper;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Hopper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.material.PistonBaseMaterial;
import org.bukkit.metadata.FixedMetadataValue;


public final class Events implements Listener
{
	// properties
	protected PH plugin;
	private List<Material> whitelist = new ArrayList<Material>();
	
	// constructor, register events
	public Events(PH p, List<String> w)
	{
		Material m;
		for (String s: w)
		{
			m = Material.getMaterial(s);
			if (m != null) whitelist.add(m);
		}
		PH.log("Materials: readed = "+w.size()+", loaded = "+whitelist.size());
		
		plugin = p;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	// remember piston may be stucked
	// check block physics
	// check if block is piston
	@EventHandler
	public void onBlockPhysics(BlockPhysicsEvent e)
	{
		// check piston conditions
		Block piston = e.getBlock();
		if (!piston.getType().equals(Material.PISTON_BASE) || piston.getBlockPower() <= 0)
			return;
		
		// check race condition
		if (piston.hasMetadata("pushing"))
			return;
		 
		BlockFace face = ((PistonBaseMaterial) piston.getState().getData()).getFacing();

		// pushed against hopper or obsidian?
		Block against  = piston.getRelative(face, 2);
		if (against.getType().equals(Material.HOPPER))
		{
			// get whitelisted pushed block
			Block pushed  = piston.getRelative(face, 1);
			if (!whitelist.contains(pushed.getType()))
				return;
	
	        // try to store block and check remaining items
			if (((Hopper)against.getState()).getInventory().addItem(pushed.getState().getData().toItemStack(1)).isEmpty())
			{
				// avoid race conditions
				piston.setMetadata("pushing", new FixedMetadataValue(plugin, true));
				// having air in front of powered piston make extends itself
				pushed.setType(Material.AIR);
			}
		}
		else if (against.getType().equals(Material.OBSIDIAN))
		{
			// get whitelisted pushed block
			Block pushed  = piston.getRelative(face, 1);
			if (!whitelist.contains(pushed.getType()))
				return;
			
			// avoid race conditions
			piston.setMetadata("pushing", new FixedMetadataValue(plugin, true));
			// drop item, having air in front of powered piston make extends itself
			pushed.getWorld().dropItem(pushed.getLocation(), pushed.getState().getData().toItemStack(1));
			pushed.setType(Material.AIR);
		}
	}
	
	@EventHandler
	public void onPistonRetract(BlockPistonRetractEvent e)
	{
		if (e.getBlock().hasMetadata("pushing")) e.getBlock().removeMetadata("pushing", plugin);
	}
}
