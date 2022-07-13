package net.ccbluex.liquidbounce.utils.extensions

import net.ccbluex.liquidbounce.injection.implementations.IMixinNetworkManager
import net.minecraft.client.network.NetworkPlayerInfo
import net.minecraft.network.NetworkManager
import net.minecraft.network.Packet
import net.minecraft.network.play.client.C16PacketClientStatus

fun NetworkPlayerInfo.getFullName(useDisplayNameIfPresent: Boolean): String
{
    val displayName = displayName

    if (useDisplayNameIfPresent && displayName != null) return displayName.formattedText

    val name = gameProfile.name

    return playerTeam?.formatString(name) ?: name
}

@Suppress("CAST_NEVER_SUCCEEDS")
fun NetworkManager.sendPacketWithoutEvent(packet: Packet<*>) = (this as IMixinNetworkManager).sendPacketWithoutEvent(packet)

fun isOpenInventoryPacket(packet: Packet<*>): Boolean = packet is C16PacketClientStatus && packet.status == C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT

fun createOpenInventoryPacket(): Packet<*> = C16PacketClientStatus(C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT)
