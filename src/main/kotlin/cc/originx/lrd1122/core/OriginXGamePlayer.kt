package cc.originx.lrd1122.core

import org.bukkit.configuration.file.YamlConfiguration
import taboolib.common.io.newFile
import taboolib.common.platform.function.getDataFolder
import taboolib.library.xseries.setItemStack
import taboolib.module.configuration.Configuration
import taboolib.module.configuration.Type
import java.io.File
import java.util.UUID

class OriginXGamePlayer(var uuid: UUID) {

    var historyItems = HashMap<String, OriginXItemHistory>()

    var dir = File(getDataFolder(), "data")
    var file = File(dir, "$uuid.yml")
    lateinit var yaml: YamlConfiguration;
    fun save(): OriginXGamePlayer {
        yaml.set("uuid", uuid.toString())
        for (value in historyItems.values) {
            yaml.set("historyItems.${value.key}.originItem", value.originItem)
            yaml.set("historyItems.${value.key}.targetItem", value.targetItem)
            yaml.set("historyItems.${value.key}.sender", value.sender)
            yaml["historyItems.${value.key}.timestamp"] = value.timestamp
        }
        yaml.save(file)
        return this
    }

    fun initialize(): OriginXGamePlayer {
        if(!dir.exists()) dir.mkdir()
        if(!file.exists()) newFile(dir, "$uuid.yml", create = true)
        yaml = YamlConfiguration.loadConfiguration(file)

        if(yaml.contains("historyItems")){
            var sec = yaml.getConfigurationSection("historyItems")
            for (key in sec!!.getKeys(false)) {
                var history = OriginXItemHistory(key)
                history.originItem = sec.getItemStack("$key.originItem")!!
                history.targetItem = sec.getItemStack("$key.targetItem")!!
                history.sender = sec.getString("$key.sender", "").toString()
                history.timestamp = sec.getLong("$key.timestamp")
                historyItems[key] = history
            }
        }
        return this
    }
    fun addHistoryItem(history: OriginXItemHistory) {
        this.historyItems[history.key] = history
        this.save()
    }
}