package com.w1ldcard.fenceprotect;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;

import net.md_5.bungee.api.ChatColor;

public class PlotCreateListener implements Listener {

	private GatedCommunity plugin;

	public PlotCreateListener(GatedCommunity plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onSignEdit(SignChangeEvent event) {
		List<Block> fences = BlockUtil.getConnectedFences(event.getBlock(), plugin);
		if (fences.size() != 0) {
			FencePlot.attemptCreatePlotFromSign(fences.get(0), event, plugin);
		}
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		try {
			PlotManager pm = plugin.getPlotManager();
			if (BlockUtil.isFence(event.getBlock())) {
				FencePlot plot = pm.whichPlotFence(event.getBlock());
				if (plot != null && (plot.getWhitelist().contains(event.getPlayer().getName())
						|| Bukkit.getOperators().contains(event.getPlayer()))) {
					FenceLoop loop = plot.getTerritory();
					loop.getFences().remove(event.getBlock());
					event.getBlock().breakNaturally();
					new Thread(() -> {
						loop.calculateLoop(plugin);
						if (!loop.isClosed()) {
							Bukkit.getScheduler().runTask(plugin, () -> {
								try {
									plugin.getPlotManager().removePlot(plot);
								} catch (SQLException e) {
									event.getPlayer().sendMessage(ChatColor.RED
											+ "Something went wrong while trying to destroy a plot, please contact the server owner.");
									event.getPlayer().sendMessage(ChatColor.GRAY
											+ "Honestly, I'm just as confused as you are. This should never happen.");
									event.getBlock().breakNaturally();
									return;
								}
								event.getPlayer().sendMessage(ChatColor.GRAY + "Plot removed!");
							});
						}
					}).start();
				}
			}
			if (BlockUtil.isSign(event.getBlock())) {
				FencePlot plot = pm.whichPlotSign(event.getBlock());
				if (plot != null && (plot.getWhitelist().contains(event.getPlayer().getName())
						|| Bukkit.getOperators().contains(event.getPlayer()))) {
					event.setCancelled(true);
					try {
						plugin.getPlotManager().removePlot(plot);
					} catch (SQLException e) {
						event.getPlayer().sendMessage(ChatColor.RED
								+ "Something went wrong while trying to destroy a plot, please contact the server owner.");
						event.getPlayer().sendMessage(ChatColor.GRAY
								+ "Honestly, I'm just as confused as you are. This should never happen.");
						event.getBlock().breakNaturally();
						return;
					}
					event.getPlayer().sendMessage(ChatColor.GRAY + "Plot removed!");
				}
			}
		} catch (Throwable e) {
			event.getPlayer().sendMessage(ChatColor.RED
					+ "Something went wrong while trying to destroy a plot, please contact the server owner!");
			event.getPlayer().sendMessage(
					ChatColor.GRAY + "Honestly, I'm just as confused as you are. This should never happen.");
			plugin.getLogger().log(Level.SEVERE, "Couldn't destroy plot", e);
		}
	}

	// TODO fence with already placed sign checking
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {

	}

}
