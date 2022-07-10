/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.world

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.minecraft.client.settings.GameSettings
import net.minecraft.init.Blocks

@ModuleInfo(name = "AutoBreak", description = "Automatically breaks the block you are looking at.", category = ModuleCategory.WORLD)
class AutoBreak : Module()
{

    @EventTarget
    fun onUpdate(@Suppress("UNUSED_PARAMETER") event: UpdateEvent)
    {
        val theWorld = mc.theWorld ?: return
        val mouseOverPos = mc.objectMouseOver?.blockPos ?: return

        val keybindAttack = mc.gameSettings.keyBindAttack

        if (keybindAttack.pressed && mc.playerController.isHittingBlock) keybindAttack.pressed = false
        else keybindAttack.pressed = theWorld.getBlockState(mouseOverPos).block != Blocks.air
    }

    override fun onDisable()
    {
        val gameSettings = mc.gameSettings

        if (!GameSettings.isKeyDown(gameSettings.keyBindAttack)) gameSettings.keyBindAttack.pressed = false
    }
}
