/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.utils.login

import com.google.common.base.Joiner
import java.util.*

class MinecraftAccount
{
	var serviceType: AltServiceType
	val name: String
	var password: String? = null
		private set
	var accountName: String? = null
	var bannedServers: List<String> = ArrayList()

	constructor(serviceType: AltServiceType, name: String)
	{
		this.serviceType = serviceType
		this.name = name
	}

	constructor(serviceType: AltServiceType, name: String, bannedServers: List<String>)
	{
		this.serviceType = serviceType
		this.name = name
		this.bannedServers = bannedServers
	}

	constructor(serviceType: AltServiceType, name: String, password: String?)
	{
		this.serviceType = serviceType
		this.name = name
		this.password = password
	}

	constructor(serviceType: AltServiceType, name: String, password: String?, bannedServers: List<String>)
	{
		this.serviceType = serviceType
		this.name = name
		this.password = password
		this.bannedServers = bannedServers
	}

	constructor(serviceType: AltServiceType, name: String, password: String?, inGameName: String?)
	{
		this.serviceType = serviceType
		this.name = name
		this.password = password
		accountName = inGameName
	}

	constructor(serviceType: AltServiceType, name: String, password: String?, inGameName: String?, bannedServers: List<String>)
	{
		this.serviceType = serviceType
		this.name = name
		this.password = password
		accountName = inGameName
		this.bannedServers = bannedServers
	}

	val isCracked: Boolean
		get() = password == null || password?.isEmpty() ?: true

	fun serializeBannedServers(): String = if (bannedServers.isEmpty()) "" else Joiner.on(", ").join(bannedServers)

	enum class AltServiceType(val id: String, private val parent: AltServiceType?)
	{
		MOJANG("Mojang", null), MOJANG_INVALID("Mojang(Invalid)", MOJANG), MOJANG_MIGRATED("Mojang(Migrated)", MOJANG), MCLEAKS("MCLeaks", null), MCLEAKS_INVALID("MCLeaks(Invalid)", MCLEAKS), THEALTENING("TheAltening", null), THEALTENING_INVALID("TheAltening(Invalid)", THEALTENING);

		fun equals(other: AltServiceType?): Boolean = other != null && (this == other || parent != null && other == parent || this == other.parent)

		companion object
		{
			fun getById(id: String?): AltServiceType? = Arrays.stream(values()).filter { altServiceType: AltServiceType -> altServiceType.id.equals(id, ignoreCase = true) }.findFirst().orElse(null)
		}
	}
}
