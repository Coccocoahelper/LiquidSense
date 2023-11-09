/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.modules.player.Blink
import net.ccbluex.liquidbounce.features.module.modules.misc.Teams
import net.ccbluex.liquidbounce.features.module.modules.misc.AntiBot.isBot
import net.ccbluex.liquidbounce.injection.implementations.IMixinEntity
import net.ccbluex.liquidbounce.utils.*
import net.ccbluex.liquidbounce.utils.PacketUtils.handlePacket
import net.ccbluex.liquidbounce.utils.extensions.getDistanceToEntityBox
import net.ccbluex.liquidbounce.utils.extensions.isClientFriend
import net.ccbluex.liquidbounce.utils.misc.StringUtils.contains
import net.ccbluex.liquidbounce.utils.render.ColorUtils.rainbow
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawBacktrackBox
import net.ccbluex.liquidbounce.utils.render.RenderUtils.glColor
import net.ccbluex.liquidbounce.utils.timing.MSTimer
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.Packet
import net.minecraft.network.play.server.*
import net.minecraft.util.AxisAlignedBB
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import java.util.*

object Backtrack : Module("Backtrack", ModuleCategory.COMBAT) {

    private val delay by object : IntegerValue("Delay", 80, 0..700) {
        override fun onChange(oldValue: Int, newValue: Int): Int {
            if (mode == "Modern") {
                clearPackets()
            }

            return newValue
        }
    }

    val mode by ListValue("Mode", arrayOf("Legacy", "Modern"), "Modern")

    // Modern
    private val style by ListValue("Style", arrayOf("Pulse", "Smooth"), "Smooth") { mode == "Modern" }

    private val maxDistanceValue: FloatValue = object : FloatValue("MaxDistance", 3.0f, 0.0f..3.5f) {
        override fun onChange(oldValue: Float, newValue: Float) = newValue.coerceAtLeast(minDistance)
        override fun isSupported() = mode == "Modern"
    }
    private val maxDistance by maxDistanceValue
    private val minDistance by object : FloatValue("MinDistance", 2.0f, 0.0f..3.0f) {
        override fun onChange(oldValue: Float, newValue: Float) = newValue.coerceIn(minimum, maxDistance)
        override fun isSupported() = mode == "Modern"
    }

    // ESP
    private val rainbow by BoolValue("Rainbow", true) { mode == "Modern" }
    private val red by IntegerValue("R", 0, 0..255) { !rainbow && mode == "Modern" }
    private val green by IntegerValue("G", 255, 0..255) { !rainbow && mode == "Modern" }
    private val blue by IntegerValue("B", 0, 0..255) { !rainbow && mode == "Modern" }

    private val packetQueue = LinkedHashMap<Packet<*>, Long>()

    private var target: Entity? = null

    private var globalTimer = MSTimer()

    // Legacy
    private val maximumCachedPositions by IntegerValue("MaxCachedPositions", 10, 1..20) { mode == "Legacy" }

    private val backtrackedPlayer = mutableMapOf<UUID, MutableList<BacktrackData>>()

    private val nonDelayedSoundSubstrings = arrayOf("game.player.hurt", "game.player.die")

