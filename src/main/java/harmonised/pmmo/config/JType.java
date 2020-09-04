package harmonised.pmmo.config;

import harmonised.pmmo.skills.Skill;

import java.util.HashMap;
import java.util.Map;

public enum JType
{
    NONE(0),
    REQ_WEAR(1),
    REQ_TOOL(2),
    REQ_WEAPON(3),
    REQ_USE(4),
    REQ_PLACE(5),
    REQ_BREAK(6),
    REQ_BIOME(7),
    REQ_KILL(8),
    REQ_CRAFT(9),
    XP_VALUE_GENERAL(10),
    XP_VALUE_BREAK(11),
    XP_VALUE_CRAFT(12),
    XP_VALUE_BREED(13),
    XP_VALUE_TAME(14),
    XP_VALUE_KILL(15),
    XP_VALUE_SMELT(16),
    XP_VALUE_COOK(17),
    XP_VALUE_TRIGGER(18),
    XP_VALUE_BREW(19),
    XP_VALUE_GROW(20),
    INFO_ORE(21),
    INFO_LOG(22),
    INFO_PLANT(23),
    INFO_SMELT(24),
    INFO_COOK(25),
    INFO_BREW(26),
    BIOME_EFFECT(27),
    BIOME_MOB_MULTIPLIER(28),
    XP_BONUS_BIOME(29),
    XP_BONUS_HELD(30),
    XP_BONUS_WORN(31),
    SALVAGE_TO(32),
    FISH_POOL(33),
    FISH_ENCHANT_POOL(34),
    MOB_RARE_DROP(35),
    LEVEL_UP_COMMAND(36),
    PLAYER_SPECIFIC(37),
    BLOCK_SPECIFIC(38),
    VEIN_BLACKLIST(39),
    THREE_LAYER_DATA( 100 ),
    SALVAGE_FROM(101),
    STATS(102),
    DIMENSION(103),
    CREDITS(104),
    SETTINGS(105),
    GUI_SETTINGS(106);

    public static final Map< JType, Integer > jTypeMap = new HashMap<>();
    public static final Map< Integer, JType > intMap = new HashMap<>();
    public static final Map< String, JType > stringMap = new HashMap<>();
    private final int value;

    JType( int value )
    {
        this.value = value;
    }

    static
    {
        for( JType jType : JType.values() )
        {
            jTypeMap.put( jType, jType.value );
            intMap.put( jType.value, jType );
            stringMap.put( jType.name().toLowerCase(), jType );
        }
    }

    public int getValue()
    {
        return this.value;
    }

    @Override
    public String toString()
    {
        return this.name().toLowerCase();
    }

    public static JType getJType( String str )
    {
        return stringMap.get( str );
    }

    public static JType getJType( int i )
    {
        return intMap.get( i );
    }
}
