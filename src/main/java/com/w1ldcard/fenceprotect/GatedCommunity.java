package com.w1ldcard.fenceprotect;

import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;

import org.bukkit.plugin.java.JavaPlugin;

public class GatedCommunity extends JavaPlugin {

	private SQLiteManager sqliteManager;
	private PlotManager plotManager;

	@Override
	public void onEnable() {

		this.sqliteManager = new SQLiteManager(this);
		this.plotManager = new PlotManager(this);

		try {
			this.sqliteManager.initializeDB();
			this.sqliteManager.loadPlots();
		} catch (SQLException | IOException e) {
			getLogger().log(Level.SEVERE, "Could not initialize plot DB, shutting down", e);
			return;
		}

		getServer().getPluginManager().registerEvents(new PlotCreateListener(this), this);
		getServer().getPluginManager().registerEvents(new PlotProtectListener(this), this);
	}

	@Override
	public void onDisable() {
		this.sqliteManager.closeDB();
	}

	public PlotManager getPlotManager() {
		return plotManager;
	}

	public SQLiteManager getSqliteManager() {
		return sqliteManager;
	}

}
