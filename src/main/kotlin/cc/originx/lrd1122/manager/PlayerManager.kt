package cc.originx.lrd1122.manager

import cc.originx.lrd1122.core.OriginXGamePlayer
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.info
import taboolib.module.configuration.Configuration
import java.io.File
import java.util.UUID
import kotlin.coroutines.Continuation

object PlayerManager {

    var players = HashMap<UUID, OriginXGamePlayer>()
    var dataDir = File(getDataFolder(), "data")


    fun getPlayer(uuid: UUID): OriginXGamePlayer{
        if(!players.containsKey(uuid)){ players[uuid] = OriginXGamePlayer(uuid).initialize() }
        return players[uuid]!!
    }
    @Awake(LifeCycle.ENABLE)
    fun initialize() {
        if (!dataDir.exists()) {
            dataDir.mkdir()
        }
        if (dataDir.listFiles() != null) {
            for (listFile in dataDir.listFiles()) {
                var yaml = Configuration.loadFromFile(listFile)
                var uuid = UUID.fromString(yaml.getString("uuid"))
                var gamePlayer = OriginXGamePlayer(uuid).initialize()
                players[uuid] = gamePlayer
                info("loaded data -> $uuid")
            }
        }
    }
}