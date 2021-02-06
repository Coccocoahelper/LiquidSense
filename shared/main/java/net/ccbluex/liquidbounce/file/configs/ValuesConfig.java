/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.file.configs;

import java.io.*;
import java.util.Map.Entry;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.modules.misc.LiquidChat;
import net.ccbluex.liquidbounce.features.special.AntiModDisable;
import net.ccbluex.liquidbounce.features.special.AutoReconnect;
import net.ccbluex.liquidbounce.features.special.BungeeCordSpoof;
import net.ccbluex.liquidbounce.file.FileConfig;
import net.ccbluex.liquidbounce.file.FileManager;
import net.ccbluex.liquidbounce.ui.client.GuiBackground;
import net.ccbluex.liquidbounce.ui.client.altmanager.sub.GuiDonatorCape;
import net.ccbluex.liquidbounce.ui.client.altmanager.sub.altgenerator.GuiTheAltening;
import net.ccbluex.liquidbounce.utils.EntityUtils;
import net.ccbluex.liquidbounce.utils.misc.MiscUtils;
import net.ccbluex.liquidbounce.value.Value;

public class ValuesConfig extends FileConfig
{

	/**
	 * Constructor of config
	 *
	 * @param file
	 *             of config
	 */
	public ValuesConfig(final File file)
	{
		super(file);
	}

