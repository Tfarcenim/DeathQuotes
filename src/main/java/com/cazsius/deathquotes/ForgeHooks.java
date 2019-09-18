package com.cazsius.deathquotes;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;


import java.util.Random;

public class ForgeHooks {

    @SubscribeEvent
    public void LivingDeathEvent(LivingDeathEvent event) {
        if (event.getEntity().world.isRemote) {
            return;
        } // server side only
        if (!(event.getEntity() instanceof PlayerEntity)) {
            return;
        } // for players only
        PlayerEntity player = (PlayerEntity) event.getEntity();
        Random randomGenerator = new Random();
        if (DeathQuotes.quotes.length == 0) // if no quotes in array
        {
            Do.Err(player, "file " + DeathQuotes.quotesPathAndFileName + " contains no quotes. Delete it and restart for default quotes.");
            return;
        }
        int tryCount = 0;
        int n = 0;
        boolean foundOne = false;
        while (!foundOne) {
            tryCount++;
            if (tryCount > 10) {
                return;
            } // prevent infinite loop when only blank lines found
            n = randomGenerator.nextInt(DeathQuotes.quotes.length);
            if (!DeathQuotes.quotes[n].trim().equals("")) {
                foundOne = true;
            } else {
                Do.Err(player, "file " + DeathQuotes.quotesPathAndFileName + " contains blank lines and it should not.");
            }
        }
        Do.SayToAll(player, "\"" + DeathQuotes.quotes[n] + "\"");
        return;
    }
}
