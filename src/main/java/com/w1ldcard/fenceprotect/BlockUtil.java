package com.w1ldcard.fenceprotect;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Fence;
import org.bukkit.block.data.type.Gate;
import org.bukkit.block.data.type.Sign;
import org.bukkit.block.data.type.Wall;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.plugin.Plugin;

public class BlockUtil {

	public static boolean isFence(Block block) {
		return block.getBlockData() instanceof Fence || block.getBlockData() instanceof Wall
				|| block.getBlockData() instanceof Gate;
	}

	public static boolean isSign(Block block) {
		return block.getBlockData() instanceof Sign || block.getBlockData() instanceof WallSign;
	}

	public static List<Block> getConnectedFences(Block block, Plugin plugin) {
		if (!Bukkit.isPrimaryThread()) {
			try {
				return Bukkit.getScheduler().callSyncMethod(plugin, new Callable<List<Block>>() {
					@Override
					public List<Block> call() throws Exception {
						return _getConnectedFences(block);
					}
				}).get();
			} catch (InterruptedException | ExecutionException e) {
				plugin.getLogger().log(Level.SEVERE, "Could not get connected fences asynchronously", e);
				return null;
			}
		} else {
			return _getConnectedFences(block);
		}
	}

	private static List<Block> _getConnectedFences(Block block) {
		List<Block> connected = new ArrayList<Block>();
		World blockWorld = block.getWorld();
		int x = block.getX();
		int y = block.getY();
		int z = block.getZ();
		Block xp = blockWorld.getBlockAt(x + 1, y, z);
		if (isFence(xp)) {
			connected.add(xp);
		}
		Block xn = blockWorld.getBlockAt(x - 1, y, z);
		if (isFence(xn)) {
			connected.add(xn);
		}
		Block zp = blockWorld.getBlockAt(x, y, z + 1);
		if (isFence(zp)) {
			connected.add(zp);
		}
		Block zn = blockWorld.getBlockAt(x, y, z - 1);
		if (isFence(zn)) {
			connected.add(zn);
		}
		Block yp = blockWorld.getBlockAt(x, y + 1, z);
		if (isFence(yp)) {
			connected.add(yp);
		}
		Block yn = blockWorld.getBlockAt(x, y - 1, z);
		if (isFence(yn)) {
			connected.add(yn);
		}
		return connected;
	}

}
