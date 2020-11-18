package com.w1ldcard.fenceprotect;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.spigotmc.event.entity.EntityMountEvent;

import net.md_5.bungee.api.ChatColor;

public class PlotProtectListener implements Listener {

	private PlotManager pm;

	public PlotProtectListener(GatedCommunity plugin) {
		this.pm = plugin.getPlotManager();
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		if (!pm.isValidBlockInteract(event.getPlayer(), event.getBlock())) {
			event.setCancelled(true);
			event.getPlayer().sendMessage(ChatColor.GRAY + "You can't place that there!");
		}
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		if (!pm.isValidBlockInteract(event.getPlayer(), event.getBlock())) {
			event.setCancelled(true);
			event.getPlayer().sendMessage(ChatColor.GRAY + "You can't break that!");
		}
	}

	@EventHandler
	public void onBlockBurn(BlockBurnEvent event) {
		if (pm.whichPlot(event.getBlock()) != null) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onBlockIgnite(BlockIgniteEvent event) {
		if (!pm.isValidBlockInteract(event.getPlayer(), event.getBlock())) {
			event.setCancelled(true);
			event.getPlayer().sendMessage(ChatColor.GRAY + "You can't do that there!");
		}
	}

	@EventHandler
	public void onEntityChangeBlock(EntityChangeBlockEvent event) {
		if (pm.whichPlot(event.getBlock()) != null) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onEntityExplode(EntityExplodeEvent event) {
		for (Block b : event.blockList()) {
			if (pm.whichPlot(b) != null) {
				event.setCancelled(true);
			}
		}
	}

	@SuppressWarnings("rawtypes")
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (event.getClickedBlock() != null) {
			for (Class c : event.getClickedBlock().getBlockData().getClass().getInterfaces()) {
				if (c.getPackage().getName().endsWith(".type") && !c.getSimpleName().equalsIgnoreCase("Gate")) {
					if (!pm.isValidBlockInteract(event.getPlayer(), event.getClickedBlock())) {
						event.setCancelled(true);
						event.getPlayer().sendMessage(ChatColor.GRAY + "You can't interact there!");
						return;
					}
				}
			}
		}
	}

	@EventHandler
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
		if (!pm.isValidBlockInteract(event.getPlayer(), event.getRightClicked().getLocation().getBlock())) {
			event.setCancelled(true);
			event.getPlayer().sendMessage(ChatColor.GRAY + "You can't interact with that here!");
		}
	}

	@EventHandler
	public void onPlayerArmorStandManipulate(PlayerArmorStandManipulateEvent event) {
		if (!pm.isValidBlockInteract(event.getPlayer(), event.getRightClicked().getLocation().getBlock())) {
			event.setCancelled(true);
			event.getPlayer().sendMessage(ChatColor.GRAY + "You can't interact there!");
		}
	}

	@EventHandler
	public void onHangingBreakByEntity(HangingBreakByEntityEvent event) {
		if (!(event.getRemover() instanceof Player)
				|| !pm.isValidBlockInteract((Player) event.getRemover(), event.getEntity().getLocation().getBlock())) {
			event.setCancelled(true);
			((Player) event.getRemover()).sendMessage(ChatColor.GRAY + "You can't break that!");
		}
	}

	@EventHandler
	public void onHangingPlace(HangingPlaceEvent event) {
		if (!pm.isValidBlockInteract(event.getPlayer(), event.getEntity().getLocation().getBlock())) {
			event.setCancelled(true);
			event.getPlayer().sendMessage(ChatColor.GRAY + "You can't break that!");
		}
	}

	@EventHandler
	public void onBlockPistonExtendEvent(BlockPistonExtendEvent event) {
		for (Block b : event.getBlocks()) {
			if (pm.whichPlot(b) != null) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onBlockPistonRetractEvent(BlockPistonRetractEvent event) {
		for (Block b : event.getBlocks()) {
			if (pm.whichPlot(b) != null) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if (event.getDamager() instanceof Player
				&& !pm.isValidBlockInteract((Player) event.getDamager(), event.getEntity().getLocation().getBlock())) {
			event.setCancelled(true);
			((Player) event.getDamager()).sendMessage(ChatColor.GRAY + "You can't do that here!");
		}
	}

	@EventHandler
	public void onEntityMount(EntityMountEvent event) {
		if (event.getEntity() instanceof Player
				&& !pm.isValidBlockInteract((Player) event.getEntity(), event.getMount().getLocation().getBlock())) {
			event.setCancelled(true);
			((Player) event.getEntity())
					.sendMessage(ChatColor.GRAY + "You can't ride mounts inside someone else's plot!");
		}
	}

	@EventHandler
	public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
		if (!pm.isValidBlockInteract(event.getPlayer(), event.getBlock())) {
			event.setCancelled(true);
			event.getPlayer().sendMessage(ChatColor.GRAY + "You can't place that here!");
		}
	}

	@EventHandler
	public void onPlayerBucketFill(PlayerBucketFillEvent event) {
		if (!pm.isValidBlockInteract(event.getPlayer(), event.getBlock())) {
			event.setCancelled(true);
			event.getPlayer().sendMessage(ChatColor.GRAY + "You can't do that here!");
		}
	}

	@EventHandler
	public void onBlockFromTo(BlockFromToEvent event) {
		if (!pm.isValidBlockInteract(null, event.getBlock()) || !pm.isValidBlockInteract(null, event.getToBlock())) {
			event.setCancelled(true);
		}
	}

}