    @EventTarget(ignoreCondition = true)
    fun onPacket(event: PacketEvent) {

        val packet = event.packet

        val world = mc.theWorld ?: return

        when (packet) {
            is S0CPacketSpawnPlayer -> {
                val entity = world.getEntityByID(packet.entityID) as? IMixinEntity
                entity?.apply {
                    trueX = packet.realX
                    trueY = packet.realY
                    trueZ = packet.realZ
                    truePos = true
                }
            }

            is S0FPacketSpawnMob -> {
                val entity = world.getEntityByID(packet.entityID) as? IMixinEntity
                entity?.apply {
                    trueX = packet.realX
                    trueY = packet.realY
                    trueZ = packet.realZ
                    truePos = true
                }
            }

            is S14PacketEntity -> {
                val realEntity = packet.getEntity(world)
                val entity = realEntity as? IMixinEntity
                entity?.let {
                    if (!it.truePos)
                    {
                        it.trueX = realEntity.posX
                        it.trueY = realEntity.posY
                        it.trueZ = realEntity.posZ
                        it.truePos = true
                    }
                    it.trueX = (it.trueX + packet.realMotionX)
                    it.trueY = (it.trueY + packet.realMotionY)
                    it.trueZ = (it.trueZ + packet.realMotionZ)
                }
            }

            is S18PacketEntityTeleport -> {
                val entity = world.getEntityByID(packet.entityId) as? IMixinEntity
                entity?.apply {
                    trueX = packet.realX
                    trueY = packet.realY
                    trueZ = packet.realZ
                    truePos = true
                }
            }
        }

        if (!handleEvents())
            return

        if (Blink.blinkingReceive())
            return

        if (event.isCancelled)
            return

        when (mode.lowercase()) {
            "legacy" -> {
                when (packet) {
                    // Check if packet is a spawn player packet
                    is S0CPacketSpawnPlayer -> {
                        // Insert first backtrack data
                        addBacktrackData(
                            packet.player,
                            packet.realX,
                            packet.realY,
                            packet.realZ,
                            System.currentTimeMillis()
                        )
                    }
                }

                backtrackedPlayer.forEach { (key, backtrackData) ->
                    // Remove old data
                    backtrackData.removeIf { it.time + delay < System.currentTimeMillis() }

                    // Remove player if there is no data left. This prevents memory leaks.
                    if (backtrackData.isEmpty()) {
                        removeBacktrackData(key)
                    }
                }
            }

            "modern" -> {
                // Prevent cancelling packets when not needed
                if (packetQueue.isEmpty() && !shouldBacktrack())
                    return

                when (packet) {
                    // Ignore chat packets
                    is S02PacketChat -> return

                    // Flush on teleport or disconnect
                    is S08PacketPlayerPosLook, is S40PacketDisconnect -> {
                        clearPackets()
                        return
                    }

                    is S29PacketSoundEffect -> if (nonDelayedSoundSubstrings in packet.soundName) return

                    // Flush on own death
                    is S06PacketUpdateHealth ->
                        if (packet.health <= 0) {
                            clearPackets()
                            return
                        }

                    is S13PacketDestroyEntities ->
                        if (target != null && target!!.entityId in packet.entityIDs) {
                            clearPackets()
                            return
                        }

                    // Insert checks that check for if S1CPacketEntityMetadata and entity is target and in that metadata, health is set to 0 or less than set target to null and clearPackets()
                    // ^ what if server spoofs target's health to 0?
                }

                // Cancel every received packet to avoid possible server synchronization issues from random causes.
                if (event.eventType == EventState.RECEIVE) {
                    event.cancelEvent()
                    synchronized(packetQueue) {
                        packetQueue[packet] = System.currentTimeMillis()
                    }
                }
            }
        }
    }

    @EventTarget(ignoreCondition=true)
    fun onTick(event: UpdateEvent) {
        for (entity in mc.theWorld.loadedEntityList) {
            if (entity is EntityLivingBase) {
                val entityMixin = entity as? IMixinEntity
                if (entityMixin?.truePos == false) {
                    entityMixin?.trueX = entity.posX
                    entityMixin?.trueY = entity.posY
                    entityMixin?.trueZ = entity.posZ
                    entityMixin?.truePos = true
                }
            }
        }

        if (!handleEvents())
            return

        if (mode != "Modern")
            return

        val target = target as? IMixinEntity

        if (target != null && !Blink.blinkingReceive() && shouldBacktrack() && target.truePos && mc.thePlayer.getDistance(
                target.trueX,
                target.trueY,
                target.trueZ
            ) <= 6f && (style == "Smooth" || !globalTimer.hasTimePassed(delay))
        ) {
            handlePackets()
        } else {
            clearPackets()
            globalTimer.reset()
        }
    }