	/**
	 * Load config from file
	 *
	 * @throws IOException
	 */
	@Override
	protected void loadConfig() throws IOException
	{
		final JsonElement jsonElement = new JsonParser().parse(MiscUtils.createBufferedFileReader(getFile()));

		if (jsonElement instanceof JsonNull)
			return;

		final JsonObject jsonObject = (JsonObject) jsonElement;

		for (final Entry<String, JsonElement> entry : jsonObject.entrySet())
			if ("CommandPrefix".equalsIgnoreCase(entry.getKey()))
				LiquidBounce.commandManager.setPrefix(entry.getValue().getAsCharacter());
			else if ("ShowRichPresence".equalsIgnoreCase(entry.getKey()))
				LiquidBounce.clientRichPresence.setShowRichPresenceValue(entry.getValue().getAsBoolean());
			else if ("targets".equalsIgnoreCase(entry.getKey())) {
				final JsonObject jsonValue = (JsonObject) entry.getValue();

				if (jsonValue.has("TargetPlayer"))
					EntityUtils.targetPlayer = jsonValue.get("TargetPlayer").getAsBoolean();
				if (jsonValue.has("TargetMobs"))
					EntityUtils.targetMobs = jsonValue.get("TargetMobs").getAsBoolean();
				if (jsonValue.has("TargetAnimals"))
					EntityUtils.targetAnimals = jsonValue.get("TargetAnimals").getAsBoolean();
				if (jsonValue.has("TargetInvisible"))
					EntityUtils.targetInvisible = jsonValue.get("TargetInvisible").getAsBoolean();
				if (jsonValue.has("TargetDead"))
					EntityUtils.targetDead = jsonValue.get("TargetDead").getAsBoolean();
			} else if ("features".equalsIgnoreCase(entry.getKey())) {
				final JsonObject jsonValue = (JsonObject) entry.getValue();

				if (jsonValue.has("AntiModDisable"))
					AntiModDisable.enabled = jsonValue.get("AntiModDisable").getAsBoolean();
				if (jsonValue.has("AntiModDisableBlockFML"))
					AntiModDisable.blockFMLPackets = jsonValue.get("AntiModDisableBlockFMLPackets").getAsBoolean();
				if (jsonValue.has("AntiModDisableBlockFMLProxyPackets"))
					AntiModDisable.blockFMLProxyPackets = jsonValue.get("AntiModDisableBlockFMLProxyPackets").getAsBoolean();
				if (jsonValue.has("AntiModDisableSpoofBrandPayloadPackets"))
					AntiModDisable.blockClientBrandRetrieverPackets = jsonValue.get("AntiModDisableSpoofBrandPayloadPackets").getAsBoolean();
				if (jsonValue.has("AntiModDisableBlockWDLPayloads"))
					AntiModDisable.blockWDLPayloads = jsonValue.get("AntiModDisableBlockWDLPayloads").getAsBoolean();
				if (jsonValue.has("AntiModDisableBlockBetterSprintingPayloads"))
					AntiModDisable.blockBetterSprintingPayloads = jsonValue.get("AntiModDisableBlockBetterSprintingPayloads").getAsBoolean();
				if (jsonValue.has("AntiModDisableBlock5zigPayloads"))
					AntiModDisable.block5zigsmodPayloads = jsonValue.get("AntiModDisableBlock5zigPayloads").getAsBoolean();
				if (jsonValue.has("AntiModDisableBlockPermsReplPayloads"))
					AntiModDisable.blockPermissionsReplPayloads = jsonValue.get("AntiModDisableBlockPermsReplPayloads").getAsBoolean();
				if (jsonValue.has("AntiModDisableBlockDIPermsPayloads"))
					AntiModDisable.blockDIPermissionsPayloads = jsonValue.get("AntiModDisableBlockDIPermsPayloads").getAsBoolean();
				if (jsonValue.has("AntiModDisableBlockCrackedVapeSabotages"))
					AntiModDisable.blockCrackedVapeSabotages = jsonValue.get("AntiModDisableBlockCrackedVapeSabotages").getAsBoolean();
				if (jsonValue.has("AntiModDisableBlockSchematicaPayloads"))
					AntiModDisable.blockSchematicaPayloads = jsonValue.get("AntiModDisableBlockSchematicaPayloads").getAsBoolean();
				if (jsonValue.has("AntiModDisableDebug"))
					AntiModDisable.debug = jsonValue.get("AntiModDisableDebug").getAsBoolean();

				if (jsonValue.has("BungeeSpoof"))
					BungeeCordSpoof.enabled = jsonValue.get("BungeeSpoof").getAsBoolean();
				if (jsonValue.has("AutoReconnectDelay"))
					AutoReconnect.INSTANCE.setDelay(jsonValue.get("AutoReconnectDelay").getAsInt());
			} else if ("thealtening".equalsIgnoreCase(entry.getKey())) {
				final JsonObject jsonValue = (JsonObject) entry.getValue();

				if (jsonValue.has("API-Key"))
					GuiTheAltening.Companion.setApiKey(jsonValue.get("API-Key").getAsString());
			} else if ("liquidchat".equalsIgnoreCase(entry.getKey())) {
				final JsonObject jsonValue = (JsonObject) entry.getValue();

				if (jsonValue.has("token"))
					LiquidChat.Companion.setJwtToken(jsonValue.get("token").getAsString());
			} else if ("DonatorCape".equalsIgnoreCase(entry.getKey())) {
				final JsonObject jsonValue = (JsonObject) entry.getValue();

				if (jsonValue.has("TransferCode"))
					GuiDonatorCape.Companion.setTransferCode(jsonValue.get("TransferCode").getAsString());

				if (jsonValue.has("CapeEnabled"))
					GuiDonatorCape.Companion.setCapeEnabled(jsonValue.get("CapeEnabled").getAsBoolean());
			} else if ("Background".equalsIgnoreCase(entry.getKey())) {
				final JsonObject jsonValue = (JsonObject) entry.getValue();

				if (jsonValue.has("Enabled"))
					GuiBackground.Companion.setEnabled(jsonValue.get("Enabled").getAsBoolean());

				if (jsonValue.has("Particles"))
					GuiBackground.Companion.setParticles(jsonValue.get("Particles").getAsBoolean());
			} else {
				final Module module = LiquidBounce.moduleManager.getModule(entry.getKey());

				if (module != null) {
					final JsonObject jsonModule = (JsonObject) entry.getValue();

					for (final Value moduleValue : module.getValues()) {
						final JsonElement element = jsonModule.get(moduleValue.getName());

						if (element != null)
							moduleValue.fromJson(element);
					}
				}
			}
	}

