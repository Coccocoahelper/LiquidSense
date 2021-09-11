package net.ccbluex.liquidbounce.injection.backend

import net.ccbluex.liquidbounce.api.minecraft.tileentity.ITileEntityChest
import net.ccbluex.liquidbounce.api.minecraft.util.WEnumChestType
import net.minecraft.tileentity.TileEntityChest

class TileEntityChestImpl(override val wrapped: TileEntityChest) : TileEntityImpl(wrapped), ITileEntityChest
{
	override val chestType: Int
		get() = WEnumChestType.values()[wrapped.chestType.ordinal]
}
