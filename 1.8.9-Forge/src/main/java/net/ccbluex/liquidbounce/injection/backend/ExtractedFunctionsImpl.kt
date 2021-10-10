/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */

package net.ccbluex.liquidbounce.injection.backend

import com.mojang.authlib.GameProfile
import net.ccbluex.liquidbounce.api.IExtractedFunctions
import net.ccbluex.liquidbounce.api.minecraft.client.block.IBlock
import net.ccbluex.liquidbounce.api.minecraft.enchantments.IEnchantment
import net.ccbluex.liquidbounce.api.minecraft.entity.IEnumCreatureAttribute
import net.ccbluex.liquidbounce.api.minecraft.item.IItem
import net.ccbluex.liquidbounce.api.minecraft.item.IItemStack
import net.ccbluex.liquidbounce.api.minecraft.potion.IPotion
import net.ccbluex.liquidbounce.api.minecraft.scoreboard.ITeam
import net.ccbluex.liquidbounce.api.minecraft.tileentity.ITileEntity
import net.ccbluex.liquidbounce.api.minecraft.util.IEnumFacing
import net.ccbluex.liquidbounce.api.minecraft.util.IIChatComponent
import net.ccbluex.liquidbounce.api.minecraft.util.IResourceLocation
import net.ccbluex.liquidbounce.api.util.WrappedCollection
import net.minecraft.block.Block
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher
import net.minecraft.client.resources.I18n
import net.minecraft.client.settings.GameSettings
import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.init.Items
import net.minecraft.item.Item
import net.minecraft.potion.Potion
import net.minecraft.potion.PotionHelper
import net.minecraft.scoreboard.ScorePlayerTeam
import net.minecraft.util.*
import java.lang.reflect.Field

object ExtractedFunctionsImpl : IExtractedFunctions
{
	private var ofFastRenderField: Field? = null

	init
	{
		try
		{
			val declaredField = GameSettings::class.java.getDeclaredField("ofFastRender").also { ofFastRenderField = it }

			if (!declaredField.isAccessible) declaredField.isAccessible = true
		}
		catch (ignored: NoSuchFieldException)
		{
			// OptiFine not installed
		}
	}

	// <editor-fold desc="Block">
	override fun getBlockById(id: Int): IBlock? = Block.getBlockById(id)?.let(::BlockImpl)

	override fun getIdFromBlock(block: IBlock): Int = Block.getIdFromBlock(block.unwrap())

	override fun getBlockFromName(name: String): IBlock? = Block.getBlockFromName(name)?.wrap()

	override fun getBlockRegistryKeys(): Collection<IResourceLocation> = WrappedCollection(Block.blockRegistry.keys, IResourceLocation::unwrap, ResourceLocation::wrap)

	override fun isBlockEqualTo(block1: IBlock?, block2: IBlock?): Boolean = Block.isEqualTo(block1?.unwrap(), block2?.unwrap())
	// </editor-fold>

	// <editor-fold desc="Item">
	override fun getModifierForCreature(heldItem: IItemStack?, creatureAttribute: IEnumCreatureAttribute): Float = EnchantmentHelper.getModifierForCreature(heldItem?.unwrap(), creatureAttribute.unwrap())

	override fun getItemRegistryKeys(): Collection<IResourceLocation> = WrappedCollection(Item.itemRegistry.keys, IResourceLocation::unwrap, ResourceLocation::wrap)

	override fun getObjectFromItemRegistry(res: IResourceLocation): IItem? = Item.itemRegistry.getObject(res.unwrap())?.wrap()

	override fun getItemByName(name: String): IItem? = (Items::class.java.getField(name).get(null) as Item?)?.wrap()

	override fun getIdFromItem(item: IItem): Int = Item.getIdFromItem(item.unwrap())
	// </editor-fold>

	// <editor-fold desc="Enchantment">
	override fun getEnchantmentByLocation(location: String): IEnchantment? = Enchantment.getEnchantmentByLocation(location)?.wrap()

