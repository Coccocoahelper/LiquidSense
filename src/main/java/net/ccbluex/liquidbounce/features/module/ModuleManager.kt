/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.KeyEvent
import net.ccbluex.liquidbounce.event.Listenable
import net.ccbluex.liquidbounce.features.module.modules.`fun`.Derp
import net.ccbluex.liquidbounce.features.module.modules.`fun`.SkinDerp
import net.ccbluex.liquidbounce.features.module.modules.combat.*
import net.ccbluex.liquidbounce.features.module.modules.exploit.*
import net.ccbluex.liquidbounce.features.module.modules.misc.*
import net.ccbluex.liquidbounce.features.module.modules.movement.*
import net.ccbluex.liquidbounce.features.module.modules.player.*
import net.ccbluex.liquidbounce.features.module.modules.render.*
import net.ccbluex.liquidbounce.features.module.modules.world.*
import net.ccbluex.liquidbounce.features.module.modules.world.Timer
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.timer.TimeUtils
import java.util.*

/**
 * TODO
 * * Add TpFucker (like TpEggBreaker in Jigsaw)
 * * ProjectileAimbot (https://github.com/CCBlueX/Old-LiquidBounce-Issues/issues/3496)
 * * FarmBot (Wheat Nuker + Automatically plant seeds) + BonemealAura
 * * AutoFishHook + FishHookAimbot(or integrate FishHook support to ProjectileAimbot)
 * * KillInsults (a.k.a. AutoSay)
 * * Features in AutoGG
 */
class ModuleManager : Listenable
{
    val modules = TreeSet<Module> { module1, module2 -> module1.name.compareTo(module2.name) }
    private val moduleClassMap = hashMapOf<Class<*>, Module>()
    private val moduleNameMap = hashMapOf<String, Module>()

    init
    {
        LiquidBounce.eventManager.registerListener(this)
    }

    /**
     * Register all modules
     */
    fun registerModules()
    {
        ClientUtils.logger.info("[ModuleManager] Loading modules...")

        val nanoTime = System.nanoTime()

        registerModules(

            AutoArmor::class.java, //
            AutoBow::class.java, //
            AutoLeave::class.java, //
            AutoPot::class.java, //
            AutoSoup::class.java, //
            AutoWeapon::class.java, //
            BowAimbot::class.java, //
            Criticals::class.java, //
            KillAura::class.java, //
            Trigger::class.java, //
            Velocity::class.java, //
            Fly, //
            ClickGUI::class.java, //
            HighJump::class.java, //
            InventoryMove::class.java, //
            NoSlow::class.java, //
            LiquidWalk::class.java, //
            SafeWalk::class.java, //
            WallClimb::class.java, //
            Strafe::class.java, //
            Sprint::class.java, //
            Teams::class.java, //
            NoRotateSet::class.java, //
            ChestStealer::class.java, //
            Scaffold::class.java, //
            CivBreak::class.java, //
            Tower::class.java, //
            FastBreak::class.java, //
            FastPlace::class.java, //
            ESP::class.java, //
            Speed, //
            Tracers::class.java, //
            NameTags::class.java, //
            FastUse::class.java, //
            Teleport::class.java, //
            Fullbright::class.java, //
            ItemESP::class.java, //
            StorageESP::class.java, //
            Projectiles::class.java, //
            NoClip::class.java, //
            Nuker::class.java, //
            PingSpoof::class.java, //
            FastClimb::class.java, //
            Step::class.java, //
            AutoRespawn::class.java, //
            AutoTool::class.java, //
            NoWeb::class.java, //
            Spammer::class.java, //
            IceSpeed::class.java, //
            Zoot::class.java, //
            Regen::class.java, //
            NoFall::class.java, //
            Blink::class.java, //
            NameProtect::class.java, //
            HurtCam::class.java, //
            Ghost::class.java, //
            MidClick::class.java, //
            XRay::class.java, //
            Timer::class.java, //
            Sneak::class.java, //
            SkinDerp::class.java, //
            GhostHand::class.java, //
            AutoWalk::class.java, //
            AutoBreak::class.java, //
            FreeCam::class.java, //
            Aimbot::class.java, //
            Eagle::class.java, //
            HitBox::class.java, //
            AntiCactus::class.java, //
            Plugins::class.java, //
            AntiHunger::class.java, //
            ConsoleSpammer::class.java, //
            LongJump::class.java, //
            Parkour::class.java, //
            LadderJump::class.java, //
            FastBow::class.java, //
            MultiActions::class.java, //
            AirJump::class.java, //
            AutoClicker::class.java, //
            Bobbing::class.java, //
            BlockOverlay::class.java, //
            NoFriends::class.java, //
            BlockESP::class.java, //
            Chams::class.java, //
            Clip::class.java, //
            Phase::class.java, //
            ServerCrasher::class.java, //
            NoFOV::class.java, //
            FastStairs::class.java, //
            SwingAnimation::class.java, //
            Derp::class.java, //
            ReverseStep::class.java, //
            TNTBlock::class.java, //
            InventoryCleaner::class.java, //
            TrueSight::class.java, //
            LiquidChat::class.java, //
            AntiBlind::class.java, //
            NoSwing::class.java, //
            BedGodMode::class.java, //
            BugUp::class.java, //
            Breadcrumbs::class.java, //
            AbortBreaking::class.java, //
            PotionSaver::class.java, //
            CameraClip::class.java, //
            WaterSpeed::class.java, //
            Ignite::class.java, //
            SlimeJump::class.java, //
            MoreCarry::class.java, //
            NoPitchLimit::class.java, //
            Kick::class.java, //
            Liquids::class.java, //
            AtAllProvider::class.java, //
            AirLadder::class.java, //
            GodMode::class.java, //
            TeleportHit::class.java, //
            ChatBypass::class.java, //
            ItemTeleport::class.java, //
            BufferSpeed::class.java, //
            SuperKnockback::class.java, //
            ProphuntESP::class.java, //
            AutoFish::class.java, //
            Damage::class.java, //
            Freeze::class.java, //
            KeepContainer::class.java, //
            VehicleOneHit::class.java, //
            Reach::class.java, //
            Rotations::class.java, //
            NoJumpDelay::class.java, //
            BlockWalk::class.java, //
            AntiAFK::class.java, //
            PerfectHorseJump::class.java, //
            HUD::class.java, //
            TNTESP::class.java, //
            ComponentOnHover::class.java, //
            KeepAlive::class.java, //
            ResourcePackSpoof::class.java, //
            NoSlowBreak::class.java, //
            PortalMenu::class.java, //
            AutoLogin::class.java, //
            ItemPhysics::class.java, //
            ExtendedReach::class.java, //
            TpAura::class.java, //
            MurderDetector::class.java, //
            ExtendedTooltips::class.java, //
            EatAnimation::class.java, //
            InstaPortal::class.java, //
            AutoUse::class.java, //
            LagDetector::class.java, //
            TimeChanger::class.java, //
            WeatherChanger::class.java, //
            TargetStrafe::class.java, //
            Disabler::class.java, //
            DamageParticle::class.java, //
            AntiVanish::class.java, //
            LightningDetector::class.java, //
            AutoEnchant::class.java, //
            AntiCAPTCHA::class.java, //
            NoScoreboard, //
            Fucker, //
            ChestAura, //
            AntiBot

        )

        ClientUtils.logger.info("[ModuleManager] Loaded ${modules.size} modules. Took ${TimeUtils.nanosecondsToString(System.nanoTime() - nanoTime)}.")
    }

