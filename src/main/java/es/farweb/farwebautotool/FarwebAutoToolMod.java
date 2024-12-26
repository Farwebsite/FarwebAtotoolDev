package es.farweb.farwebautotool;

import net.fabricmc.api.ClientModInitializer;

public class FarwebAutoToolMod implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        System.out.println("[FarwebAutoToolMod] Mod inicializado en cliente.");
        FarwebAttackHandler.register();
    }
}
