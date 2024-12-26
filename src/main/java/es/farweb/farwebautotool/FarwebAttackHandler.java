package es.farweb.farwebautotool;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.item.*;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import org.lwjgl.glfw.GLFW;
import org.quiltmc.qsl.lifecycle.api.client.event.ClientBlockAttackEvents;
import org.quiltmc.qsl.input.api.client.event.MouseButtonPressed;

import java.util.HashMap;
import java.util.Map;

/**
 * FarwebAttackHandler
 * Maneja la lógica de:
 * 1) Auto-herramienta al romper bloques.
 * 2) Auto-arma al atacar entidades hostiles.
 */
public class FarwebAttackHandler {

    // Tags para herramientas
    private static final TagKey<net.minecraft.block.Block> MINEABLE_PICKAXE = TagKey.of(
            RegistryKeys.BLOCK, new Identifier("minecraft", "mineable/pickaxe")
    );
    private static final TagKey<net.minecraft.block.Block> MINEABLE_AXE = TagKey.of(
            RegistryKeys.BLOCK, new Identifier("minecraft", "mineable/axe")
    );
    private static final TagKey<net.minecraft.block.Block> MINEABLE_SHOVEL = TagKey.of(
            RegistryKeys.BLOCK, new Identifier("minecraft", "mineable/shovel")
    );
    private static final TagKey<net.minecraft.block.Block> MINEABLE_HOE = TagKey.of(
            RegistryKeys.BLOCK, new Identifier("minecraft", "mineable/hoe")
    );

    /**
     * Registrar los eventos necesarios (romper bloque y clic de mouse).
     */
    public static void register() {
        // Evento al iniciar rotura de bloque
        ClientBlockAttackEvents.START.register(FarwebAttackHandler::onBlockStartBreak);
        // Evento de clic de mouse => para atacar mobs hostiles
        MouseButtonPressed.EVENT.register(FarwebAttackHandler::onMouseClick);
    }

    // ------------------------------------------
    // 1 Romper Bloque => Auto-herramienta
    // ------------------------------------------
    private static ActionResult onBlockStartBreak(BlockPos pos, Direction direction) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.world == null || client.player == null) {
            return ActionResult.PASS;
        }

        World world = client.world;
        BlockState blockState = world.getBlockState(pos);

        Class<? extends Item> toolClass = getToolClassForBlock(blockState);
        if (toolClass == null) {
            // Si no requiere herramienta particular, no hacemos nada.
            return ActionResult.PASS;
        }

        // Elegimos la "mejor" herramienta de ese tipo en la hotbar
        equipBestToolInHotbar(toolClass);

        return ActionResult.PASS;
    }

    /**
     * Determina la clase de la herramienta (PickaxeItem, AxeItem, ShovelItem, HoeItem)
     * que requiere un bloque, basándose en las tags vanilla: "mineable/pickaxe", etc.
     */
    private static Class<? extends Item> getToolClassForBlock(BlockState state) {
        if (state.isIn(MINEABLE_PICKAXE)) {
            return PickaxeItem.class;
        } else if (state.isIn(MINEABLE_AXE)) {
            return AxeItem.class;
        } else if (state.isIn(MINEABLE_SHOVEL)) {
            return ShovelItem.class;
        } else if (state.isIn(MINEABLE_HOE)) {
            return HoeItem.class;
        }
        // Podrías añadir casos especiales (ShearsItem) si quisieras
        return null;
    }

    /**
     * Recorre los 9 slots de la hotbar y busca la herramienta del tipo 'toolClass' 
     * con el mayor "harvest level" (por ejemplo, netherite > diamond > iron > stone > wood).
     */
    private static void equipBestToolInHotbar(Class<? extends Item> toolClass) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) return;

        int bestSlot = -1;
        int bestLevel = -1;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = client.player.getInventory().getStack(i);
            // Comprobar si la clase coincide EXACTAMENTE
            if (toolClass.isAssignableFrom(stack.getItem().getClass())) {
                int level = getHarvestLevel(stack.getItem());
                if (level > bestLevel) {
                    bestLevel = level;
                    bestSlot = i;
                }
            }
        }

        // Si encontramos alguna herramienta, cambiamos a ese slot
        if (bestSlot != -1) {
            client.player.getInventory().selectedSlot = bestSlot;
        }
    }

    // ------------------------------------------
    // 2) Atacar Entidad => Auto-arma
    // ------------------------------------------
    private static void onMouseClick(MinecraftClient client, int button, int action, int mods) {
        // Detectamos el clic izquierdo (GLFW.GLFW_MOUSE_BUTTON_LEFT) presionado
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && action == GLFW.GLFW_PRESS) {
            if (client == null || client.world == null || client.player == null) return;

            HitResult hitResult = client.crosshairTarget;
            if (hitResult instanceof EntityHitResult entityHit) {
                // Verificar si la entidad es hostil
                if (entityHit.getEntity() instanceof HostileEntity) {
                    // 1) Intentar equipar la mejor espada
                    boolean foundSword = equipBestWeaponInHotbar(SwordItem.class);
                    // 2) Si no hay espada, usar la mejor hacha
                    if (!foundSword) {
                        equipBestWeaponInHotbar(AxeItem.class);
                    }
                }
            }
        }
    }

    /**
     * Busca en la hotbar el arma del tipo 'weaponClass' (SwordItem o AxeItem)
     * con el mayor 'harvest level' (aprovechamos la misma función de nivel).
     * Devuelve true si encontró y equipó algo, false en caso contrario.
     */
    private static boolean equipBestWeaponInHotbar(Class<? extends Item> weaponClass) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) return false;

        int bestSlot = -1;
        int bestLevel = -1;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = client.player.getInventory().getStack(i);
            if (weaponClass.isAssignableFrom(stack.getItem().getClass())) {
                int level = getHarvestLevel(stack.getItem());
                if (level > bestLevel) {
                    bestLevel = level;
                    bestSlot = i;
                }
            }
        }

        if (bestSlot != -1) {
            client.player.getInventory().selectedSlot = bestSlot;
            return true;
        }
        return false;
    }

    // ------------------------------------------
    // Utilidad: Asignar "harvest level" según material
    // ------------------------------------------
    /**
     * Retorna un número que representa la "calidad" o "nivel" del material.
     * Cuanto más alto, "mejor" la herramienta/arma.
     */
    private static int getHarvestLevel(Item item) {
        if (!(item instanceof ToolItem toolItem)) {
            return 0;
        }

        ToolMaterial mat = toolItem.getMaterial();
        // Mapa básico de materiales a un número de nivel
        Map<String, Integer> materialMap = new HashMap<>();
        materialMap.put("wood", 1);
        materialMap.put("stone", 2);
        materialMap.put("iron", 3);
        materialMap.put("gold", 3);      // El oro es un caso especial: su harvest level es bajo, pero es rápido.
        materialMap.put("diamond", 4);
        materialMap.put("netherite", 5);

        // El .toString() en la mayoría de ToolMaterial regresa algo como "WOOD", "STONE", etc.
        String materialName = mat.toString().toLowerCase();

        return materialMap.getOrDefault(materialName, 0);
    }
}
