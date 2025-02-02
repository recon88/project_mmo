package harmonised.pmmo.events;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.JType;
import harmonised.pmmo.gui.WorldXpDrop;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.XP;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.event.entity.player.PlayerEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class CraftedHandler
{
    public static final Logger LOGGER = LogManager.getLogger();

    public static void handleCrafted( PlayerEvent.ItemCraftedEvent event )
    {
        try
        {
            if( event.getPlayer() instanceof ServerPlayerEntity )
            {
                ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
                Vector3d pos = player.getPositionVec();
                double defaultCraftingXp = Config.forgeConfig.defaultCraftingXp.get();
                double durabilityMultiplier = 1;

                ItemStack itemStack = event.getCrafting();
                ResourceLocation resLoc = itemStack.getItem().getRegistryName();
                Map<String, Double> xpValue = XP.getXp( XP.getResLoc( resLoc.toString() ), JType.XP_VALUE_CRAFT );

                Map<String, Double> award = new HashMap<>();
                if( xpValue.size() == 0 )
                {
                    if( itemStack.getItem() instanceof BlockItem)
                        award.put( "crafting", (double) ((BlockItem) itemStack.getItem()).getBlock().getDefaultState().getBlockHardness( null, null ) );
                    else
                        award.put( "crafting", defaultCraftingXp );
                }
                else
                    XP.addMapsAnyDouble( award, xpValue );

                if( itemStack.isDamageable() )
                    durabilityMultiplier = (double) ( itemStack.getMaxDamage() - itemStack.getDamage() ) / (double) itemStack.getMaxDamage();

//            XP.multiplyMap( award, itemStack.getCount() );
                XP.multiplyMapAnyDouble( award, durabilityMultiplier );

                for( String awardSkillName : award.keySet() )
                {
                    WorldXpDrop xpDrop = WorldXpDrop.fromXYZ( XP.getDimResLoc( player.getServerWorld() ), pos.getX(), pos.getY() + player.getEyeHeight() + 0.523, pos.getZ(), 1.523, award.get( awardSkillName ), awardSkillName );
                    XP.addWorldXpDrop( xpDrop, player );
                    Skill.addXp( awardSkillName, player, award.get( awardSkillName ), "crafting", false, false );
                }
            }
        }
        catch( Exception e )
        {
            LOGGER.error( "PMMO error while crafting", e );
        }
    }
}