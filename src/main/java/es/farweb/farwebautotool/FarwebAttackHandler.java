package es.farweb.farwebautotool;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.item.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;

public class FarwebAttackHandler {

    public static void register() {
        // Manejo de eventos con ClientTickEvents (obsoleto)
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null && client.crosshairTarget != null) {
                handleBlockBreaking(client);
                handleEntityAttack(client);
            }
        });
    }

    private static void handleBlockBreaking(MinecraftClient client) {
        if (client.crosshairTarget instanceof BlockHitResult hitResult) {
            BlockPos pos = hitResult.getBlockPos();
            if (pos != null && client.world != null) {
                BlockState blockState = client.world.getBlockState(pos);
                Block block = blockState.getBlock();

                if (block != null) {
                    ItemStack bestTool = getBestToolForBlock(client, blockState);
                    if (bestTool != null) {
                        client.player.getInventory().selectedSlot = client.player.getInventory().getSlotWithStack(bestTool);
                    }
                }
            }
        }
    }

    private static ItemStack getBestToolForBlock(MinecraftClient client, BlockState blockState) {
        float maxSpeed = 0.0F;
        ItemStack bestTool = ItemStack.EMPTY;

        for (ItemStack stack : client.player.getInventory().main) {
            if (!stack.isEmpty() && stack.isSuitableFor(blockState)) {
                float speed = stack.getMiningSpeedMultiplier(blockState);
                if (speed > maxSpeed) {
                    maxSpeed = speed;
                    bestTool = stack;
                }
            }
        }

        return bestTool;
    }

    private static void handleEntityAttack(MinecraftClient client) {
        if (client.targetedEntity instanceof HostileEntity) {
            ItemStack bestWeapon = getBestWeapon(client);
            if (bestWeapon != null) {
                client.player.getInventory().selectedSlot = client.player.getInventory().getSlotWithStack(bestWeapon);
            }
        }
    }

    private static ItemStack getBestWeapon(MinecraftClient client) {
        float maxDamage = 0.0F;
        ItemStack bestWeapon = ItemStack.EMPTY;

        for (ItemStack stack : client.player.getInventory().main) {
            if (!stack.isEmpty() && stack.getItem() instanceof MiningToolItem miningToolItem) {
                float damage = miningToolItem.getAttackDamage();
                if (damage > maxDamage) {
                    maxDamage = damage;
                    bestWeapon = stack;
                }
            } else if (stack.getItem() instanceof SwordItem swordItem) {
                float damage = swordItem.getAttackDamage();
                if (damage > maxDamage) {
                    maxDamage = damage;
                    bestWeapon = stack;
                }
            }
        }

        return bestWeapon;
    }
}
