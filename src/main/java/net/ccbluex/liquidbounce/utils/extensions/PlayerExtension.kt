/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.utils.extensions

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.utils.MinecraftInstance.mc
import net.ccbluex.liquidbounce.utils.Rotation
import net.ccbluex.liquidbounce.utils.RotationUtils.getFixedSensitivityAngle
import net.ccbluex.liquidbounce.utils.render.ColorUtils.stripColor
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.entity.Entity
import net.minecraft.entity.boss.EntityDragon
import net.minecraft.entity.monster.EntityGhast
import net.minecraft.entity.monster.EntityGolem
import net.minecraft.entity.monster.EntityMob
import net.minecraft.entity.monster.EntitySlime
import net.minecraft.entity.passive.EntityAnimal
import net.minecraft.entity.passive.EntityBat
import net.minecraft.entity.passive.EntitySquid
import net.minecraft.entity.passive.EntityVillager
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.Vec3

/**
 * Allows to get the distance between the current entity and [entity] from the nearest corner of the bounding box
 */
fun Entity.getDistanceToEntityBox(entity: Entity): Double {
    val eyes = this.getPositionEyes(1F)
    val pos = getNearestPointBB(eyes, entity.hitBox)
    return eyes.distanceTo(pos)
}

fun getNearestPointBB(eye: Vec3, box: AxisAlignedBB): Vec3 {
    val origin = doubleArrayOf(eye.xCoord, eye.yCoord, eye.zCoord)
    val destMins = doubleArrayOf(box.minX, box.minY, box.minZ)
    val destMaxs = doubleArrayOf(box.maxX, box.maxY, box.maxZ)
    for (i in 0..2) {
        origin[i] = origin[i].coerceIn(destMins[i], destMaxs[i])
    }
    return Vec3(origin[0], origin[1], origin[2])
}

fun EntityPlayer.getPing(): Int {
    val playerInfo = mc.netHandler.getPlayerInfo(uniqueID)
    return playerInfo?.responseTime ?: 0
}

fun Entity.isAnimal(): Boolean {
    return this is EntityAnimal || this is EntitySquid || this is EntityGolem || this is EntityBat
}

fun Entity.isMob(): Boolean {
    return this is EntityMob || this is EntityVillager || this is EntitySlime || this is EntityGhast || this is EntityDragon
}

fun EntityPlayer.isClientFriend(): Boolean {
    val entityName = name ?: return false

    return LiquidBounce.fileManager.friendsConfig.isFriend(stripColor(entityName))
}

val Entity.rotation: Rotation
    get() = Rotation(rotationYaw, rotationPitch)

val Entity.hitBox: AxisAlignedBB
    get() {
        val borderSize = collisionBorderSize.toDouble()
        return entityBoundingBox.expand(borderSize, borderSize, borderSize)
    }

/**
 * Setting yaw to a fixed sensitivity angle
 */

fun EntityPlayerSP.setFixedSensitivityAngles(yaw: Float? = null, pitch: Float? = null) {
    if (yaw != null) mc.thePlayer.fixedSensitivityYaw = yaw

    if (pitch != null) mc.thePlayer.fixedSensitivityPitch = pitch
}

var EntityPlayerSP.fixedSensitivityYaw: Float
    get() = getFixedSensitivityAngle(mc.thePlayer.rotationYaw)
    set(yaw) {
        mc.thePlayer.rotationYaw = getFixedSensitivityAngle(yaw, mc.thePlayer.rotationYaw)
    }

var EntityPlayerSP.fixedSensitivityPitch: Float
    get() = getFixedSensitivityAngle(mc.thePlayer.rotationPitch)
    set(pitch) {
        mc.thePlayer.rotationPitch = getFixedSensitivityAngle(pitch.coerceIn(-90f, 90f), mc.thePlayer.rotationPitch)
    }

// Makes fixedSensitivityYaw, ... += work
operator fun EntityPlayerSP.plusAssign(value: Float) {
    fixedSensitivityYaw += value
    fixedSensitivityPitch += value
}