    @EventTarget
    fun onAttack(event: AttackEvent) {
        if (!isEnemy(event.targetEntity))
            return

        // Clear all packets, start again on enemy change
        if (target != event.targetEntity)
            clearPackets()

        target = event.targetEntity
    }

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        when (mode.lowercase()) {
            "legacy" -> {
                val color = Color.RED

                for (entity in mc.theWorld.loadedEntityList) {
                    if (entity is EntityPlayer) {
                        glPushMatrix()
                        glDisable(GL_TEXTURE_2D)
                        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
                        glEnable(GL_LINE_SMOOTH)
                        glEnable(GL_BLEND)
                        glDisable(GL_DEPTH_TEST)

                        mc.entityRenderer.disableLightmap()

                        glBegin(GL_LINE_STRIP)
                        glColor(color)

                        val renderPosX = mc.renderManager.viewerPosX
                        val renderPosY = mc.renderManager.viewerPosY
                        val renderPosZ = mc.renderManager.viewerPosZ

                        loopThroughBacktrackData(entity) {
                            glVertex3d(entity.posX - renderPosX, entity.posY - renderPosY, entity.posZ - renderPosZ)
                            false
                        }

                        glColor4d(1.0, 1.0, 1.0, 1.0)
                        glEnd()
                        glEnable(GL_DEPTH_TEST)
                        glDisable(GL_LINE_SMOOTH)
                        glDisable(GL_BLEND)
                        glEnable(GL_TEXTURE_2D)
                        glPopMatrix()
                    }
                }
            }

            "modern" -> {
                if (!shouldBacktrack())
                    return

                val renderManager = mc.renderManager

                target?.let {
                    val targetEntity = target as IMixinEntity

                    if (targetEntity.truePos) {

                        val x =
                            targetEntity.trueX - renderManager.renderPosX
                        val y =
                            targetEntity.trueY - renderManager.renderPosY
                        val z =
                            targetEntity.trueZ - renderManager.renderPosZ
                        val axisAlignedBB = it.entityBoundingBox.offset(-it.posX, -it.posY, -it.posZ).offset(x, y, z)

                        drawBacktrackBox(
                            AxisAlignedBB.fromBounds(
                                axisAlignedBB.minX,
                                axisAlignedBB.minY,
                                axisAlignedBB.minZ,
                                axisAlignedBB.maxX,
                                axisAlignedBB.maxY,
                                axisAlignedBB.maxZ
                            ), color
                        )
                    }
                }
            }
        }
    }

    @EventTarget
    fun onEntityMove(event: EntityMovementEvent) {
        if (mode == "Legacy") {
            val entity = event.movedEntity

            // Check if entity is a player
            if (entity is EntityPlayer) {
                // Add new data
                addBacktrackData(entity.uniqueID, entity.posX, entity.posY, entity.posZ, System.currentTimeMillis())
            }
        }
    }

    @EventTarget
    fun onWorld(event: WorldEvent) {
        // Clear packets on disconnect only
        if (mode == "Modern" && event.worldClient == null) {
            clearPackets(false)
        }
    }

    override fun onEnable() = reset()

    override fun onDisable() {
        if (mode == "Modern") {
            clearPackets()
        }
    }

    private fun handlePackets() {
        synchronized(packetQueue) {
            packetQueue.entries.removeAll { (packet, timestamp) ->
                if (timestamp <= System.currentTimeMillis() - delay) {
                    handlePacket(packet)
                    true
                } else false
            }
        }
    }

    private fun clearPackets(handlePackets: Boolean = true) {
        synchronized(packetQueue) {
            if (handlePackets)
                packetQueue.keys.forEach(::handlePacket)

            packetQueue.clear()

            reset()
        }
    }

    private fun addBacktrackData(id: UUID, x: Double, y: Double, z: Double, time: Long) {
        // Get backtrack data of player
        val backtrackData = getBacktrackData(id)

        // Check if there is already data of the player
        if (backtrackData != null) {
            // Check if there is already enough data of the player
            if (backtrackData.size >= maximumCachedPositions) {
                // Remove first data
                backtrackData.removeFirst()
            }

            // Insert new data
            backtrackData += BacktrackData(x, y, z, time)
        } else {
            // Create new list
            backtrackedPlayer[id] = mutableListOf(BacktrackData(x, y, z, time))
        }
    }

    private fun getBacktrackData(id: UUID) = backtrackedPlayer[id]

    private fun removeBacktrackData(id: UUID) = backtrackedPlayer.remove(id)

    private fun isEnemy(entity: Entity?): Boolean {
        if (entity is EntityLivingBase && entity != mc.thePlayer) {
            if (entity is EntityPlayer) {
                if (entity.isSpectator || isBot(entity)) return false

                if (entity.isClientFriend() && !NoFriends.handleEvents()) return false

                return !Teams.handleEvents() || !Teams.isInYourTeam(entity)
            }

            return true
        }

        return false
    }

    /**
     * This function will return the nearest tracked range of an entity.
     */
    fun getNearestTrackedDistance(entity: Entity): Double {
        var nearestRange = 0.0

        loopThroughBacktrackData(entity) {
            val range = entity.getDistanceToEntityBox(mc.thePlayer)

            if (range < nearestRange || nearestRange == 0.0) {
                nearestRange = range
            }

            false
        }

        return nearestRange
    }

    /**
     * This function will loop through the backtrack data of an entity.
     */
    fun loopThroughBacktrackData(entity: Entity, action: () -> Boolean) {
        if (!Backtrack.state || entity !is EntityPlayer || mode == "Modern")
            return

        val backtrackDataArray = getBacktrackData(entity.uniqueID) ?: return
        val entityPosition = entity.positionVector
        val (prevX, prevY, prevZ) = Triple(entity.prevPosX, entity.prevPosY, entity.prevPosZ)

        // This will loop through the backtrack data. We are using reversed() to loop through the data from the newest to the oldest.
        for ((x, y, z, _) in backtrackDataArray.reversed()) {
            entity.setPosition(x, y, z)
            entity.prevPosX = x
            entity.prevPosY = y
            entity.prevPosZ = z

            if (action())
                break
        }

        // Reset position
        entity.prevPosX = prevX
        entity.prevPosY = prevY
        entity.prevPosZ = prevZ

        entity.setPosition(entityPosition.xCoord, entityPosition.yCoord, entityPosition.zCoord)
    }

    val color
        get() = if (rainbow) rainbow() else Color(red, green, blue)

    private fun shouldBacktrack() =
        target?.let {
            !it.isDead && isEnemy(it) && (mc.thePlayer?.ticksExisted ?: 0) > 20
                && mc.thePlayer.getDistanceToEntityBox(it) in minDistance..maxDistance
        } ?: false

    private fun reset() {
        target = null
        globalTimer.reset()
    }
}

data class BacktrackData(val x: Double, val y: Double, val z: Double, val time: Long)