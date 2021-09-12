/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.Listenable
import net.ccbluex.liquidbounce.features.module.modules.render.HUD
import net.ccbluex.liquidbounce.file.FileManager
import net.ccbluex.liquidbounce.injection.backend.Backend
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.NotificationIcon
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.MinecraftInstance
import net.ccbluex.liquidbounce.value.AbstractValue
import net.ccbluex.liquidbounce.value.ValueGroup
import kotlin.random.Random

open class Module : MinecraftInstance(), Listenable
{
	var isSupported: Boolean

	// Module information
	// TODO: Remove ModuleInfo and change to constructor (#Kotlin)
	var name: String
	var description: String
	var category: ModuleCategory
	var keyBinds = mutableSetOf<Int>()
		set(keyBind)
		{
			field = keyBind

			if (!LiquidBounce.isStarting) FileManager.saveConfig(LiquidBounce.fileManager.modulesConfig)
		}
	var array = true
		set(array)
		{
			field = array

			if (!LiquidBounce.isStarting) FileManager.saveConfig(LiquidBounce.fileManager.modulesConfig)
		}
	private val canEnable: Boolean

	var slideStep = 0F

	init
	{
		val moduleInfo = javaClass.getAnnotation(ModuleInfo::class.java) ?: throw Exception("Module ${javaClass.simpleName} doesn't have ModuleInfo annotation")

		name = moduleInfo.name
		description = moduleInfo.description
		category = moduleInfo.category
		keyBinds = moduleInfo.defaultKeyBinds.toMutableSet()
		array = moduleInfo.array
		canEnable = moduleInfo.canEnable
		isSupported = Backend.REPRESENTED_BACKEND_VERSION in moduleInfo.supportedVersions
	}

	/**
	 * Current state of module
	 */
	var state = false
		set(value)
		{
			if (field == value) return

			// Call toggle
			onToggle(value)

			// Play sound and add notification
			if (!LiquidBounce.isStarting)
			{
				mc.soundHandler.playSound("random.click", 1.0F)
				if ((LiquidBounce.moduleManager[HUD::class.java] as HUD).notificationModuleManagerValue.get()) LiquidBounce.hud.addNotification(Notification(NotificationIcon.INFORMATION, "Module Manager", "${if (value) "Enabled " else "Disabled "}$name"))
			}

			// Call on enabled or disabled
			if (value)
			{
				try
				{
					if (canEnable) field = true
					onEnable()
				}
				catch (e: Exception)
				{
					field = false
					ClientUtils.logger.error("Uncaught exception '$e' occurred while onEnable() in module $name", e)
					LiquidBounce.hud.addNotification(Notification(NotificationIcon.ERROR, "Module Manager", "Something went wrong while enabling module $name"))
				}
			}
			else
			{
				try
				{
					onDisable()
					field = false
				}
				catch (e: Exception)
				{
					ClientUtils.logger.error("Uncaught exception '$e' occurred while onDisable() in module $name", e)
					LiquidBounce.hud.addNotification(Notification(NotificationIcon.ERROR, "Module Manager", "Something went wrong while disabling module $name"))
				}
			}

			// Save module state
			FileManager.saveConfig(LiquidBounce.fileManager.modulesConfig)
		}

	/**
	 * HUD
	 */
	val hue = Random.nextFloat()
	var slide = 0F

	/**
	 * Tag
	 */
	open val tag: String?
		get() = null

	/**
	 * Toggle module
	 */
	fun toggle()
	{
		state = !state
	}

	/**
	 * Called when module toggled
	 */
	open fun onToggle(state: Boolean)
	{
	}

	/**
	 * Called when module enabled
	 */
	open fun onEnable()
	{
	}

	/**
	 * Called when module disabled
	 */
	open fun onDisable()
	{
	}

	/**
	 * Get module by [valueName]
	 */
	open fun getValue(valueName: String) = flatValues.find { it.name.equals(valueName, ignoreCase = true) } ?: flatValues.find { it.aliasMatches(valueName) }?.adaptToValue(valueName) // Backward compatibility

	/**
	 * Get all values of module
	 */
	open val values: List<AbstractValue> by lazy(LazyThreadSafetyMode.NONE, javaClass.declaredFields.asSequence().map { valueField ->
		valueField.isAccessible = true
		valueField[this]
	}.filterIsInstance<AbstractValue>().filter(AbstractValue::isSupported).filterNot(AbstractValue::isBelongsToGroup)::toList)

	open val flatValues: List<AbstractValue> by lazy(LazyThreadSafetyMode.NONE, javaClass.declaredFields.asSequence().map { valueField ->
		valueField.isAccessible = true
		valueField[this]
	}.filterIsInstance<AbstractValue>().filter(AbstractValue::isSupported).filterNot { it is ValueGroup }::toList)

	/**
	 * Events should be handled when module is enabled
	 */
	override fun handleEvents() = state
}
