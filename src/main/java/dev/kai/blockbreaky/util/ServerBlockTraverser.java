package dev.kai.blockbreaky.util;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.*;
import java.util.function.Predicate;

import static dev.kai.blockbreaky.BlockBreaky.config;

public class ServerBlockTraverser {
	static int maxTraversed = config.maxBlocks;

	private static List<BlockPos> getNeighbors(BlockPos pos, boolean includeDiagonals) {
		List<BlockPos> neighbors = new ArrayList<>();
		// Six direct neighbors
		neighbors.add(pos.north());
		neighbors.add(pos.east());
		neighbors.add(pos.south());
		neighbors.add(pos.west());
		neighbors.add(pos.up());
		neighbors.add(pos.down());
		if (includeDiagonals) {
			// Top layer
			neighbors.add(pos.up().north());
			neighbors.add(pos.up().east());
			neighbors.add(pos.up().south());
			neighbors.add(pos.up().west());

			neighbors.add(pos.up().north().east());
			neighbors.add(pos.up().north().west());
			neighbors.add(pos.up().south().east());
			neighbors.add(pos.up().south().west());

			// Middle layer
			neighbors.add(pos.north().east());
			neighbors.add(pos.north().west());
			neighbors.add(pos.south().east());
			neighbors.add(pos.south().west());

			// Bottom layer
			neighbors.add(pos.down().north());
			neighbors.add(pos.down().east());
			neighbors.add(pos.down().south());
			neighbors.add(pos.down().west());

			neighbors.add(pos.down().north().east());
			neighbors.add(pos.down().north().west());
			neighbors.add(pos.down().south().east());
			neighbors.add(pos.down().south().west());
		}

		return neighbors;
	}

	public static List<BlockPos> floodFill(BlockPos seed, Predicate<BlockPos> predicate) {
		Queue<BlockPos> searching = new ArrayDeque<>();
		List<BlockPos> searched = new ArrayList<>();
		List<BlockPos> passed = new ArrayList<>();
		searching.add(seed);
		passed.add(seed);

		while (searched.size() < maxTraversed && searching.size() > 0) {
			BlockPos node = searching.remove();
			if (!searched.contains(node)) {
				List<BlockPos> neighbors = getNeighbors(node, true);
				for (BlockPos neighbor : neighbors) {
					if (predicate.test(neighbor) && !searched.contains(neighbor)) {
						searching.add(neighbor);
						passed.add(neighbor);
					}
				}
			}
			searched.add(node);
		}
		return passed;
	}
}
