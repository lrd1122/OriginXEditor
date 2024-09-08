package cc.originx.lrd1122.core

import org.bukkit.inventory.ItemStack
import taboolib.library.xseries.XMaterial
import taboolib.platform.util.buildItem

class OriginXItem(var key: String) {
    var itemStack: ItemStack = buildItem(XMaterial.DIAMOND){
        name = "§f未设定"
    }
}