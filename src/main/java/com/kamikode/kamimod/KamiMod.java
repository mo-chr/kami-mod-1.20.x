package com.kamikode.kamimod;

import com.google.gson.JsonObject;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.kamikode.kamimod.Helpers;

import java.util.List;

public class KamiMod implements ModInitializer {
	public static final String MOD_ID = "kami-vote";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public String getModVersion() {
		ModContainer modContainer = FabricLoader.getInstance().getModContainer(MOD_ID).orElseThrow(() -> new RuntimeException("Mod ID not found"));
		ModMetadata metadata = modContainer.getMetadata();
		return metadata.getVersion().getFriendlyString();
	}
	private static final Formatting DEFAULT_VOTE_TEXT_COLOR = Formatting.WHITE;
	private static final Formatting DEFAULT_LINK_COLOR = Formatting.AQUA;

	@Override
	public void onInitialize() {
		ConfigManager.loadConfig();
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(
					CommandManager.literal("vote")
							.executes(context -> sendVoteLinks(context.getSource()))
							.then(CommandManager.literal("reload")
									.requires(source -> source.hasPermissionLevel(3))
									.executes(context -> reloadConfig(context.getSource())))
			);
		});
	}

	private int sendVoteLinks(ServerCommandSource source) {
		try {
			// Get the text and styles for the vote links
			String onVoteText = ConfigManager.getOnVoteText();
			List<ConfigManager.VoteLinkConfig> configs = ConfigManager.getVoteLinkConfigs();
			String voteTitle = ConfigManager.getVoteTitle();
			// Get the style settings from the config
			JsonObject configObject = ConfigManager.config.getAsJsonObject("config");
			Formatting voteTextColor = Formatting.byName(configObject.get("voteTextColor").getAsString().toUpperCase());
			Formatting linkColor = Formatting.byName(configObject.get("linkColor").getAsString().toUpperCase());
			boolean bold = configObject.get("bold").getAsBoolean();
			boolean italic = configObject.get("italic").getAsBoolean();
			boolean underlined = configObject.get("underlined").getAsBoolean();
			boolean strikethrough = configObject.get("strikethrough").getAsBoolean();
			boolean obfuscated = configObject.get("obfuscated").getAsBoolean();

			// Create a title or header
			Text title = Text.literal(voteTitle)
					.styled(style -> style
							.withColor(Formatting.GOLD)
							.withBold(true)
							.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Kami Vote v " + getModVersion()))));

			// Create the on-vote text with styles
			Text onVoteTextComponent = Text.literal(onVoteText)
					.styled(style -> style
							.withColor(voteTextColor)
							.withBold(bold)
							.withItalic(italic)
							.withUnderline(underlined)
							.withStrikethrough(strikethrough)
							.withObfuscated(obfuscated)
					);

			Text separator = Text.literal("§8--------------------").styled(style -> style.withColor(Formatting.GRAY));

			source.sendFeedback(() -> separator, false);
			source.sendFeedback(() -> title, false);
			source.sendFeedback(() -> separator, false);
			source.sendFeedback(() -> onVoteTextComponent, false);
			source.sendFeedback(() -> separator, false);

			for (ConfigManager.VoteLinkConfig config : configs) {
				Text clickableLink = Text.literal(config.link)
						.styled(style -> style
								.withColor(linkColor)
								.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, config.link))
								.withUnderline(true)
								.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(config.link)))
						);

				source.sendFeedback(() -> Text.literal(""), false);
				source.sendFeedback(() -> clickableLink, false);
				source.sendFeedback(() -> Text.literal(""), false);
			}
			Text endLine = Text.literal("§8--------------------").styled(style -> style.withColor(Formatting.GRAY));
			source.sendFeedback(() -> endLine, false);

			return 1;
		} catch (Exception e) {
			Helpers.logError("Error sending vote links:", e);
		}
		return 0;
	}



	private int reloadConfig(ServerCommandSource source) {
		if (!source.hasPermissionLevel(2)) {
			source.sendError(Text.literal("["+KamiMod.MOD_ID+"] You do not have permission to reload the configuration!").formatted(Formatting.RED));
			return 0;
		}
		try {
			ConfigManager.reloadConfig();
			source.sendFeedback(() -> Text.literal("["+KamiMod.MOD_ID+"] Configuration reloaded successfully!").formatted(Formatting.GREEN), false);
			return 1;
		} catch (Exception e) {
			Helpers.logError("["+KamiMod.MOD_ID+"] Error reloading config:", e);
			source.sendError(Text.literal("["+KamiMod.MOD_ID+"] Error reloading configuration!").formatted(Formatting.RED));
			return 0;
		}
	}
}
