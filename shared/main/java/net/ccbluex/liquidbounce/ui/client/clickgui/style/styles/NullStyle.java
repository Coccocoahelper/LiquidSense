/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles;

import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import net.ccbluex.liquidbounce.api.minecraft.client.gui.IFontRenderer;
import net.ccbluex.liquidbounce.api.minecraft.util.WMathHelper;
import net.ccbluex.liquidbounce.features.module.modules.render.ClickGUI;
import net.ccbluex.liquidbounce.ui.client.clickgui.Panel;
import net.ccbluex.liquidbounce.ui.client.clickgui.elements.ButtonElement;
import net.ccbluex.liquidbounce.ui.client.clickgui.elements.ModuleElement;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.Style;
import net.ccbluex.liquidbounce.ui.font.AWTFontRenderer;
import net.ccbluex.liquidbounce.ui.font.Fonts;
import net.ccbluex.liquidbounce.ui.font.Fonts.FontInfo;
import net.ccbluex.liquidbounce.ui.font.GameFontRenderer;
import net.ccbluex.liquidbounce.utils.block.BlockUtils;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.ccbluex.liquidbounce.value.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.StringUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.input.Mouse;

@SideOnly(Side.CLIENT)
public class NullStyle extends Style
{

	private boolean mouseDown;
	private boolean rightMouseDown;

	@Override
	public void drawPanel(final int mouseX, final int mouseY, final Panel panel)
	{
		RenderUtils.drawRect((float) panel.getX() - 3, panel.getY(), (float) panel.getX() + panel.getWidth() + 3, (float) panel.getY() + 19, ClickGUI.generateColor().getRGB());
		if (panel.getFade() > 0)
			RenderUtils.drawBorderedRect(panel.getX(), (float) panel.getY() + 19, (float) panel.getX() + panel.getWidth(), panel.getY() + 19 + panel.getFade(), 1, Integer.MIN_VALUE, Integer.MIN_VALUE);
		GlStateManager.resetColor();
		final float textWidth = Fonts.font35.getStringWidth("\u00A7f" + StringUtils.stripControlCodes(panel.getName()));
		Fonts.font35.drawString("\u00A7f" + panel.getName(), (int) (panel.getX() - (textWidth - 100.0F) / 2.0F), panel.getY() + 7, Integer.MAX_VALUE);
	}

	@Override
	public void drawDescription(final int mouseX, final int mouseY, final String text)
	{
		final int textWidth = Fonts.font35.getStringWidth(text);

		RenderUtils.drawRect(mouseX + 9, mouseY, mouseX + textWidth + 14, mouseY + Fonts.font35.getFontHeight() + 3, ClickGUI.generateColor().getRGB());
		GlStateManager.resetColor();
		Fonts.font35.drawString(text, mouseX + 12, mouseY + Fonts.font35.getFontHeight() / 2, Integer.MAX_VALUE);
	}

	@Override
	public void drawButtonElement(final int mouseX, final int mouseY, final ButtonElement buttonElement)
	{
		GlStateManager.resetColor();
		Fonts.font35.drawString(buttonElement.getDisplayName(), (int) (buttonElement.getX() - (Fonts.font35.getStringWidth(buttonElement.getDisplayName()) - 100.0f) / 2.0f), buttonElement.getY() + 6, buttonElement.getColor());
	}

