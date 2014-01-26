package vc.pvp.skywars.utilities;

import com.google.common.collect.Maps;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Messaging {

    private static Messaging instance;
    private static final Pattern COLOR_PATTERN = Pattern.compile("(?i)(&|" + String.valueOf(ChatColor.COLOR_CHAR) + ")[0-9A-FK-OR]");
    private final FileConfiguration storage;
    private final File storageFile;

    public Messaging(Plugin plugin) {
        this.storageFile = new File(plugin.getDataFolder(), "messages.yml");

        if (!storageFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }

        storage = YamlConfiguration.loadConfiguration(storageFile);
        this.addMessages();
        instance = this;
    }

    public static String stripColor(String input) {
        if (input == null) {
            return "";
        }

        return COLOR_PATTERN.matcher(input).replaceAll("");
    }

    public String getPrefix() {
        return storage.getString("prefix", "");
    }

    public String getMessage(String format) {
        if (storage.contains(format)) {
            return storage.getString(format);
        }

        return null;
    }

    public static class MessageFormatter {

        private final static Pattern PATTERN = Pattern.compile("(?i)(\\{[a-z0-9_]+\\})");
        private final Map<String, String> variableMap = Maps.newHashMap();
        private boolean prefix;

        public MessageFormatter withPrefix() {
            prefix = true;
            return this;
        }

        public MessageFormatter setVariable(String format, String value) {
            if (format != null && !format.isEmpty()) {
                if (value == null) {
                    variableMap.remove(format);
                } else {
                    variableMap.put(format, value);
                }
            }
            return this;
        }

        public String format(String message) {
            if (message == null || message.isEmpty()) {
                return "";
            }

            if (getInstance().getMessage(message) != null) {
                message = getInstance().getMessage(message);
            }

            Matcher matcher = PATTERN.matcher(message);

            while (matcher.find()) {
                String variable = matcher.group();
                variable = variable.substring(1, variable.length() - 1);

                String value = variableMap.get(variable);
                if (value == null) {
                    value = "";
                }

                message = message.replaceFirst(Pattern.quote(matcher.group()), Matcher.quoteReplacement(value));
            }

            if (prefix) {
                message = getInstance().getPrefix() + message;
            }

            return ChatColor.translateAlternateColorCodes('&', message);
        }
    }

    public static Messaging getInstance() {
        return instance;
    }

    public void addMessages() {
        boolean update = false;
        Map<String, String> newVariables = Maps.newHashMap();
        newVariables.put("cmd.score", "&a{player}'s score is: &6{value}");
        newVariables.put("error.no-valid-score", "&cNot a valid score!");
        newVariables.put("error.no-valid-player", "&cNot a valid player!");
        newVariables.put("error.not-enough-arguments", "&cNot enough arguments. &f{example}");
        newVariables.put("success.score-set", "&6{player}&a's score has been set to &6{value}");
        newVariables.put("success.score-give", "&6{value} &ahas been added to &6{player}&a's score");
        newVariables.put("success.score-take", "&6{value} &ahas been removed from &6{player}&a's score");
        newVariables.put("error.invalid-cmd", "&eInvalid command");

        for (Map.Entry<String, String> variable : newVariables.entrySet()) {
            if (!storage.isSet(variable.getKey())) {
                storage.set(variable.getKey(), variable.getValue());
                update = true;
            }
        }
        if (update) {
            try {
                storage.save(this.storageFile);
            } catch (IOException ex) {
                LogUtils.log(Level.SEVERE, "Unable to add messages: " + ex.getMessage());
            }
        }
    }
}
