/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.ui.client.altmanager.menus

import com.thealtening.AltService
import net.ccbluex.liquidbounce.ui.client.altmanager.GuiAltManager
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.login.LoginUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.runAsync
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.GuiTextField
import org.lwjgl.input.Keyboard

// FIXME: The session token format changed since microsoft account migration
class GuiSessionLogin(private val prevGui: GuiScreen) : GuiScreen()
{

    // Buttons
    private lateinit var loginButton: GuiButton

    // User Input Fields
    private lateinit var sessionTokenField: GuiTextField

    // Status
    private var status = ""

    /**
     * Initialize Session Login GUI
     */
    override fun initGui()
    { // Enable keyboard repeat events
        Keyboard.enableRepeatEvents(true)

        // Add buttons to screen

        val buttonX = (width shr 1) - 100
        val quarterScreen = height shr 2

        val buttonList = buttonList

        buttonList.add(GuiButton(1, buttonX, quarterScreen + 96, "Login").also { loginButton = it })
        buttonList.add(GuiButton(0, buttonX, quarterScreen + 120, "Back"))

        // Add fields to screen
        sessionTokenField = GuiTextField(666, Fonts.font40, buttonX, 80, 200, 20)
        sessionTokenField.isFocused = true
        sessionTokenField.maxStringLength = Integer.MAX_VALUE
        sessionTokenField

        // Call sub method
        super.initGui()
    }

    /**
     * Draw screen
     */
    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float)
    { // Draw background to screen
        drawBackground(0)
        RenderUtils.drawRect(30.0f, 30.0f, width - 30.0f, height - 30.0f, Integer.MIN_VALUE)

        // Draw title and status
        val middleScreen = (width shr 1).toFloat()
        Fonts.font35.drawCenteredString("Session Login", middleScreen, 36.0f, 0xffffff)
        Fonts.font35.drawCenteredString(status, middleScreen, (height shr 2) + 80.0f, 0xffffff)

        // Draw fields
        sessionTokenField.drawTextBox()

        Fonts.font40.drawCenteredString("\u00A77Session Token:", middleScreen - 65.0f, 66.0f, 0xffffff)

        // Call sub method
        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    /**
     * Handle button actions
     */
    override fun actionPerformed(button: GuiButton)
    {
        if (!button.enabled) return

        when (button.id)
        {
            0 -> mc.displayGuiScreen(prevGui)

            1 -> processToken(sessionTokenField.text)
        }
    }

    fun processToken(token: String)
    {
        loginButton.enabled = false
        status = "\u00A7aLogging in..."

        runAsync {
            val loginResult = LoginUtils.loginSessionId(token)

            status = when (loginResult)
            {
                LoginUtils.LoginResult.LOGGED_IN ->
                {
                    if (GuiAltManager.altService.currentService != AltService.EnumAltService.MOJANG)
                    {
                        try
                        {
                            GuiAltManager.altService.switchService(AltService.EnumAltService.MOJANG)
                        }
                        catch (e: NoSuchFieldException)
                        {
                            ClientUtils.logger.error("Something went wrong while trying to switch alt service.", e)
                        }
                        catch (e: IllegalAccessException)
                        {
                            ClientUtils.logger.error("Something went wrong while trying to switch alt service.", e)
                        }
                    }

                    "\u00A7cYour name is now \u00A7f\u00A7l${mc.session.username}\u00A7c"
                }

                LoginUtils.LoginResult.FAILED_PARSE_SESSION -> "\u00A7cFailed to parse Session ID!"
                LoginUtils.LoginResult.INVALID_ACCOUNT_DATA -> "\u00A7cInvalid Session ID!"
                else -> ""
            }

            loginButton.enabled = true
        }
    }

    /**
     * Handle key typed
     */
    override fun keyTyped(typedChar: Char, keyCode: Int)
    { // Check if user want to escape from screen
        if (Keyboard.KEY_ESCAPE == keyCode)
        { // Send back to prev screen
            mc.displayGuiScreen(prevGui)

            // Quit
            return
        }

        // Check if field is focused, then call key typed
        if (sessionTokenField.isFocused) sessionTokenField.textboxKeyTyped(typedChar, keyCode)

        // Call sub method
        super.keyTyped(typedChar, keyCode)
    }

    /**
     * Handle mouse clicked
     */
    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int)
    { // Call mouse clicked to field
        sessionTokenField.mouseClicked(mouseX, mouseY, mouseButton)

        // Call sub method
        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    /**
     * Handle screen update
     */
    override fun updateScreen()
    {
        sessionTokenField.updateCursorCounter()
        super.updateScreen()
    }

    /**
     * Handle gui closed
     */
    override fun onGuiClosed()
    { // Disable keyboard repeat events
        Keyboard.enableRepeatEvents(false)

        // Call sub method
        super.onGuiClosed()
    }
}
