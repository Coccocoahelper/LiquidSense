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
import net.ccbluex.liquidbounce.file.FileConfig;
import net.ccbluex.liquidbounce.file.FileManager;
import net.ccbluex.liquidbounce.utils.misc.MiscUtils;

public class ModulesConfig extends FileConfig
{

	/**
	 * Constructor of config
	 *
	 * @param file
	 *             of config
	 */
	public ModulesConfig(final File file)
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

		for (final Entry<String, JsonElement> entry : jsonElement.getAsJsonObject().entrySet())
		{
			final Module module = LiquidBounce.moduleManager.getModule(entry.getKey());

			if (module != null)
			{
				final JsonObject jsonModule = (JsonObject) entry.getValue();

				module.setState(jsonModule.get("State").getAsBoolean());
				module.setKeyBind(jsonModule.get("KeyBind").getAsInt());

				if (jsonModule.has("Array"))
					module.setArray(jsonModule.get("Array").getAsBoolean());
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

		for (final Module module : LiquidBounce.moduleManager.getModules())
		{
			final JsonObject jsonMod = new JsonObject();
			jsonMod.addProperty("State", module.getState());
			jsonMod.addProperty("KeyBind", module.getKeyBind());
			jsonMod.addProperty("Array", module.getArray());
			jsonObject.add(module.getName(), jsonMod);
		}

		final BufferedWriter writer = MiscUtils.createBufferedFileWriter(getFile());
		writer.write(FileManager.PRETTY_GSON.toJson(jsonObject) + System.lineSeparator());
		writer.close();
	}
}
