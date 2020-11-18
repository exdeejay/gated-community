package com.w1ldcard.fenceprotect;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

public class SQLiteManager {
	private GatedCommunity plugin;
	private Connection conn = null;
	private PreparedStatement addPlotStmt = null;
	private PreparedStatement removePlotStmt = null;

	public SQLiteManager(GatedCommunity plugin) {
		this.plugin = plugin;
	}

	public void initializeDB() throws SQLException, IOException {
		Path pluginDir = plugin.getDataFolder().toPath();
		if (!Files.isDirectory(pluginDir)) {
			Files.createDirectories(pluginDir);
		}
		conn = DriverManager.getConnection("jdbc:sqlite:" + plugin.getDataFolder() + "/community.db");
		Statement stmt = conn.createStatement();
		stmt.executeUpdate(
				"CREATE TABLE IF NOT EXISTS plots (owner TEXT, world TEXT, x INTEGER, y INTEGER, z INTEGER)");
		addPlotStmt = conn.prepareStatement("INSERT INTO plots VALUES (?, ?, ?, ?, ?)");
		removePlotStmt = conn
				.prepareStatement("DELETE FROM plots WHERE owner = ? AND world = ? AND x = ? AND y = ? AND z = ?");
	}

	public void loadPlots() throws SQLException {
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT * FROM plots");
		while (rs.next()) {
			String ownersName = rs.getString(1);
			String worldName = rs.getString(2);
			int x = rs.getInt(3);
			int y = rs.getInt(4);
			int z = rs.getInt(5);

			try {
				World world = Bukkit.getWorld(worldName);
				Block sign = world.getBlockAt(x, y, z);

				String[] whitelistNames = ((Sign) sign.getState()).getLines();
				List<String> whitelist = new ArrayList<String>();
				for (String s : whitelistNames) {
					if (s.length() > 0) {
						whitelist.add(s);
					}
				}
				FenceLoop loop = FenceLoop.buildLoop(BlockUtil.getConnectedFences(sign, plugin).get(0));

				FencePlot plot = new FencePlot(whitelist.get(0), whitelist, loop, sign);
				plugin.getPlotManager().addPlot(plot, false);
			} catch (Throwable e) {
				plugin.getLogger().warning(String.format(
						"Could not rebuild plot of player %s at location [x: %d, y: %d, z: %d], was the plugin updated?",
						ownersName, x, y, z));
			}
		}
		stmt.close();

	}

	public void addPlot(String owner, String world, int x, int y, int z) throws SQLException {
		addPlotStmt.setString(1, owner);
		addPlotStmt.setString(2, world);
		addPlotStmt.setInt(3, x);
		addPlotStmt.setInt(4, y);
		addPlotStmt.setInt(5, z);
		addPlotStmt.executeUpdate();
	}

	public void removePlot(String owner, String world, int x, int y, int z) throws SQLException {
		removePlotStmt.setString(1, owner);
		removePlotStmt.setString(2, world);
		removePlotStmt.setInt(3, x);
		removePlotStmt.setInt(4, y);
		removePlotStmt.setInt(5, z);
		removePlotStmt.executeUpdate();
	}

	public void closeDB() {
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
}
