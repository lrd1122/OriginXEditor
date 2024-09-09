package cc.originx.lrd1122.core

import org.bukkit.inventory.ItemStack

class OriginXItemHistory(var key: String) {
    lateinit var originItem: ItemStack
    lateinit var targetItem: ItemStack
    var historyCommand = ""
    var timestamp: Long = 0L

}