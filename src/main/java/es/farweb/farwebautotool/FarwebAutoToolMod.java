package es.farweb.farwebautotool;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;

/**
 * FarwebAutoToolMod
 * Punto de entrada principal de tu mod (v1.0.0).
 */
public class FarwebAutoToolMod implements ModInitializer {

    @Override
    public void onInitialize(ModContainer mod) {
        System.out.println("[FarwebAutoToolMod] Iniciando Farweb Auto Tool Mod - v1.0.0!");

        // Registramos la lógica de auto-herramienta y auto-arma
        FarwebAttackHandler.register();
    }
}
