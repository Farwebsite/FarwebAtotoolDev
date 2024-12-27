package es.farweb.farwebautotool;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;

public class FarwebAttackHandler {

    /**
     * Maneja la lógica de romper bloques en función del mejor instrumento disponible.
     * 
     * @param minecraft Instancia del cliente Minecraft.
     */
    public static void handleBlockBreaking(Minecraft minecraft) {
        if (minecraft.hitResult instanceof BlockHitResult hitResult && minecraft.player != null) {
            BlockPos blockPos = hitResult.getBlockPos();
            BlockState blockState = minecraft.level.getBlockState(blockPos);

            ItemStack bestTool = getBestToolForBlock(minecraft, blockState);
            if (bestTool != null) {
                // Cambiar a la herramienta óptima
                minecraft.player.getInventory().selected = findToolSlot(minecraft, bestTool);
            }
        }
    }

    /**
     * Encuentra la mejor herramienta para romper un bloque específico.
     * 
     * @param minecraft Instancia del cliente Minecraft.
     * @param blockState Estado del bloque.
     * @return La herramienta más adecuada.
     */
    private static ItemStack getBestToolForBlock(Minecraft minecraft, BlockState blockState) {
        ItemStack bestTool = ItemStack.EMPTY;
        float bestSpeed = 0.0f;

        if (minecraft.player != null) {
            for (ItemStack itemStack : minecraft.player.getInventory().items) {
                if (!itemStack.isEmpty() && itemStack.getDestroySpeed(blockState) > bestSpeed) {
                    bestSpeed = itemStack.getDestroySpeed(blockState);
                    bestTool = itemStack;
                }
            }
        }
        return bestTool;
    }

    /**
     * Maneja la lógica de atacar entidades en función del mejor arma disponible.
     * 
     * @param minecraft Instancia del cliente Minecraft.
     */
    public static void handleEntityAttack(Minecraft minecraft) {
        if (minecraft.hitResult instanceof BlockHitResult && minecraft.player != null) {
            ItemStack bestWeapon = getBestWeapon(minecraft);
            if (bestWeapon != null) {
                // Cambiar al arma óptima
                minecraft.player.getInventory().selected = findToolSlot(minecraft, bestWeapon);
            }
        }
    }

    /**
     * Encuentra la mejor arma para atacar.
     * 
     * @param minecraft Instancia del cliente Minecraft.
     * @return La mejor arma disponible.
     */
    private static ItemStack getBestWeapon(Minecraft minecraft) {
        ItemStack bestWeapon = ItemStack.EMPTY;
        float bestDamage = 0.0f;

        if (minecraft.player != null) {
            for (ItemStack itemStack : minecraft.player.getInventory().items) {
                if (!itemStack.isEmpty() && itemStack.getItem() instanceof SwordItem sword) {
                    float damage = sword.getAttackDamage();
                    if (damage > bestDamage) {
                        bestDamage = damage;
                        bestWeapon = itemStack;
                    }
                }
            }
        }
        return bestWeapon;
    }

    /**
     * Encuentra la ranura en el inventario donde está la herramienta o arma.
     * 
     * @param minecraft Instancia del cliente Minecraft.
     * @param item      La herramienta o arma a buscar.
     * @return El índice de la ranura donde está el ítem.
     */
    private static int findToolSlot(Minecraft minecraft, ItemStack item) {
        if (minecraft.player != null) {
            for (int i = 0; i < minecraft.player.getInventory().items.size(); i++) {
                if (minecraft.player.getInventory().items.get(i).equals(item)) {
                    return i;
                }
            }
        }
        return 0; // Por defecto, volver a la ranura 0 si no se encuentra
    }
}
