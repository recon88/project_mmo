package harmonised.pmmo.config;

import com.google.common.collect.Maps;
import com.google.gson.*;
import harmonised.pmmo.ProjectMMOMod;
import harmonised.pmmo.skills.Skill;
import net.minecraft.util.JSONUtils;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class Requirements
{
    public static Map<String, Map<String, Object>> wearReq = new HashMap<>();
    public static Map<String, Map<String, Object>> toolReq = new HashMap<>();
    public static Map<String, Map<String, Object>> weaponReq = new HashMap<>();
    public static Map<String, Map<String, Object>> mobReq = new HashMap<>();
    public static Map<String, Map<String, Object>> useReq = new HashMap<>();
    public static Map<String, Map<String, Object>> placeReq = new HashMap<>();
    public static Map<String, Map<String, Object>> breakReq = new HashMap<>();
    public static Map<String, Map<String, Object>> xpValue = new HashMap<>();
    public static Map<String, Map<String, Object>> oreInfo = new HashMap<>();
    public static Map<String, Map<String, Object>> logInfo = new HashMap<>();
    public static Map<String, Map<String, Object>> plantInfo = new HashMap<>();
    public static Map<String, Map<String, Object>> salvageInfo = new HashMap<>();

    private static Map<String, Object> tempMap;
    private static String dataPath = "pmmo/data.json";
    private static String templateDataPath = "pmmo/data_template.json";
    private static String defaultDataPath = "/assets/pmmo/util/default_data.json";
    private static final Logger LOGGER = LogManager.getLogger();
    private static Requirements defaultReq, customReq;

    public static void init()
    {
        File templateData = FMLPaths.CONFIGDIR.get().resolve( templateDataPath ).toFile();
        File data = FMLPaths.CONFIGDIR.get().resolve( dataPath ).toFile();

        createData( templateData ); //always rewrite template data with hardcoded one
        if ( !data.exists() )   //If no data file, create one
            createData( data );

        defaultReq = Requirements.readFromFile( templateData.getPath() );
        customReq = Requirements.readFromFile( data.getPath() );

        if( Config.config.loadDefaultConfig.get() )
            updateFinal( defaultReq );
        updateFinal( customReq );
    }

    private static boolean checkValidSkills( Map<String, Object> theMap )
    {
        boolean anyValidSkills = false;

        for( String key : theMap.keySet() )
        {
            if( Skill.getInt( key ) != 0 )
                anyValidSkills = true;
        }

        return anyValidSkills;
    }

    private static void updateReqSkills( Map<String, RequirementItem> req, Map<String, Map<String, Object>> outReq )
    {
        req.forEach( (key, value) ->
        {
            if( checkValidSkills( value.requirements ) )
            {
                if(  !outReq.containsKey( key ) )
                    outReq.put( key, new HashMap<>() );

                for( Map.Entry<String, Object> entry : value.requirements.entrySet() )
                {
                    if( entry.getValue() instanceof Double )
                    {
                        if( Skill.getInt( entry.getKey() ) != 0 && (double) entry.getValue() > 0 )
                            outReq.get( key ).put( entry.getKey(), entry.getValue() );
                    }
                }
            }
        });
    }

    private static void updateReqExtra( Map<String, RequirementItem> req, Map<String, Map<String, Object>> outReq )
    {
        req.forEach( (key, value) ->
        {
            if( !outReq.containsKey( key ) )
                outReq.put( key, new HashMap<>() );

            for( Map.Entry<String, Object> entry : value.requirements.entrySet() )
            {
                if( entry.getValue() instanceof Double )
                {
                    if( entry.getKey().equals( "extraChance" ) && (double) entry.getValue() > 0 )
                        outReq.get( key ).put( entry.getKey(), entry.getValue() );
                }
            }
        });
    }

    private static void updateReqSalvage( Map<String, RequirementItem> req, Map<String, Map<String, Object>> outReq )
    {
        req.forEach( (key, value) ->
        {
            boolean failed = false;
            Map<String, Object> inMap = value.requirements;


            if( !( inMap.containsKey( "salvageItem" ) && inMap.get( "salvageItem" ) instanceof String ) )
            {
                LOGGER.error( "Failed to load Salvage Item " + key + " \"salvageItem\" is invalid" );
                failed = true;
            }
            if( !( inMap.containsKey( "salvageMax" ) && inMap.get( "salvageMax" ) instanceof Double ) )
            {
                LOGGER.error( "Failed to load Salvage Item " + key + " \"salvageMax\" is invalid" );
                failed = true;
            }
            if( !( inMap.containsKey( "baseChance" ) && inMap.get( "baseChance" ) instanceof Double ) )
            {
                LOGGER.error( "Failed to load Salvage Item " + key + " \"baseChance\" is invalid" );
                failed = true;
            }
            if( !( inMap.containsKey( "chancePerLevel" ) && inMap.get( "chancePerLevel" ) instanceof Double ) )
            {
                LOGGER.error( "Failed to load Salvage Item " + key + " \"chancePerLevel\" is invalid" );
                failed = true;
            }
            if( !( inMap.containsKey( "xpPerItem" ) && inMap.get( "xpPerItem" ) instanceof Double ) )
            {
                LOGGER.error( "Failed to load Salvage Item " + key + " \"xpPerItem\" is invalid" );
                failed = true;
            }
            if( !( inMap.containsKey( "levelReq" ) && inMap.get( "levelReq" ) instanceof Double ) )
            {
                LOGGER.error( "Failed to load Salvage Item " + key + " \"levelReq\" is invalid" );
                failed = true;
            }

            if( !failed )
            {
                if( !outReq.containsKey( key ) )
                    outReq.put( key, new HashMap<>() );
                Map<String, Object> outMap = outReq.get( key );

                outMap.put( "salvageItem", inMap.get( "salvageItem" ) );

                if( (double) inMap.get( "salvageMax" ) < 1 )
                    outMap.put( "salvageMax", 1 );
                else
                    outMap.put( "salvageMax", inMap.get( "salvageMax" ) );

                if( (double) inMap.get( "levelReq" ) < 1 )
                    outMap.put( "levelReq", 1 );
                else
                    outMap.put( "levelReq", inMap.get( "levelReq" ) );

                if( (double) inMap.get( "xpPerItem" ) < 0 )
                    outMap.put( "xpPerItem", 0 );
                else
                    outMap.put( "xpPerItem", inMap.get( "xpPerItem" ) );

                if( (double) inMap.get( "baseChance" ) < 0 )
                    outMap.put( "baseChance", 0 );
                else if( (double) inMap.get( "baseChance" ) > 100 )
                    outMap.put( "baseChance", 100 );
                else
                    outMap.put( "baseChance", inMap.get( "baseChance" ) );

                if( (double) inMap.get( "chancePerLevel" ) < 0 )
                    outMap.put( "chancePerLevel", 0 );
                else if( (double) inMap.get( "chancePerLevel" ) > 100 )
                    outMap.put( "chancePerLevel", 100 );
                else
                    outMap.put( "chancePerLevel", inMap.get( "chancePerLevel" ) );
            }
        });
    }

    private static void updateFinal( Requirements req )
    {
        if( Config.config.wearReqEnabled.get() )
            updateReqSkills( req.wears, wearReq );
        if( Config.config.toolReqEnabled.get() )
            updateReqSkills( req.tools, toolReq );
        if( Config.config.weaponReqEnabled.get() )
            updateReqSkills( req.weapons, weaponReq );
//        updateReqSkills( req.mobs, mobReq );
        if( Config.config.useReqEnabled.get() )
            updateReqSkills( req.use, useReq );
        if( Config.config.xpValueEnabled.get() )
            updateReqSkills( req.xpValues, xpValue );
        if( Config.config.placeReqEnabled.get() )
            updateReqSkills( req.placing, placeReq );
        if( Config.config.breakReqEnabled.get() )
            updateReqSkills( req.breaking, breakReq );

        if( Config.config.oreEnabled.get() )
            updateReqExtra( req.ores, oreInfo );
        if( Config.config.logEnabled.get() )
            updateReqExtra( req.logs, logInfo );
        if( Config.config.plantEnabled.get() )
            updateReqExtra( req.plants, plantInfo );

        if( Config.config.salvageEnabled.get() )
            updateReqSalvage( req.salvage, salvageInfo );
    }

    private static void createData( File dataFile )
    {
        try     //create template data file
        {
            dataFile.getParentFile().mkdir();
            dataFile.createNewFile();
        }
        catch( IOException e )
        {
            LOGGER.error( "Could not create template json config!", dataFile.getPath(), e );
        }

        try( InputStream inputStream = ProjectMMOMod.class.getResourceAsStream( defaultDataPath );
             FileOutputStream outputStream = new FileOutputStream( dataFile ); )
        {
            IOUtils.copy( inputStream, outputStream );
        }
        catch( IOException e )
        {
            LOGGER.error( "Error copying over default json config to " + dataFile.getPath(), dataFile.getPath(), e );
        }
    }

    public static Requirements readFromFile( String path )
    {
        try (
                InputStream input = new FileInputStream( path );
                Reader reader = new BufferedReader(new InputStreamReader(input));
        )
        {
            return DESERIALIZER.fromJson(reader, Requirements.class);
        }
        catch (IOException e)
        {
            LOGGER.error("Could not parse json from {}", path, e);

            // If couldn't read, just return an empty object. This may not be what you want.
            return new Requirements();
        }
    }

    public static class RequirementItem
    {
        private final Map<String, Object> requirements = Maps.newHashMap();

//        public HashMap<String, Object> getMap()
//        {
//            return new HashMap<>( requirements );
//        }

        public double getDouble(String registryName)
        {
            return (double) requirements.get(registryName);
        }
    }

    private final Map<String, RequirementItem> wears = Maps.newHashMap();
    private final Map<String, RequirementItem> tools = Maps.newHashMap();
    private final Map<String, RequirementItem> weapons = Maps.newHashMap();
    private final Map<String, RequirementItem> mobs = Maps.newHashMap();
    private final Map<String, RequirementItem> use = Maps.newHashMap();
    private final Map<String, RequirementItem> placing = Maps.newHashMap();
    private final Map<String, RequirementItem> breaking = Maps.newHashMap();
    private final Map<String, RequirementItem> xpValues = Maps.newHashMap();
    private final Map<String, RequirementItem> ores = Maps.newHashMap();
    private final Map<String, RequirementItem> logs = Maps.newHashMap();
    private final Map<String, RequirementItem> plants = Maps.newHashMap();
    private final Map<String, RequirementItem> salvage = Maps.newHashMap();

//    public Map<String, Object> getWear(String registryName)
//    {
//        if( wears.containsKey( registryName ) )
//            return wears.get( registryName ).getMap();
//        else
//            return null;
//    }
//
//    public Map<String, Object> getTool(String registryName)
//    {
//        if( tools.containsKey( registryName ) )
//            return tools.get( registryName ).getMap();
//        else
//            return new HashMap<>();
//    }
//
//    public Map<String, Object> getWeapon(String registryName)
//    {
//        if( weapons.containsKey( registryName ) )
//            return weapons.get( registryName ).getMap();
//        else
//            return new HashMap<>();
//    }
//
//    public Map<String, Object> getXp(String registryName)
//    {
//        if( xpValues.containsKey( registryName ) )
//            return xpValues.get( registryName ).getMap();
//        else
//            return new HashMap<>();
//    }

    // -----------------------------------------------------------------------------
    //
    // GSON STUFFS BELOW
    //
    //

    private static final Gson DESERIALIZER = new GsonBuilder()
            .registerTypeAdapter(Requirements.class, new Deserializer())
            .registerTypeAdapter(RequirementItem.class, new EntryDeserializer())
            .create();

    private static class Deserializer implements JsonDeserializer<Requirements>
    {

        @Override
        public Requirements deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            Requirements req = new Requirements();

            JsonObject obj = json.getAsJsonObject();
            deserializeGroup(obj, "wear_requirement", req.wears::put, context);
            deserializeGroup(obj, "tool_requirement", req.tools::put, context);
            deserializeGroup(obj, "weapon_requirement", req.weapons::put, context);
            deserializeGroup(obj, "mob_requirement", req.mobs::put, context);
            deserializeGroup(obj, "use_requirement", req.use::put, context);
            deserializeGroup(obj, "place_requirement", req.placing::put, context);
            deserializeGroup(obj, "break_requirement", req.breaking::put, context);
            deserializeGroup(obj, "xp_value", req.xpValues::put, context);
            deserializeGroup(obj, "ore", req.ores::put, context);
            deserializeGroup(obj, "log", req.logs::put, context);
            deserializeGroup(obj, "plant", req.plants::put, context);
            deserializeGroup(obj, "salvage", req.salvage::put, context);

            return req;
        }

        private void deserializeGroup(JsonObject obj, String requirementGroupName, BiConsumer<String, RequirementItem> putter, JsonDeserializationContext context)
        {
            if (obj.has(requirementGroupName))
            {
                JsonObject wears = JSONUtils.getJsonObject(obj, requirementGroupName);
                for(Map.Entry<String, JsonElement> entries : wears.entrySet())
                {
                    String name = entries.getKey();
                    RequirementItem values = context.deserialize(entries.getValue(), RequirementItem.class);

                    putter.accept(name, values);
                }
            }
        }
    }

    private static class EntryDeserializer implements JsonDeserializer<RequirementItem>
    {

        @Override
        public RequirementItem deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            RequirementItem item = new RequirementItem();

            JsonObject obj = json.getAsJsonObject();
            for(Map.Entry<String, JsonElement> entries : obj.entrySet())
            {
                String name = entries.getKey();
                Object values;
                if( name.equals( "salvageItem" ) )
                    values = entries.getValue().getAsString();
                else
                    values = entries.getValue().getAsDouble();

                item.requirements.put( name, values );
            }

            return item;
        }
    }
}