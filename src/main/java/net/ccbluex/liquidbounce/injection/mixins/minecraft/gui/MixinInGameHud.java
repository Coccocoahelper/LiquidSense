/*
 * This file is part of LiquidBounce (https://github.com/CCBlueX/LiquidBounce)
 *
 * Copyright (c) 2015 - 2024 CCBlueX
 *
 * LiquidBounce is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LiquidBounce is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LiquidBounce. If not, see <https://www.gnu.org/licenses/>.
 */
package net.ccbluex.liquidbounce.injection.mixins.minecraft.gui;

import net.ccbluex.liquidbounce.common.SidebarEntry;
import net.ccbluex.liquidbounce.features.module.modules.render.ModuleAntiBlind;
import net.ccbluex.liquidbounce.features.module.modules.render.ModuleFreeCam;
import net.ccbluex.liquidbounce.features.module.modules.render.ModuleScoreboard;
import net.ccbluex.liquidbounce.render.engine.UIRenderer;
import net.ccbluex.liquidbounce.web.theme.component.ComponentOverlay;
import net.ccbluex.liquidbounce.web.theme.component.FeatureTweak;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardEntry;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.Team;
import net.minecraft.scoreboard.number.NumberFormat;
import net.minecraft.scoreboard.number.StyledNumberFormat;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Comparator;

@Mixin(InGameHud.class)
public abstract class MixinInGameHud {

    @Final
    @Shadow
    private static Identifier PUMPKIN_BLUR;

    @Final
    @Shadow
    private static Identifier POWDER_SNOW_OUTLINE;

    @Shadow
    private int scaledHeight;

    @Shadow
    private int scaledWidth;

    @Shadow
    protected abstract void renderHotbarItem(DrawContext context, int x, int y, float tickDelta, PlayerEntity player, ItemStack stack, int seed);

    @Shadow
    @Nullable
    protected abstract PlayerEntity getCameraPlayer();


    /**
     * Hook render hud event at the top layer
     */
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderStatusEffectOverlay(Lnet/minecraft/client/gui/DrawContext;)V", shift = At.Shift.AFTER))
    private void hookRenderEventStart(DrawContext context, float tickDelta, CallbackInfo callbackInfo) {
        UIRenderer.INSTANCE.startUIOverlayDrawing(context, tickDelta);

        // Draw after overlay event
        if (ComponentOverlay.isTweakEnabled(FeatureTweak.TWEAK_HOTBAR)) {
            drawHotbar(context, tickDelta);
        }
    }

    @Inject(method = "renderOverlay", at = @At("HEAD"), cancellable = true)
    private void injectPumpkinBlur(DrawContext context, Identifier texture, float opacity, CallbackInfo callback) {
        ModuleAntiBlind module = ModuleAntiBlind.INSTANCE;
        if (!module.getEnabled()) {
            return;
        }

        if (module.getPumpkinBlur() && PUMPKIN_BLUR.equals(texture)) {
            callback.cancel();
            return;
        }

        if (module.getPowerSnowFog() && POWDER_SNOW_OUTLINE.equals(texture)) {
            callback.cancel();
        }
    }

    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    private void hookFreeCamRenderCrosshairInThirdPerson(DrawContext context, CallbackInfo ci) {
        if ((ModuleFreeCam.INSTANCE.getEnabled() && ModuleFreeCam.INSTANCE.shouldDisableCrosshair())
                || ComponentOverlay.isTweakEnabled(FeatureTweak.DISABLE_CROSSHAIR)) {
            ci.cancel();
        }
    }


    @Inject(method = "renderScoreboardSidebar", at = @At("HEAD"), cancellable = true)
    private void renderScoreboardSidebar(CallbackInfo ci) {
        if (ComponentOverlay.isTweakEnabled(FeatureTweak.DISABLE_SCOREBOARD)) {
            ci.cancel();
        }
    }

    @Inject(method = "renderHotbar", at = @At("HEAD"), cancellable = true)
    private void hookRenderHotbar(CallbackInfo ci) {
        if (ComponentOverlay.isTweakEnabled(FeatureTweak.TWEAK_HOTBAR)) {
            ci.cancel();
        }
    }

    @Inject(method = "renderStatusBars", at = @At("HEAD"), cancellable = true)
    private void hookRenderStatusBars(CallbackInfo ci) {
        if (ComponentOverlay.isTweakEnabled(FeatureTweak.DISABLE_STATUS_BAR)) {
            ci.cancel();
        }
    }

    @Inject(method = "renderExperienceBar", at = @At("HEAD"), cancellable = true)
    private void hookRenderExperienceBar(CallbackInfo ci) {
        if (ComponentOverlay.isTweakEnabled(FeatureTweak.DISABLE_EXP_BAR)) {
            ci.cancel();
        }
    }

    @Inject(method = "renderHeldItemTooltip", at = @At("HEAD"), cancellable = true)
    private void hookRenderHeldItemTooltip(CallbackInfo ci) {
        if (ComponentOverlay.isTweakEnabled(FeatureTweak.DISABLE_HELD_ITEM_TOOL_TIP)) {
            ci.cancel();
        }
    }

    @Unique
    private void drawHotbar(DrawContext context, float tickDelta) {
        // TODO: Customize via Metadata

        var playerEntity = this.getCameraPlayer();
        if (playerEntity == null) {
            return;
        }

        int center = this.scaledWidth / 2;
        var y = this.scaledHeight - 27;

        int l = 1;
        for (int m = 0; m < 9; ++m) {
            var x = center - 98 + m * 22.5;
            this.renderHotbarItem(context, (int) x, y, tickDelta, playerEntity,
                    playerEntity.getInventory().main.get(m), l++);
        }

        var offHandStack = playerEntity.getOffHandStack();
        if (!offHandStack.isEmpty()) {
            this.renderHotbarItem(context, center - 100 - 26, y, tickDelta, playerEntity, offHandStack, l++);
        }
    }

}
