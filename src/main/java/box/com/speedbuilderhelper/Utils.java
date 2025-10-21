package box.com.speedbuilderhelper;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.ChatComponentText;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class Utils implements MC {


    public static boolean nullCheck() {
        return mc.thePlayer != null && mc.theWorld != null;
    }


    public static String getUser() {
        if (!nullCheck()) {
            return "null";
        }
        EntityPlayer player = mc.thePlayer;
        return player.getName();
    }

    public static void sendMessage(String message) {
        if (!nullCheck()) {
            return;
        }
        mc.thePlayer.addChatMessage(new ChatComponentText(replace("§7[§dSBH§7]§r " + message)));
    }
    public static void sendDebug(String message) {
        if (!nullCheck() || !SpeedBuilderHelper.debug) {
            return;
        }
        mc.thePlayer.addChatMessage(new ChatComponentText(replace("§7[§dDEBUG§7]§r " + message)));
    }

    public static String replace(String text) {
        return text.replace("&", "§").replace("%and", "&");
    }

    public static double round(double number, int decimals) {
        if (decimals == 0) {
            return Math.round(number);
        }
        double power = Math.pow(10.0, decimals);
        return (double)Math.round(number * power) / power;
    }

    public static boolean contains(List<String> list, String target) {
        return list.stream().anyMatch(s -> s.equalsIgnoreCase(target));
    }

    public static List<String> getSidebar() {
        if (mc.theWorld == null) {
            return Collections.emptyList();
        }

        final Scoreboard scoreboard = mc.theWorld.getScoreboard();
        if (scoreboard == null) {
            return Collections.emptyList();
        }

        final ScoreObjective objective = scoreboard.getObjectiveInDisplaySlot(1);
        if (objective == null) {
            return Collections.emptyList();
        }

        Collection<Score> scores = scoreboard.getSortedScores(objective);
        final List<Score> filteredScores = new ArrayList<>(Math.min(scores.size(), 15));

        for (final Score score : scores) {
            if (score != null && score.getPlayerName() != null &&
                    !score.getPlayerName().startsWith("#")) {
                filteredScores.add(score);
            }
        }

        final int startIndex = Math.max(0, filteredScores.size() - 15);
        final List<Score> limitedScores = filteredScores.subList(startIndex, filteredScores.size());

        final List<String> lines = new ArrayList<>(limitedScores.size());
        for (final Score score : limitedScores) {
            final ScorePlayerTeam team = scoreboard.getPlayersTeam(score.getPlayerName());
            lines.add(ScorePlayerTeam.formatPlayerName(team, score.getPlayerName()));
        }

        return lines;
    }

    private static final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)§[0-9A-FK-OR]");

    public static String stripColour(String text) {
        if (text == null) return "";
        return STRIP_COLOR_PATTERN.matcher(text).replaceAll("").trim();
    }



    public static int getGame() {
        List<String> sidebar = Utils.getSidebar();
        boolean isGame = false;

        for (String line : sidebar) {
            String clean = stripColour(line);
            if (clean.startsWith("Round: ")) {
                return 2;
            }
            if (clean.startsWith("Mode: Speed Builders")) {
                isGame = true;
            }
        }
        return isGame ? 1 : 0;
    }
}