	/**
	 * Save config to file
	 *
	 * @throws IOException
	 */
	@Override
	protected void saveConfig() throws IOException
	{
		final JsonObject jsonObject = new JsonObject();

		jsonObject.addProperty("CommandPrefix", LiquidBounce.commandManager.getPrefix());
		jsonObject.addProperty("ShowRichPresence", LiquidBounce.clientRichPresence.getShowRichPresenceValue());

		final JsonObject jsonTargets = new JsonObject();
		jsonTargets.addProperty("TargetPlayer", EntityUtils.targetPlayer);
		jsonTargets.addProperty("TargetMobs", EntityUtils.targetMobs);
		jsonTargets.addProperty("TargetAnimals", EntityUtils.targetAnimals);
		jsonTargets.addProperty("TargetInvisible", EntityUtils.targetInvisible);
		jsonTargets.addProperty("TargetDead", EntityUtils.targetDead);
		jsonObject.add("targets", jsonTargets);

		final JsonObject jsonFeatures = new JsonObject();
		jsonFeatures.addProperty("AntiModDisable", AntiModDisable.enabled);
		jsonFeatures.addProperty("AntiModDisableBlockFMLPackets", AntiModDisable.blockFMLPackets);
		jsonFeatures.addProperty("AntiModDisableBlockFMLProxyPackets", AntiModDisable.blockFMLProxyPackets);
		jsonFeatures.addProperty("AntiModDisableSpoofBrandPayloadPackets", AntiModDisable.blockClientBrandRetrieverPackets);
		jsonFeatures.addProperty("AntiModDisableBlockWDLPayloads", AntiModDisable.blockWDLPayloads);
		jsonFeatures.addProperty("AntiModDisableBlockBetterSprintingPayloads", AntiModDisable.blockBetterSprintingPayloads);
		jsonFeatures.addProperty("AntiModDisableBlock5zigPayloads", AntiModDisable.block5zigsmodPayloads);
		jsonFeatures.addProperty("AntiModDisableBlockPermsReplPayloads", AntiModDisable.blockPermissionsReplPayloads);
		jsonFeatures.addProperty("AntiModDisableBlockDIPermsPayloads", AntiModDisable.blockDIPermissionsPayloads);
		jsonFeatures.addProperty("AntiModDisableBlockCrackedVapeSabotages", AntiModDisable.blockCrackedVapeSabotages);
		jsonFeatures.addProperty("AntiModDisableBlockSchematicaPayloads", AntiModDisable.blockSchematicaPayloads);
		jsonFeatures.addProperty("AntiModDisableDebug", AntiModDisable.debug);

		jsonFeatures.addProperty("BungeeSpoof", BungeeCordSpoof.enabled);
		jsonFeatures.addProperty("AutoReconnectDelay", AutoReconnect.INSTANCE.getDelay());
		jsonObject.add("features", jsonFeatures);

		final JsonObject theAlteningObject = new JsonObject();
		theAlteningObject.addProperty("API-Key", GuiTheAltening.Companion.getApiKey());
		jsonObject.add("thealtening", theAlteningObject);

		final JsonObject liquidChatObject = new JsonObject();
		liquidChatObject.addProperty("token", LiquidChat.Companion.getJwtToken());
		jsonObject.add("liquidchat", liquidChatObject);

		final JsonObject capeObject = new JsonObject();
		capeObject.addProperty("TransferCode", GuiDonatorCape.Companion.getTransferCode());
		capeObject.addProperty("CapeEnabled", GuiDonatorCape.Companion.getCapeEnabled());
		jsonObject.add("DonatorCape", capeObject);

		final JsonObject backgroundObject = new JsonObject();
		backgroundObject.addProperty("Enabled", GuiBackground.Companion.getEnabled());
		backgroundObject.addProperty("Particles", GuiBackground.Companion.getParticles());
		jsonObject.add("Background", backgroundObject);

		LiquidBounce.moduleManager.getModules().stream().filter(module -> !module.getValues().isEmpty()).forEach(module ->
		{
			final JsonObject jsonModule = new JsonObject();
			module.getValues().forEach(value -> jsonModule.add(value.getName(), value.toJson()));
			jsonObject.add(module.getName(), jsonModule);
		});

		final BufferedWriter writer = MiscUtils.createBufferedFileWriter(getFile());
		writer.write(FileManager.PRETTY_GSON.toJson(jsonObject) + System.lineSeparator());
		writer.close();
	}
}
