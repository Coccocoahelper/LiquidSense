package net.ccbluex.liquidbounce.injection.backend

import net.ccbluex.liquidbounce.api.minecraft.item.IItemStack
import net.ccbluex.liquidbounce.api.minecraft.network.play.server.ISPacketEntityEquipment
import net.ccbluex.liquidbounce.api.minecraft.util.WEnumEquipmentSlot
import net.minecraft.network.play.server.S04PacketEntityEquipment

class SPacketEntityEquipmentImpl<out T : S04PacketEntityEquipment>(wrapped: T) : PacketImpl<T>(wrapped), ISPacketEntityEquipment
{
	override val entityID: Int
		get() = wrapped.entityID

	override val slot: WEnumEquipmentSlot
		get()
		{
			val value = wrapped.equipmentSlot
			return if (value == 0) WEnumEquipmentSlot.MAIN_HAND else WEnumEquipmentSlot.values()[value + 1]
		}

	override val item: IItemStack?
		get() = wrapped.itemStack?.wrap()
}

fun ISPacketEntityEquipment.unwrap(): S04PacketEntityEquipment = (this as SPacketEntityEquipmentImpl<*>).wrapped
fun S04PacketEntityEquipment.wrap(): ISPacketEntityEquipment = SPacketEntityEquipmentImpl(this)
