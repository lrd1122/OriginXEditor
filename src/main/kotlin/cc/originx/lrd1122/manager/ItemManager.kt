package cc.originx.lrd1122.manager

import cc.originx.lrd1122.core.OriginXItem
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.inventory.ItemStack
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.function.getDataFolder
import taboolib.library.xseries.getItemStack
import taboolib.library.xseries.setItemStack
import taboolib.module.configuration.Configuration
import taboolib.platform.util.buildItem
import java.io.File

object ItemManager {

    var itemsDir = File(getDataFolder(), "items")
    var items = HashMap<String, OriginXItem>()

    @Awake(LifeCycle.ENABLE)
    fun initialize() {
        if(!itemsDir.exists()) itemsDir.mkdir()
        if(itemsDir.listFiles() != null) {
            for (listFile in itemsDir.listFiles()) {
                if (listFile.name.endsWith(".yml")) {
                    var yaml = Configuration.loadFromFile(listFile)
                    for (key in yaml.getKeys(false)) {
                        var sec = yaml.getConfigurationSection(key)
                        var originXItem = OriginXItem(key)
                        originXItem.itemStack = sec?.getItemStack("itemStack")!!

                        items[key] = originXItem
                    }
                }
            }
        }
    }
    fun save(key: String, item: ItemStack) {
        val originXItem = OriginXItem(key)
        originXItem.itemStack = item.clone()
        var file = File(itemsDir, "$key.yml")
        if(!file.exists()) file.createNewFile()
        var yaml = Configuration.loadFromFile(file)
        yaml.setItemStack("$key.itemStack", item)
        yaml.saveToFile(file)
        items[key] = originXItem
    }
    fun saveall(){
        for (item in items.values) {
            var key = item.key
            var file = File(itemsDir, "$key.yml")
            if(!file.exists()) file.createNewFile()
            var yaml = Configuration.loadFromFile(file)
            yaml.setItemStack("$key.itemStack", item.itemStack!!)
            yaml.saveToFile(file)
        }
    }
    fun addItemLore(itemStack: ItemStack, str: String): ItemStack {
         return buildItem(itemStack) { lore.add(str) }
    }
    fun setItemLore(itemStack: ItemStack, line: Int, str: String): ItemStack {
        return buildItem(itemStack) { lore[line] = str }
    }
    fun removeLore(itemStack: ItemStack, line: Int): ItemStack {
        return buildItem(itemStack) { lore.removeAt(line) }
    }
    fun removeLore(itemStack: ItemStack, value: String): ItemStack {
        return buildItem(itemStack) {
            lore.removeAll { it == value }
        }
    }
    fun setDisplayName(itemStack: ItemStack, str: String): ItemStack {
        return buildItem(itemStack) { name = str }
    }
}