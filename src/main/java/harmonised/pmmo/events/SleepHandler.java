package harmonised.pmmo.events;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.pmmo_saved_data.PmmoSavedData;
import harmonised.pmmo.util.DP;
import harmonised.pmmo.util.XP;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.event.world.SleepFinishedTimeEvent;

import java.util.Map;

public class SleepHandler
{
    public static void handleSleepFinished( SleepFinishedTimeEvent event )
    {
        double rechargeAmount = Config.forgeConfig.sleepVeinRestorePercent.get();
        if( rechargeAmount > 0 )
        {
            ( (World) event.getWorld() ).getServer().getPlayerList().getPlayers().forEach(player ->
            {
                Map<String, Double> configMap = Config.getAbilitiesMap( player );
                double maxVein = Config.forgeConfig.maxVeinCharge.get();
                double newVein = rechargeAmount * maxVein + configMap.getOrDefault( "veinLeft", 0D );
                configMap.put( "veinLeft", newVein );
                player.sendStatusMessage( new TranslationTextComponent( "pmmo.veinCharge", DP.dpSoft( ( newVein / maxVein ) * 100 ) ).setStyle( XP.getColorStyle( 0x00ff00 ) ), true );
            });
        }
    }
}
