package dev.kai.blockbreaky;

import net.minecraft.block.Block;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class Breaky {
	public ServerWorld world;
	public BlockPos origin;
	public ServerPlayerEntity serverPlayer;
	public Block toMine;
	public boolean exhausted;

	public Breaky(ServerWorld world, BlockPos origin, ServerPlayerEntity serverPlayer, Block toMine) {
		this.world = world;
		this.origin = origin;
		this.serverPlayer = serverPlayer;
		this.toMine = toMine;
		this.exhausted = false;
	}
}
