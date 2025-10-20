package box.com.speedbuilderhelper;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.io.File;
import java.util.List;

// §
@Mod(modid = SpeedBuilderHelper.MODID, version = SpeedBuilderHelper.VERSION)
public class SpeedBuilderHelper {

    // Mod Information
    public static final String MODID = "speedbuilderhelper";
    public static final String VERSION = "2.0";

    // File Setup
    public final static File Directory = new File(Minecraft.getMinecraft().mcDataDir + File.separator + "SBH");
    private static final File TIMES = new File(Directory, "times.json");
    private static final File CONFIG = new File(Directory,"conf.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // Strings
    public static String username;
    private String currentTheme = "";
    private String currentDiff = "";

    // Booleans
     private static boolean loaded = false;


    // Events
    @EventHandler
    public void init(FMLInitializationEvent e) {
        if (!Directory.exists()) {
            Directory.mkdirs();
        }
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent e) {
        if (e.world.isRemote) {
            if (e.entity == MC.mc.thePlayer && !loaded) {
                loaded = true;
                onFirstWorldJoin();
            }
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null || mc.thePlayer == null) return;

        if (Utils.getGame() == 2) {
            updateInfo();
        }
    }


    // Func
    private void onFirstWorldJoin() {username = Utils.getUser();}

    private void updateInfo() {
        List<String> sidebar = Utils.getSidebar();
        if (sidebar == null || sidebar.isEmpty()) return;

        String foundTheme = null;
        String foundDiff = null;

        for (String line : sidebar) {
            String clean = Utils.stripColour(line);
            if (clean.startsWith("Theme: ")) {
                foundTheme = clean.substring(7).trim();
            }else if (clean.startsWith("Difficulty: ")) {
                foundDiff = clean.substring(11).trim();
            }
            if (foundTheme != null && foundDiff != null) break;
        }
        if (foundTheme != null && !foundTheme.equalsIgnoreCase(currentTheme)) {
            currentTheme = foundTheme;
            currentDiff = foundDiff;
            Utils.sendMessage("§bTheme: §f " + currentTheme + " §Difficulty: §f " + currentDiff);
        }
    }
}