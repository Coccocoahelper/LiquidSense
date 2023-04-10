package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.value.BoolValue

object NoFluid : Module("NoFluid", "Ignore fluids like water and lava.", ModuleCategory.MOVEMENT) {

    val waterValue = BoolValue("Water", true)
    val lavaValue = BoolValue("Lava", true)
}