	override fun getEnchantmentById(enchantID: Int): IEnchantment? = Enchantment.getEnchantmentById(enchantID)?.wrap()

	override fun getEnchantments(): Collection<IResourceLocation> = WrappedCollection(Enchantment.func_181077_c(), IResourceLocation::unwrap, ResourceLocation::wrap)

	override fun getEnchantments(item: IItemStack): Map<Int, Int> = EnchantmentHelper.getEnchantments(item.unwrap())

	override fun getEnchantmentLevel(enchId: Int, stack: IItemStack): Int = EnchantmentHelper.getEnchantmentLevel(enchId, stack.unwrap())
	// </editor-fold>

	// <editor-fold desc="Render">
	override fun enableStandardItemLighting() = RenderHelper.enableStandardItemLighting()
	override fun enableGUIStandardItemLighting() = RenderHelper.enableGUIStandardItemLighting()
	override fun disableStandardItemLighting() = RenderHelper.disableStandardItemLighting()
	override fun setActiveTextureLightMapTexUnit() = GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit)
	override fun setActiveTextureDefaultTexUnit() = GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit)
	override fun getLightMapTexUnit(): Int = OpenGlHelper.lightmapTexUnit
	override fun setLightmapTextureCoords(target: Int, x: Float, y: Float) = OpenGlHelper.setLightmapTextureCoords(target, x, y)
	override fun renderTileEntity(tileEntity: ITileEntity, partialTicks: Float, destroyStage: Int) = TileEntityRendererDispatcher.instance.renderTileEntity(tileEntity.unwrap(), partialTicks, destroyStage)

	/**
	 * Disable OptiFine fast render
	 */
	override fun disableFastRender()
	{
		try
		{
			val fastRenderer = ofFastRenderField ?: return

			if (!fastRenderer.isAccessible) fastRenderer.isAccessible = true

			fastRenderer.setBoolean(Minecraft.getMinecraft().gameSettings, false)
		}
		catch (ignored: IllegalAccessException)
		{
		}
	}
	// </editor-fold>

	// <editor-fold desc="Translation">
	override fun formatI18n(key: String, vararg values: String): String = I18n.format(key, values)
	override fun translateToLocal(key: String): String = StatCollector.translateToLocal(key)
	// </editor-fold>

	// <editor-fold desc="Potion">
	override fun getPotionById(potionID: Int): IPotion = Potion.potionTypes[potionID].wrap()

	override fun getLiquidColor(potionDamage: Int, bypassCache: Boolean): Int = PotionHelper.getLiquidColor(potionDamage, bypassCache)

	override fun getLiquidColor(potion: IItemStack, bypassCache: Boolean): Int = Backend.BACKEND_UNSUPPORTED()
	// </editor-fold>

	// <editor-fold desc="Session">
	override fun sessionServiceJoinServer(profile: GameProfile, token: String, sessionHash: String) = Minecraft.getMinecraft().sessionService.joinServer(profile, token, sessionHash)
	// </editor-fold>

	// <editor-fold desc="Scoreboard">
	override fun scoreboardFormatPlayerName(scorePlayerTeam: ITeam?, playerName: String): String = ScorePlayerTeam.formatPlayerName(scorePlayerTeam?.unwrap(), playerName)
	// </editor-fold>

	// <editor-fold desc="JSON">
	override fun jsonToComponent(toString: String): IIChatComponent = IChatComponent.Serializer.jsonToComponent(toString).wrap()
	// </editor-fold>

	// <editor-fold desc="Facing">
	override fun getHorizontalFacing(yaw: Float): IEnumFacing = EnumFacing.getHorizontal(MathHelper.floor_double((yaw * 4.0 / 360.0) + 0.5) and 3).wrap()
	// </editor-fold>

	// <editor-fold desc="Delegate to MathHelper">
	override fun cos(radians: Float): Float = MathHelper.cos(radians)

	override fun sin(radians: Float): Float = MathHelper.sin(radians)
	// </editor-fold>
}
