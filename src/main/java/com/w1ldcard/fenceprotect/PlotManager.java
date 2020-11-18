package com.w1ldcard.fenceprotect;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Sign;
import org.bukkit.entity.Player;

public class PlotManager {

	private GatedCommunity plugin;
	private List<FencePlot> plots;

	public PlotManager(GatedCommunity plugin) {
		this.plugin = plugin;
		this.plots = new LinkedList<FencePlot>();
	}

	public void addPlot(FencePlot plot) throws SQLException {
		addPlot(plot, true);
	}

	public void addPlot(FencePlot plot, boolean updateDb) throws SQLException {
		if (updateDb) {
			plugin.getSqliteManager().addPlot(plot.getOwner().toString(), plot.getSign().getWorld().getName(),
					plot.getSign().getX(), plot.getSign().getY(), plot.getSign().getZ());
		}
		plots.add(plot);
		plugin.getLogger().fine("Plot created by " + plot.getOwner() + " at " + plot.getSign().getLocation());
	}

	public boolean removePlot(FencePlot plot) throws SQLException {
		return removePlot(plot, true);
	}

	public boolean removePlot(FencePlot plot, boolean updateDb) throws SQLException {
		if (plots.contains(plot)) {
			if (updateDb) {
				plugin.getSqliteManager().removePlot(plot.getOwner().toString(), plot.getSign().getWorld().getName(),
						plot.getSign().getX(), plot.getSign().getY(), plot.getSign().getZ());
			}
			plots.remove(plot);
			plot.getSign().breakNaturally();
			plugin.getLogger().fine("Plot owned by player " + plot.getOwner().toString() + " at "
					+ plot.getSign().getLocation() + " was removed");
			return true;
		}
		plugin.getLogger().warning("Attempted to remove plot owned by player " + plot.getOwner().toString() + " at "
				+ plot.getSign().getLocation() + " where none was found");
		return false;
	}

	public boolean encompassesOtherPlot(FenceLoop loop) {
		for (FencePlot p : plots) {
			if (p.getTerritory().getApproxBounds().overlaps(loop.getApproxBounds())) {
				return true;
			}
		}
		return false;
	}

	public FencePlot whichPlot(Block block) {
		for (FencePlot plot : plots) {
			if (plot.getTerritory().contains(block)) {
				return plot;
			}
		}
		return null;
	}

	public boolean isValidBlockInteract(Player player, Block block) {
		if (Bukkit.getOperators().contains(player)) {
			return true;
		}
		for (FencePlot plot : plots) {
			if (player != null && plot.getWhitelist().contains(player.getName())) {
				continue;
			}
			Block blockUnderSign = plot.getSign().getLocation().add(0, -1, 0).getBlock();
			if (plot.getSign().equals(block)
					|| (plot.getSign().getBlockData() instanceof Sign && blockUnderSign.equals(block))
					|| plot.getTerritory().contains(block)) {
				return false;
			}
		}
		return true;
	}

	public FencePlot whichPlotFence(Block block) {
		for (FencePlot p : plots) {
			if (p.getTerritory().isFenceBorder(block)) {
				return p;
			}
		}
		return null;
	}

	public FencePlot whichPlotSign(Block block) {
		for (FencePlot p : plots) {
			if (p.getSign().equals(block)) {
				return p;
			}
		}
		return null;
	}

}
