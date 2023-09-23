/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */

package net.ccbluex.liquidbounce.utils.item

import net.ccbluex.liquidbounce.injection.implementations.IMixinItemStack
import net.ccbluex.liquidbounce.utils.MinecraftInstance
import net.minecraft.enchantment.Enchantment
import net.minecraft.item.*
import net.minecraft.nbt.JsonToNBT
import net.minecraft.util.ResourceLocation

object ItemUtils : MinecraftInstance() {
    /**
     * Allows you to create an item using the item json
     *
     * @param itemArguments arguments of item
     * @return created item
     */
    fun createItem(itemArguments: String): ItemStack? {
        return try {
            val args = itemArguments.replace('&', '§').split(" ")

            val amount = args.getOrNull(1)?.toInt() ?: 1
            val meta = args.getOrNull(2)?.toInt() ?: 0

            val resourceLocation = ResourceLocation(args[0])
            val item = Item.itemRegistry.getObject(resourceLocation) ?: return null

            val itemStack = ItemStack(item, amount, meta)

            if (args.size >= 4) {
                val nbt = args.drop(3).joinToString(" ")

                itemStack.tagCompound = JsonToNBT.getTagFromJson(nbt)
            }

            itemStack
        } catch (exception: Exception) {
            exception.printStackTrace()
            null
        }
    }

    fun getItems(startInclusive: Int = 0, endInclusive: Int = 44,
                 itemDelay: Int? = null, filter: ((ItemStack, Int) -> Boolean)? = null): Map<Int, ItemStack> {
        val items = mutableMapOf<Int, ItemStack>()

        for (i in startInclusive..endInclusive) {
            val itemStack = mc.thePlayer.inventoryContainer.getSlot(i).stack ?: continue

            if (itemStack.isEmpty)
                continue

            if (itemDelay != null && !itemStack.hasItemAgePassed(itemDelay))
                continue

            if (filter?.invoke(itemStack, i) != false)
                items[i] = itemStack
        }

        return items
    }


    /**
     * Allows you to check if player is consuming item
     */
    fun isConsumingItem(): Boolean {
        if (!mc.thePlayer.isUsingItem) {
            return false
        }

        val usingItem = mc.thePlayer.itemInUse.item
        return usingItem is ItemFood || usingItem is ItemBucketMilk || usingItem is ItemPotion
    }
}

/**
 *
 * Item extensions
 *
 */

val ItemStack.durability
    get() = maxDamage - itemDamage

val ItemStack.totalDurability
    get() = durability * (getEnchantmentLevel(Enchantment.unbreaking) + 1)

val ItemStack.enchantments: Map<Enchantment, Int>
    get() {
        val enchantments = mutableMapOf<Enchantment, Int>()

        if (this.enchantmentTagList == null || enchantmentTagList.hasNoTags())
            return enchantments

        repeat(enchantmentTagList.tagCount()) {
            val tagCompound = enchantmentTagList.getCompoundTagAt(it)
            if (tagCompound.hasKey("ench") || tagCompound.hasKey("id"))
                enchantments[Enchantment.getEnchantmentById(tagCompound.getInteger("id"))] = tagCompound.getInteger("lvl")
        }

        return enchantments
    }

val ItemStack.enchantmentCount
    get() = enchantments.size

// Returns sum of levels of all enchantment levels
val ItemStack.enchantmentSum
    get() = enchantments.values.sum()

fun ItemStack.getEnchantmentLevel(enchantment: Enchantment) = enchantments.getOrDefault(enchantment, 0)

val ItemStack?.isEmpty
    get() = this == null || item == null

@Suppress("CAST_NEVER_SUCCEEDS")
fun ItemStack?.hasItemAgePassed(delay: Int) = this == null
        || System.currentTimeMillis() - (this as IMixinItemStack).itemDelay >= delay

val ItemStack.attackDamage
    get() = (attributeModifiers["generic.attackDamage"].firstOrNull()?.amount ?: 0.0) +
            1.25 * getEnchantmentLevel(Enchantment.sharpness)

fun ItemStack.isSplashPotion() = item is ItemPotion && ItemPotion.isSplash(this.metadata)