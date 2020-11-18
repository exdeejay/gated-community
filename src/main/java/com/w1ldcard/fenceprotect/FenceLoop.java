package com.w1ldcard.fenceprotect;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.block.Block;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

public class FenceLoop {

	private boolean closed = false;
	private List<Block> fences;
	private BoundingBox approxBounds;
	private List<Vector> bounds;

	private FenceLoop() {
		fences = new LinkedList<Block>();
		approxBounds = new BoundingBox();
	}

	public static FenceLoop buildLoop(Block fence) {
		return buildLoop(fence, null, false);
	}

	public static FenceLoop buildLoop(Block fence, GatedCommunity plugin, boolean async) {
		FenceLoop newLoop = new FenceLoop();
		newLoop.getFences().add(fence);
		newLoop.calculateLoop(plugin);
		newLoop.computeBounds();
		return newLoop;
	}

	public void calculateLoop() {
		calculateLoop(null);
	}

	public void calculateLoop(GatedCommunity plugin) {
		closed = false;
		if (fences.size() != 0) {
			Block first = fences.iterator().next();
			fences = new LinkedList<Block>();
			fences.add(first);
			List<Block> blocks = BlockUtil.getConnectedFences(first, plugin);
			for (Block b : blocks) {
				if (!fences.contains(b)) {
					List<Block> path = addIfConnected(b, first, first, new ArrayList<Block>(), plugin);
					if (path != null) {
						fences.addAll(path);
						System.out.println("Fence count " + fences.size());
						return;
					}
				}
			}
		}
	}

	private List<Block> addIfConnected(Block block, Block prev, Block first, List<Block> breadcrumbs,
			GatedCommunity plugin) {
		if (block.equals(first)) {
			boolean space = false;
			for (Block b : breadcrumbs) {
				if (b.getX() != first.getX() && b.getZ() != first.getZ()) {
					space = true;
					break;
				}
			}
			if (!space) {
				return null;
			}
			closed = true;
			return new ArrayList<Block>();
		}

		List<Block> fences = BlockUtil.getConnectedFences(block, plugin);
		List<Block> yBlocks = new ArrayList<Block>();
		if (fences.size() <= 1) {
			return null;
		} else if (fences.size() == 2) {
			if (breadcrumbs.contains(block)) {
				return null;
			}
			breadcrumbs.add(block);
			int index = fences.get(0).equals(prev) ? 1 : 0;
			List<Block> path = addIfConnected(fences.get(index), block, first, breadcrumbs, plugin);
			if (path != null) {
				path.add(block);
			}
			return path;
		} else {
			if (breadcrumbs.contains(block)) {
				return null;
			}
			breadcrumbs.add(block);
			List<Block> shortest = null;
			for (Block f : fences) {
				if (f.equals(prev)) {
					continue;
				}
				if (f.getY() != block.getY()) {
					yBlocks.add(f);
					continue;
				}
				List<Block> splitBreadcrumbs = new ArrayList<Block>(breadcrumbs);
				List<Block> path = addIfConnected(f, block, first, splitBreadcrumbs, plugin);
				if (path != null && (shortest == null || path.size() < shortest.size())) {
					shortest = path;
				}
			}
			if (shortest != null) {
				shortest.add(block);
			} else {
				for (Block f : yBlocks) {
					if (f.equals(prev)) {
						continue;
					}
					List<Block> splitBreadcrumbs = new ArrayList<Block>(breadcrumbs);
					List<Block> path = addIfConnected(f, block, first, splitBreadcrumbs, plugin);
					if (path != null && (shortest == null || path.size() < shortest.size())) {
						shortest = path;
					}
				}
			}
			return shortest;
		}
	}

	private void computeBounds() {
		Iterator<Block> it = fences.iterator();
		Block first = it.next();
		int minX = first.getX();
		int minZ = first.getZ();
		int maxX = minX;
		int maxZ = minZ;

		while (it.hasNext()) {
			Block fence = it.next();
			if (fence.getX() < minX) {
				minX = fence.getX();
			}
			if (fence.getZ() < minZ) {
				minZ = fence.getZ();
			}
			if (fence.getX() > maxX) {
				maxX = fence.getX();
			}
			if (fence.getZ() > maxZ) {
				maxZ = fence.getZ();
			}
		}

		bounds = new ArrayList<Vector>();
		for (Block f : fences) {
			Vector bound = f.getLocation().toVector();
			boolean contains = false;
			for (Vector b : bounds) {
				if (b.getX() == bound.getX() && b.getZ() == bound.getZ()) {
					contains = true;
					break;
				}
			}
			if (!contains) {
				bounds.add(bound);
			}
		}

		approxBounds = new BoundingBox(minX - 0.5, 25, minZ - 0.5, maxX + 0.5, 512, maxZ + 0.5);
	}

	public BoundingBox getApproxBounds() {
		return approxBounds;
	}

	public List<Block> getFences() {
		return fences;
	}

	public boolean isClosed() {
		return closed;
	}

	public boolean isFenceBorder(Block block) {
		if (approxBounds.contains(block.getLocation().toVector())) {
			for (Block f : fences) {
				if (block.getX() == f.getX() && block.getZ() == f.getZ()) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean contains(Block block) {
		if (approxBounds.contains(block.getLocation().toVector())) {
			for (Vector b : bounds) {
				if (block.getX() == (int) b.getX() && block.getZ() == (int) b.getZ()) {
					return true;
				}
			}

			int intersections = 0;
			int x = block.getX() + 1;
			int z = block.getZ();
			int wallLength = 0;

			// iterate in +x direction to find real bounds
			while (x <= approxBounds.getMaxX() + 1) {
				int lastWallLength = wallLength;
				// for each bound pillar
				for (Vector b : bounds) {
					// if x and z match, it's in the right pillar
					if (x == (int) b.getX() && z == (int) b.getZ()) {

						if (wallLength == 0) {
							// we're at the first wall in a chain
							intersections++;
						}
						wallLength++;
						break;
					}
				}

				// reset wall length on no fence found
				if (lastWallLength == wallLength) {
					if (wallLength > 1) {
						// end of a long fence, grab the end bit and calculate if within loop or not
						// iterate over fences to find connecting fences

						int beginWallDir = 0;
						int endWallDir = 0;

						for (Block fence : fences) {
							if (fence.getX() == x - 1) {
								if (fence.getZ() == z - 1) {
									endWallDir -= 1;
								} else if (fence.getZ() == z + 1) {
									endWallDir += 1;
								}
							} else if (fence.getX() == x - wallLength) {
								if (fence.getZ() == z - 1) {
									beginWallDir -= 1;
								} else if (fence.getZ() == z + 1) {
									beginWallDir += 1;
								}
							}
						}

						// if both are the same sign then there's no intersection
						if (Math.signum(beginWallDir) == Math.signum(endWallDir)) {
							intersections--;
						}
					}
					wallLength = 0;
				}
				x++;
			}

			if (intersections % 2 == 1) {
				// by intersection law, it's inside whatever freaky shape this could be
				return true;
			}
		}
		return false;
	}

}