    /**
     * Register [module]
     */
    fun registerModule(module: Module)
    {
        modules += module
        moduleClassMap[module.javaClass] = module
        moduleNameMap[module.name.lowercase()] = module

        generateCommand(module)
        LiquidBounce.eventManager.registerListener(module)
    }

    /**
     * Register [moduleClass]
     */
    private fun registerModule(moduleClass: Class<out Module>)
    {
        try
        {
            registerModule(moduleClass.newInstance())
        }
        catch (e: Throwable)
        {
            ClientUtils.logger.error("Failed to load module: ${moduleClass.name} (${e.javaClass.name}: ${e.message})", e)
        }
    }

    /**
     * Register a list of modules
     */
    @Suppress("UNCHECKED_CAST")
    @SafeVarargs
    fun registerModules(vararg modules: Any)
    {
        modules.forEach {
            when (it)
            {
                is Module -> registerModule(it)
                is Class<*> -> registerModule(it as? Class<out Module> ?: return@forEach)
            }
        }
    }

    /**
     * Unregister module
     */
    fun unregisterModule(module: Module)
    {
        modules.remove(module)
        moduleClassMap.remove(module::class.java)
        moduleNameMap.remove(module.name.lowercase())
        LiquidBounce.eventManager.unregisterListener(module)
    }

    /**
     * Generate command for [module]
     */
    internal fun generateCommand(module: Module)
    {
        val values = module.flatValues

        if (values.isEmpty()) return

        LiquidBounce.commandManager.registerCommand(ModuleCommand(module, values))
    }

    /**
     * Legacy stuff to support scripts
     */

    /**
     * Get module by [clazz]
     */
    operator fun get(clazz: Class<*>) = moduleClassMap[clazz] ?: throw ClassNotFoundException("Module ${clazz.simpleName} is not registered")

    /**
     * Get module by [moduleName]
     */
    fun getModule(moduleName: String?) = moduleName?.let { moduleNameMap[it.lowercase()] }

    /**
     * Module related events
     */

    /**
     * Handle incoming key presses
     */
    @EventTarget
    private fun onKey(event: KeyEvent) = modules.filter { event.key in it.keyBinds }.forEach(Module::toggle)

    override fun handleEvents() = true
}
