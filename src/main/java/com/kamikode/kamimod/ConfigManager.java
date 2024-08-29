package com.kamikode.kamimod;
import com.google.gson.*;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File("config/kamimod.json");
    static JsonObject config;

    public static void loadConfig() {
        if (!CONFIG_FILE.exists()) {
            Helpers.logInfo("Config file not found, creating default config.");
            createDefaultConfig();
        } else {
            Helpers.logInfo("Created default config file: {}");
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                config = GSON.fromJson(reader, JsonObject.class);
            } catch (IOException e) {
                Helpers.logError("Error reading config file:", e);
            }
        }
    }

    private static void createDefaultConfig() {
        config = new JsonObject();
        JsonObject configSettings = new JsonObject();
        configSettings.addProperty("voteTextColor", "RED");
        configSettings.addProperty("linkColor", "BLUE");
        configSettings.addProperty("bold", true);
        configSettings.addProperty("italic", false);
        configSettings.addProperty("underlined", true);
        configSettings.addProperty("strikethrough", false);
        configSettings.addProperty("obfuscated", false);
        config.add("config", configSettings);
        config.addProperty("vote_title", "Kami Vote");
        config.addProperty("on_vote_text", "Thank you for voting! Here's the vote links below");

        JsonArray voteLinks = new JsonArray();
        JsonObject link1 = new JsonObject();
        link1.addProperty("link", "https://example.com/vote1");
        voteLinks.add(link1);

        JsonObject link2 = new JsonObject();
        link2.addProperty("link", "https://example.com/vote2");
        voteLinks.add(link2);

        config.add("vote_links", voteLinks);

        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(config, writer);
            Helpers.logInfo("Created default config file: {}");
        } catch (IOException e) {
            Helpers.logError("Error creating default config file:", e);
        }
    }

    public static void reloadConfig() {
        Helpers.logInfo("Reloading config file: {}");
        if (!CONFIG_FILE.exists()) {
            Helpers.logInfo("Config file not found, creating default config.");
            createDefaultConfig();
        } else {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                config = GSON.fromJson(reader, JsonObject.class);
                Helpers.logInfo("Config file reloaded successfully.");
            } catch (IOException e) {
                Helpers.logError("Error reloading config file:", e);
            }
        }
    }

    public static String getOnVoteText() {
        return config != null && config.has("on_vote_text") ? config.get("on_vote_text").getAsString() : "Thank you for voting! Here's the vote links below";
    }

    public static String getVoteTitle(){
        return config != null && config.has("vote_title") ? config.get("vote_title").getAsString() : "Kami Vote";
    }

    public static List<ConfigManager.VoteLinkConfig> getVoteLinkConfigs() {
        List<VoteLinkConfig> configs = new ArrayList<>();
        if (config != null && config.has("vote_links")) {
            JsonArray linkArray = config.getAsJsonArray("vote_links");
            for (JsonElement element : linkArray) {
                if (element.isJsonObject()) {
                    JsonObject linkObject = element.getAsJsonObject();
                    VoteLinkConfig voteLinkConfig = new VoteLinkConfig();
                    voteLinkConfig.link = linkObject.get("link").getAsString();
                    JsonObject configObject = config.getAsJsonObject("config");
                    voteLinkConfig.voteTextColor = configObject.get("voteTextColor").getAsString();
                    voteLinkConfig.linkColor = configObject.get("linkColor").getAsString();
                    voteLinkConfig.bold = configObject.get("bold").getAsBoolean();
                    voteLinkConfig.italic = configObject.get("italic").getAsBoolean();
                    voteLinkConfig.underlined = configObject.get("underlined").getAsBoolean();
                    voteLinkConfig.strikethrough = configObject.get("strikethrough").getAsBoolean();
                    voteLinkConfig.obfuscated = configObject.get("obfuscated").getAsBoolean();
                    configs.add(voteLinkConfig);
                } else {
                    Helpers.logWarn("Invalid element in vote_links array: {}", element.toString());
                }
            }
        }
        return configs;
    }

    public static class VoteLinkConfig {
        public String link;
        public String voteTextColor;
        public String linkColor;
        public boolean bold;
        public boolean italic;
        public boolean underlined;
        public boolean strikethrough;
        public boolean obfuscated;
    }
}
