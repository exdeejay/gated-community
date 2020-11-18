package com.w1ldcard.fenceprotect;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;

import net.md_5.bungee.api.ChatColor;

public class FencePlot {

	private String owner;
	private List<String> whitelist;
	private FenceLoop territory;
	private Block sign;

	public FencePlot(String owner, List<String> whitelist, FenceLoop territory, Block sign) {
		this.owner = owner;
		this.whitelist = whitelist;
		this.territory = territory;
		this.sign = sign;
	}

	public String getOwner() {
		return owner;
	}

	public List<String> getWhitelist() {
		return whitelist;
	}

	public FenceLoop getTerritory() {
		return territory;
	}

	public Block getSign() {
		return sign;
	}

	public static void attemptCreatePlotFromSign(Block origin, SignChangeEvent event, GatedCommunity plugin) {
		new Thread(() -> {
			FenceLoop loop = FenceLoop.buildLoop(origin, plugin, true);
			if (loop.isClosed()) {
				Bukkit.getScheduler().runTask(plugin, () -> {
					Sign signData = (Sign) event.getBlock().getState();
					String[] whitelistStrings = event.getLines();
					if (!event.getPlayer().getName().equalsIgnoreCase(whitelistStrings[0])) {
						return;
					}
					signData.setLine(0, event.getPlayer().getName());
					
					List<String> whitelist = new ArrayList<String>();
					whitelist.add(event.getPlayer().getName());
					for (int i = 1; i < whitelistStrings.length; i++) {
						if (whitelistStrings[i].length() > 0) {
							Player player = Bukkit.getPlayer(whitelistStrings[i]);
							if (player == null) {
								event.getPlayer().sendMessage(
										ChatColor.RED + "Player name " + whitelistStrings[i] + " is invalid!");
								event.getBlock().breakNaturally();
								return;
							}
							whitelist.add(player.getName());
							signData.setLine(i, player.getName());
						}
					}

					if (plugin.getPlotManager().encompassesOtherPlot(loop)) {
						// TODO proper bounds checking
						event.getPlayer().sendMessage(ChatColor.RED + "Your plot is too close to another plot!");
						event.getBlock().breakNaturally();
						return;
					}
					FencePlot plot = new FencePlot(event.getPlayer().getName(), whitelist, loop, event.getBlock());

					try {
						plugin.getPlotManager().addPlot(plot);
					} catch (SQLException e) {
						event.getPlayer().sendMessage(ChatColor.RED
								+ "Something went wrong while trying to create a plot, please contact the server owner.");
						event.getBlock().breakNaturally();
						plugin.getLogger().log(Level.SEVERE, "Couldn't create plot", e);
						return;
					}

					signData.update();
					event.getPlayer().sendMessage(ChatColor.GRAY + "Plot created!");
					return;
				});
			}
		}).start();
	}

}
