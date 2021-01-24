/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.file.configs;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.*;

import net.ccbluex.liquidbounce.file.FileConfig;
import net.ccbluex.liquidbounce.file.FileManager;
import net.ccbluex.liquidbounce.utils.ClientUtils;

public class FriendsConfig extends FileConfig
{

	private final List<Friend> friends = new ArrayList<>();

	/**
	 * Constructor of config
	 *
	 * @param file
	 *             of config
	 */
	public FriendsConfig(final File file)
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
		clearFriends();
		try
		{
			final JsonElement jsonElement = new JsonParser().parse(new BufferedReader(new FileReader(getFile())));

			if (jsonElement instanceof JsonNull)
				return;

			for (final JsonElement friendElement : jsonElement.getAsJsonArray())
			{
				final JsonObject friendObject = friendElement.getAsJsonObject();
				addFriend(friendObject.get("playerName").getAsString(), friendObject.get("alias").getAsString());
			}

		}
		catch (final JsonSyntaxException | IllegalStateException ex)
		{
			// When the JSON Parse fail, the client try to load and update the old config
			ClientUtils.getLogger().info("[FileManager] Try to load old Friends config...");

			final BufferedReader bufferedReader = new BufferedReader(new FileReader(getFile()));

			final String emptyReplacement = Matcher.quoteReplacement("");
			String line;
			while ((line = bufferedReader.readLine()) != null)
				if (!line.contains("{") && !line.contains("}"))
				{
					line = BLANK.matcher(line).replaceAll(emptyReplacement);
					line = QUOTE.matcher(line).replaceAll(emptyReplacement);
					line = COMMA.matcher(line).replaceAll(emptyReplacement);

					if (line.contains(":"))
					{
						final String[] data = line.split(":");
						addFriend(data[0], data[1]);
					}
					else
						addFriend(line);
				}
			bufferedReader.close();
			ClientUtils.getLogger().info("[FileManager] Loaded old Friends config...");

			// Save the friends into a new valid JSON file
			saveConfig();
			ClientUtils.getLogger().info("[FileManager] Saved Friends to new config...");
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
		final JsonArray jsonArray = new JsonArray();

		for (final Friend friend : friends)
		{
			final JsonObject friendObject = new JsonObject();
			friendObject.addProperty("playerName", friend.getPlayerName());
			friendObject.addProperty("alias", friend.getAlias());
			jsonArray.add(friendObject);
		}

		final PrintWriter printWriter = new PrintWriter(new FileWriter(getFile()));
		printWriter.println(FileManager.PRETTY_GSON.toJson(jsonArray));
		printWriter.close();
	}

	/**
	 * Add friend to config
	 *
	 * @param  playerName
	 *                    of friend
	 * @return            of successfully added friend
	 */
	public boolean addFriend(final String playerName)
	{
		return addFriend(playerName, playerName);
	}

	/**
	 * Add friend to config
	 *
	 * @param  playerName
	 *                    of friend
	 * @param  alias
	 *                    of friend
	 * @return            of successfully added friend
	 */
	public boolean addFriend(final String playerName, final String alias)
	{
		if (isFriend(playerName))
			return false;

		friends.add(new Friend(playerName, alias));
		return true;
	}

	/**
	 * Remove friend from config
	 *
	 * @param playerName
	 *                   of friend
	 */
	public boolean removeFriend(final String playerName)
	{
		if (!isFriend(playerName))
			return false;

		friends.removeIf(friend -> friend.getPlayerName().equals(playerName));
		return true;
	}

	/**
	 * Check is friend
	 *
	 * @param  playerName
	 *                    of friend
	 * @return            is friend
	 */
	public boolean isFriend(final String playerName)
	{
		return friends.stream().anyMatch(friend -> friend.getPlayerName().equals(playerName));
	}

	/**
	 * Clear all friends from config
	 */
	public void clearFriends()
	{
		friends.clear();
	}

	/**
	 * Get friends
	 *
	 * @return list of friends
	 */
	public List<Friend> getFriends()
	{
		return friends;
	}

	public static class Friend
	{

		private final String playerName;
		private final String alias;

		/**
		 * @param playerName
		 *                   of friend
		 * @param alias
		 *                   of friend
		 */
		Friend(final String playerName, final String alias)
		{
			this.playerName = playerName;
			this.alias = alias;
		}

		/**
		 * @return name of friend
		 */
		public String getPlayerName()
		{
			return playerName;
		}

		/**
		 * @return alias of friend
		 */
		public String getAlias()
		{
			return alias;
		}
	}

	private static final Pattern BLANK = Pattern.compile(" ", Pattern.LITERAL);
	private static final Pattern QUOTE = Pattern.compile("\"", Pattern.LITERAL);
	private static final Pattern COMMA = Pattern.compile(",", Pattern.LITERAL);
}