	@Override
	public void drawModuleElement(final int mouseX, final int mouseY, final ModuleElement moduleElement)
	{
		final int guiColor = ClickGUI.generateColor().getRGB();
		GlStateManager.resetColor();
		Fonts.font35.drawString(moduleElement.getDisplayName(), (int) (moduleElement.getX() - (Fonts.font35.getStringWidth(moduleElement.getDisplayName()) - 100.0f) / 2.0f), moduleElement.getY() + 6, moduleElement.getModule().getState() ? guiColor : Integer.MAX_VALUE);

		final List<Value<?>> moduleValues = moduleElement.getModule().getValues();

		if (!moduleValues.isEmpty())
		{
			Fonts.font35.drawString("+", moduleElement.getX() + moduleElement.getWidth() - 8, moduleElement.getY() + moduleElement.getHeight() / 2, Color.WHITE.getRGB());

			if (moduleElement.isShowSettings())
			{
				int yPos = moduleElement.getY() + 4;
				for (final Value value : moduleValues)
				{
					final boolean isNumber = value.get() instanceof Number;

					if (isNumber)
						AWTFontRenderer.Companion.setAssumeNonVolatile(false);

					if (value instanceof BoolValue)
					{
						final String text = value.getName();
						final float textWidth = Fonts.font35.getStringWidth(text);

						if (moduleElement.getSettingsWidth() < textWidth + 8)
							moduleElement.setSettingsWidth(textWidth + 8);

						RenderUtils.drawRect(moduleElement.getX() + moduleElement.getWidth() + 4, yPos + 2, moduleElement.getX() + moduleElement.getWidth() + moduleElement.getSettingsWidth(), yPos + 14, Integer.MIN_VALUE);

						if (mouseX >= moduleElement.getX() + moduleElement.getWidth() + 4 && mouseX <= moduleElement.getX() + moduleElement.getWidth() + moduleElement.getSettingsWidth() && mouseY >= yPos + 2 && mouseY <= yPos + 14)
							if (Mouse.isButtonDown(0) && moduleElement.isntPressed()) {
								final BoolValue boolValue = (BoolValue) value;

								boolValue.set(!boolValue.get());
								mc.getSoundHandler().playSound("gui.button.press", 1.0F);
							}

						GlStateManager.resetColor();
						Fonts.font35.drawString(text, moduleElement.getX() + moduleElement.getWidth() + 6, yPos + 4, ((BoolValue) value).get() ? guiColor : Integer.MAX_VALUE);
						yPos += 12;
					}
					else if (value instanceof ListValue)
					{
						final ListValue listValue = (ListValue) value;

						final String text = value.getName();
						final float textWidth = Fonts.font35.getStringWidth(text);

						if (moduleElement.getSettingsWidth() < textWidth + 16)
							moduleElement.setSettingsWidth(textWidth + 16);

						RenderUtils.drawRect(moduleElement.getX() + moduleElement.getWidth() + 4, yPos + 2, moduleElement.getX() + moduleElement.getWidth() + moduleElement.getSettingsWidth(), yPos + 14, Integer.MIN_VALUE);
						GlStateManager.resetColor();
						Fonts.font35.drawString("\u00A7c" + text, moduleElement.getX() + moduleElement.getWidth() + 6, yPos + 4, 0xffffff);
						Fonts.font35.drawString(listValue.openList ? "-" : "+", (int) (moduleElement.getX() + moduleElement.getWidth() + moduleElement.getSettingsWidth() - (listValue.openList ? 5 : 6)), yPos + 4, 0xffffff);

						if (mouseX >= moduleElement.getX() + moduleElement.getWidth() + 4 && mouseX <= moduleElement.getX() + moduleElement.getWidth() + moduleElement.getSettingsWidth() && mouseY >= yPos + 2 && mouseY <= yPos + 14)
							if (Mouse.isButtonDown(0) && moduleElement.isntPressed()) {
								listValue.openList = !listValue.openList;
								mc.getSoundHandler().playSound("gui.button.press", 1.0F);
							}

						yPos += 12;

						for (final String valueOfList : listValue.getValues())
						{
							final float textWidth2 = Fonts.font35.getStringWidth(">" + valueOfList);

							if (moduleElement.getSettingsWidth() < textWidth2 + 12)
								moduleElement.setSettingsWidth(textWidth2 + 12);

							if (listValue.openList)
							{
								RenderUtils.drawRect(moduleElement.getX() + moduleElement.getWidth() + 4, yPos + 2, moduleElement.getX() + moduleElement.getWidth() + moduleElement.getSettingsWidth(), yPos + 14, Integer.MIN_VALUE);

								if (mouseX >= moduleElement.getX() + moduleElement.getWidth() + 4 && mouseX <= moduleElement.getX() + moduleElement.getWidth() + moduleElement.getSettingsWidth() && mouseY >= yPos + 2 && mouseY <= yPos + 14)
									if (Mouse.isButtonDown(0) && moduleElement.isntPressed()) {
										listValue.set(valueOfList);
										mc.getSoundHandler().playSound("gui.button.press", 1.0F);
									}

								GlStateManager.resetColor();
								Fonts.font35.drawString(">", moduleElement.getX() + moduleElement.getWidth() + 6, yPos + 4, Integer.MAX_VALUE);
								Fonts.font35.drawString(valueOfList, moduleElement.getX() + moduleElement.getWidth() + 14, yPos + 4, listValue.get() != null && listValue.get().equalsIgnoreCase(valueOfList) ? guiColor : Integer.MAX_VALUE);
								yPos += 12;
							}
						}
					}
					else if (value instanceof FloatValue)
					{
						final FloatValue floatValue = (FloatValue) value;
						final String text = value.getName() + "\u00A7f: \u00A7c" + round(floatValue.get());
						final float textWidth = Fonts.font35.getStringWidth(text);

						if (moduleElement.getSettingsWidth() < textWidth + 8)
							moduleElement.setSettingsWidth(textWidth + 8);

						RenderUtils.drawRect(moduleElement.getX() + moduleElement.getWidth() + 4, yPos + 2, moduleElement.getX() + moduleElement.getWidth() + moduleElement.getSettingsWidth(), yPos + 24, Integer.MIN_VALUE);
						RenderUtils.drawRect(moduleElement.getX() + moduleElement.getWidth() + 8, yPos + 18, moduleElement.getX() + moduleElement.getWidth() + moduleElement.getSettingsWidth() - 4, yPos + 19, Integer.MAX_VALUE);
						final float sliderValue = moduleElement.getX() + moduleElement.getWidth() + (moduleElement.getSettingsWidth() - 12) * (floatValue.get() - floatValue.getMinimum()) / (floatValue.getMaximum() - floatValue.getMinimum());
						RenderUtils.drawRect(8 + sliderValue, yPos + 15, sliderValue + 11, yPos + 21, guiColor);

						if (mouseX >= moduleElement.getX() + moduleElement.getWidth() + 4 && mouseX <= moduleElement.getX() + moduleElement.getWidth() + moduleElement.getSettingsWidth() - 4 && mouseY >= yPos + 15 && mouseY <= yPos + 21)
							if (Mouse.isButtonDown(0)) {
								final double i = WMathHelper.clamp_double((mouseX - moduleElement.getX() - moduleElement.getWidth() - 8) / (moduleElement.getSettingsWidth() - 12), 0, 1);
								floatValue.set(round((float) (floatValue.getMinimum() + (floatValue.getMaximum() - floatValue.getMinimum()) * i)).floatValue());
							}

						GlStateManager.resetColor();
						Fonts.font35.drawString(text, moduleElement.getX() + moduleElement.getWidth() + 6, yPos + 4, 0xffffff);
						yPos += 22;
					}
					else if (value instanceof IntegerValue)
					{
						final IntegerValue integerValue = (IntegerValue) value;
						final String text = value.getName() + "\u00A7f: \u00A7c" + (value instanceof BlockValue ? BlockUtils.getBlockName(integerValue.get()) + " (" + integerValue.get() + ")" : integerValue.get());
						final float textWidth = Fonts.font35.getStringWidth(text);

						if (moduleElement.getSettingsWidth() < textWidth + 8)
							moduleElement.setSettingsWidth(textWidth + 8);

						RenderUtils.drawRect(moduleElement.getX() + moduleElement.getWidth() + 4, yPos + 2, moduleElement.getX() + moduleElement.getWidth() + moduleElement.getSettingsWidth(), yPos + 24, Integer.MIN_VALUE);
						RenderUtils.drawRect(moduleElement.getX() + moduleElement.getWidth() + 8, yPos + 18, moduleElement.getX() + moduleElement.getWidth() + moduleElement.getSettingsWidth() - 4, yPos + 19, Integer.MAX_VALUE);
						final float sliderValue = moduleElement.getX() + moduleElement.getWidth() + (moduleElement.getSettingsWidth() - 12) * (integerValue.get() - integerValue.getMinimum()) / (integerValue.getMaximum() - integerValue.getMinimum());
						RenderUtils.drawRect(8 + sliderValue, yPos + 15, sliderValue + 11, yPos + 21, guiColor);

						if (mouseX >= moduleElement.getX() + moduleElement.getWidth() + 4 && mouseX <= moduleElement.getX() + moduleElement.getWidth() + moduleElement.getSettingsWidth() && mouseY >= yPos + 15 && mouseY <= yPos + 21)
							if (Mouse.isButtonDown(0)) {
								final double i = WMathHelper.clamp_double((mouseX - moduleElement.getX() - moduleElement.getWidth() - 8) / (moduleElement.getSettingsWidth() - 12), 0, 1);
								integerValue.set((int) (integerValue.getMinimum() + (integerValue.getMaximum() - integerValue.getMinimum()) * i));
							}

						GlStateManager.resetColor();
						Fonts.font35.drawString(text, moduleElement.getX() + moduleElement.getWidth() + 6, yPos + 4, 0xffffff);
						yPos += 22;
					}
					else if (value instanceof FontValue)
					{
						final FontValue fontValue = (FontValue) value;
						final IFontRenderer fontRenderer = fontValue.get();

						RenderUtils.drawRect(moduleElement.getX() + moduleElement.getWidth() + 4, yPos + 2, moduleElement.getX() + moduleElement.getWidth() + moduleElement.getSettingsWidth(), yPos + 14, Integer.MIN_VALUE);

						String displayString = "Font: Unknown";

						if (fontRenderer.isGameFontRenderer())
						{
							final GameFontRenderer liquidFontRenderer = fontRenderer.getGameFontRenderer();

							displayString = "Font: " + liquidFontRenderer.getDefaultFont().getFont().getName() + " - " + liquidFontRenderer.getDefaultFont().getFont().getSize();
						}
						else if (fontRenderer == Fonts.minecraftFont)
							displayString = "Font: Minecraft";
						else
						{
							final FontInfo objects = Fonts.getFontDetails(fontRenderer);

							if (objects != null)
								displayString = objects.getName() + (objects.getFontSize() == -1 ? "" : " - " + objects.getFontSize());
						}

						Fonts.font35.drawString(displayString, moduleElement.getX() + moduleElement.getWidth() + 6, yPos + 4, Color.WHITE.getRGB());
						final int stringWidth = Fonts.font35.getStringWidth(displayString);

						if (moduleElement.getSettingsWidth() < stringWidth + 8)
							moduleElement.setSettingsWidth(stringWidth + 8);

						if ((Mouse.isButtonDown(0) && !mouseDown || Mouse.isButtonDown(1) && !rightMouseDown) && mouseX >= moduleElement.getX() + moduleElement.getWidth() + 4 && mouseX <= moduleElement.getX() + moduleElement.getWidth() + moduleElement.getSettingsWidth() && mouseY >= yPos + 4 && mouseY <= yPos + 12)
						{
							final List<IFontRenderer> fonts = Fonts.getFonts();

							if (Mouse.isButtonDown(0))
								for (int i = 0; i < fonts.size(); i++) {
									final IFontRenderer font = fonts.get(i);

									if (font.equals(fontRenderer)) {
										i++;

										if (i >= fonts.size())
											i = 0;

										fontValue.set(fonts.get(i));
										break;
									}
								}
							else
								for (int i = fonts.size() - 1; i >= 0; i--) {
									final IFontRenderer font = fonts.get(i);

									if (font.equals(fontRenderer)) {
										i--;

										if (i >= fonts.size())
											i = 0;

										if (i < 0)
											i = fonts.size() - 1;

										fontValue.set(fonts.get(i));
										break;
									}
								}
						}

						yPos += 11;
					}
					else
					{
						final String text = value.getName() + "\u00A7f: \u00A7c" + value.get();
						final float textWidth = Fonts.font35.getStringWidth(text);

						if (moduleElement.getSettingsWidth() < textWidth + 8)
							moduleElement.setSettingsWidth(textWidth + 8);

						RenderUtils.drawRect(moduleElement.getX() + moduleElement.getWidth() + 4, yPos + 2, moduleElement.getX() + moduleElement.getWidth() + moduleElement.getSettingsWidth(), yPos + 14, Integer.MIN_VALUE);
						GlStateManager.resetColor();
						Fonts.font35.drawString(text, moduleElement.getX() + moduleElement.getWidth() + 6, yPos + 4, 0xffffff);
						yPos += 12;
					}

					// This state is cleaned up in ClickGUI
					if (isNumber)
						AWTFontRenderer.Companion.setAssumeNonVolatile(true);
				}

				moduleElement.updatePressed();
				mouseDown = Mouse.isButtonDown(0);
				rightMouseDown = Mouse.isButtonDown(1);

				if (moduleElement.getSettingsWidth() > 0.0F && yPos > moduleElement.getY() + 4)
					RenderUtils.drawBorderedRect(moduleElement.getX() + moduleElement.getWidth() + 4, moduleElement.getY() + 6, moduleElement.getX() + moduleElement.getWidth() + moduleElement.getSettingsWidth(), yPos + 2, 1.0F, Integer.MIN_VALUE, 0);
			}
		}
	}

	private static BigDecimal round(final float f)
	{
		return new BigDecimal(Float.toString(f)).setScale(2, RoundingMode.HALF_UP);
	}
}
