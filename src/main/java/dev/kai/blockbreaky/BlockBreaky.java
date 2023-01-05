package dev.kai.blockbreaky;

import dev.kai.blockbreaky.util.ServerBlockTraverser;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.lifecycle.api.event.ServerWorldTickEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class BlockBreaky implements ModInitializer {
	// example of how to send message in chat
	// player.sendMessage(Text.of("this is sent from the server player entity"), false);

	// This logger is used to write text to the console and the log file.
	public static final Logger LOGGER = LoggerFactory.getLogger("Block Breaky");

	public static BlockBreakyConfig config = new BlockBreakyConfig();

	// Implementation of kotlin filterNotTo
	public <T> List<T> filterTo(List<T> toFilter, Predicate<T> predicate) {
		List<T> list = new ArrayList<>();
		for (T element : toFilter) {
			if (predicate.test(element)) {
				list.add(element);
			}
		}
		return list;
	}

	private final TagKey<Block> BREAKYABLES = TagKey.of(
			RegistryKeys.BLOCK,
			new Identifier("blockbreaky", "breakyable")
	);

	public boolean blockIsBreakyable(BlockState state) {
		return state.isIn(BREAKYABLES);
	}

	public boolean isMining = false;
	public List<Breaky> activeBreakys = new ArrayList<>();

//	private <T extends World> void debugMessage(T world, String message) {
//		List<? extends PlayerEntity> players = world.getPlayers();
//		if (players.size() > 0) {
//			players.get(0).sendMessage(Text.of(message), false);
//		}
//	}

	public void onBlockBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, BlockEntity blockEntity) {
		if (world.getClass().equals(ServerWorld.class) && player.getClass().equals(ServerPlayerEntity.class) && !isMining) {
			if (player.isSneaking()) {
				if (player.getMainHandStack().isSuitableFor(state) || player.isCreative()) {
					if (blockIsBreakyable(state)) {
						activeBreakys.add(new Breaky((ServerWorld) world, pos, (ServerPlayerEntity) player, state.getBlock()));
					}
				}
			}
		}
	}

	private void onEndWorldTick(MinecraftServer server, ServerWorld world) {
		if (activeBreakys.size() > 0) {
			activeBreakys = filterTo(activeBreakys, breaky -> !breaky.exhausted);

			isMining = true;
			for (Breaky breaky : activeBreakys) {
				if (breaky.world == world) {
					List<BlockPos> floodFillBlocks = ServerBlockTraverser.floodFill(breaky.origin, pos -> world.getBlockState(pos).getBlock() == breaky.toMine);

					floodFillBlocks.forEach(pos -> {
						BlockState state = world.getBlockState(pos);
						ServerPlayerEntity player = breaky.serverPlayer;
						boolean canMine = PlayerBlockBreakEvents.BEFORE.invoker().beforeBlockBreak(world, player, pos, state, world.getBlockEntity(pos));

						if (!canMine) {
							return;
						}
						if (player.getMainHandStack().isSuitableFor(state) || player.isCreative()) {
							// TODO: drop all blocks at seed block position
							player.interactionManager.tryBreakBlock(pos);
						}
					});
					breaky.exhausted = true;
				}
			}
			isMining = false;
		}
	}

	@Override
	public void onInitialize(ModContainer mod) {
		// LOGGER.info("Hello world from {}!", mod.metadata().name());

		PlayerBlockBreakEvents.AFTER.register(this::onBlockBreak);
		ServerWorldTickEvents.END.register(this::onEndWorldTick);
	}
}
