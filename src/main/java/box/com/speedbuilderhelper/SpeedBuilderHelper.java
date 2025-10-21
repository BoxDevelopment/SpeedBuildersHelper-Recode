package box.com.speedbuilderhelper;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    // Objects
    public static String username;
    private String currentTheme = "";
    private String currentDiff = "";
    private BlockPos closestPlat = null;
    private int lastGameState = -1;

    // Booleans
    private static boolean loaded = false;
    public static boolean debug = true;

    // List
    public List<BlockPos> platformPositions = Arrays.asList(
            new BlockPos(-15, 72, 45),
            new BlockPos(18, 72, 45),
            new BlockPos(45, 72, 16),
            new BlockPos(45, 72, -17),
            new BlockPos(16, 72, -44),
            new BlockPos(-17, 72, -44),
            new BlockPos(-44, 72, -15),
            new BlockPos(-44, 72, 18)
    );




    // Events
    @EventHandler
    public void init(FMLInitializationEvent e) {
        if (!Directory.exists()) {
            Directory.mkdirs();
        }
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent e) {
        String m = e.message.getUnformattedText();
        Pattern pattern = Pattern.compile("(.*) got a perfect build in (.*)s!");
        Matcher match = pattern.matcher((m));
        if (match.find() && match.group(1).equals(username)) {
            double time = Double.parseDouble(match.group(2));
            submitTime(time);
        }
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
        int game = Utils.getGame();

        if (game == 2 && lastGameState != 2) {
            getPlatform();
        }

        if (game != 2 && lastGameState == 2) {
            closestPlat = null;
        }

        if (game == 2) {
            updateInfo();
        }
        lastGameState = game;
    }



    // Func
    private void onFirstWorldJoin() {username = Utils.getUser();}

    private void getPlatform() {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = mc.thePlayer;

        if (player == null) return;

        BlockPos playerPos = player.getPosition();
        double closestDist = Double.MAX_VALUE;
        BlockPos nearest = null;

        for (BlockPos platform: platformPositions) {
            double dist = platform.distanceSq(playerPos);
            if (dist < closestDist) {
                closestDist = dist;
                nearest = platform;
            }
        }
        if (nearest != null) {
            closestPlat = nearest;
            Utils.sendDebug("§bClosest platform: §f" + String.format("X=%d, Y=%d, Z=%d", nearest.getX(), nearest.getY(), nearest.getZ()));
        }
    }


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
            Utils.sendMessage("§bTheme: §f" + currentTheme + " §dDifficulty: §f" + currentDiff);
        }
    }

    private void submitTime(double time) {
        Utils.sendMessage("§aCompleted " + currentTheme + ", Difficulty: " + currentDiff + " Time: " + time);
    }
}