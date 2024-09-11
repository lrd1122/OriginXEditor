package cc.originx.lrd1122.manager

import cc.originx.lrd1122.core.OriginXItemHistory
import cc.originx.lrd1122.utils.CustomRenderer
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.meta.MapMeta
import org.bukkit.map.MapRenderer
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.ProxyPlayer
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.subCommand
import taboolib.common.platform.function.info
import taboolib.common5.cint
import taboolib.library.xseries.XMaterial
import taboolib.module.chat.colored
import taboolib.module.chat.component
import taboolib.module.nms.ItemTagData
import taboolib.module.nms.getItemTag
import taboolib.module.nms.itemTagReader
import taboolib.platform.util.buildItem
import taboolib.platform.util.cancelNextChat
import taboolib.platform.util.modifyMeta
import taboolib.platform.util.nextChat
import java.awt.TextComponent
import java.lang.Compiler.command
import java.text.SimpleDateFormat
import java.util.*

@CommandHeader("originxeditor", ["oxeditor"], permission = "originx.command")
object CommandManager {
    //oxeditor item lore

    @CommandBody
    val history = subCommand {
        dynamic(comment = "list") {
            suggestion<CommandSender>() { sender, context ->
                listOf("list")
            }
            execute<ProxyPlayer> { sender, context, argument ->
                var list = PlayerManager.getPlayer(sender.uniqueId).historyItems.values
                list.sortedBy { it.timestamp }
                val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()) // 定义日期格式
                for (history in list) {
                    val text = ("${format.format(history.timestamp)} -> " +
                            "[${history.originItem.type.name}]" +
                            "(cmd=/oxeditor get ${sender.uniqueId} ${history.key} origin;" +
                            "h=${history.originItem.itemMeta?.displayName}点击获取" +
                            ")" +
                            " -> " +
                            "[${history.targetItem.type.name}]" +
                            "(cmd=/oxeditor get ${sender.uniqueId} ${history.key} target;" +
                            "h=${history.originItem.itemMeta?.displayName}点击获取" +
                            ")" +
                            " 操作者: ${history.sender}")
                                    .component()
                    text.buildColored().sendTo(sender)
                }
            }
        }
    }
    @CommandBody(hidden = true)
    val get = subCommand {
        dynamic(comment = "uuid") {
            dynamic(comment = "history") {
                dynamic(comment = "itemtype") {
                    //data -> item
                    execute<Player> { sender, context, argument ->
                        var player = PlayerManager.getPlayer(UUID.fromString(context["uuid"]))
                        var returnItem = buildItem(XMaterial.DIAMOND){
                            name = "§f未知"
                        }
                        if(player.historyItems.containsKey(context["history"])) {
                            when (context["itemtype"].lowercase()) {
                                "origin" -> {
                                    returnItem = player.historyItems[context["history"]]!!.originItem
                                }
                                "target" -> {
                                    returnItem = player.historyItems[context["history"]]!!.targetItem
                                }
                            }
                        }
                        sender.inventory.addItem(returnItem)
                    }
                }
            }
        }
    }
    @CommandBody
    val item = subCommand {
        dynamic(comment = "type") {
            suggestion<CommandSender>() { sender, context ->
                listOf("lore", "display", "nbt", "lib", "map", "potion")
            }
            dynamic(comment = "operation") {
                suggestion<CommandSender>() { sender, context ->
                    when (context["type"]) {
                        "lore" -> {
                            listOf("add", "set", "remove")
                        }

                        "display" -> {
                            listOf("set")
                        }

                        "nbt" -> {
                            listOf("info", "add")
                        }

                        "lib" -> {
                            listOf("save", "give")
                        }

                        "map" -> {
                            listOf("seturl")
                        }

                        "potion" -> {
                            listOf("add")
                        }

                        else -> {
                            listOf()
                        }
                    }
                }

                dynamic(comment = "value") {
                    execute<ProxyPlayer> { sender, context, argument ->
                        var args = getArgs(argument)
                        var player = Bukkit.getPlayer(sender.uniqueId)!!
                        var itemStack = player.inventory.itemInMainHand
                        var owner = player
                        var history = OriginXItemHistory(UUID.randomUUID().toString())
                        history.originItem = itemStack.clone();
                        history.timestamp = System.currentTimeMillis()
                        history.sender = sender.name
                        when (context["type"].lowercase()) {
                            "lore" -> {
                                when (context["operation"].lowercase()) {
                                    "add" -> {
                                        var type1 = checkArguments(args, "value")
                                        if (type1.isEmpty()) {
                                            itemStack.itemMeta =
                                                ItemManager.addItemLore(itemStack, args["value"]!!).itemMeta
                                        } else {
                                            sender.sendMessage("缺少参数: $type1")
                                        }
                                    }

                                    "set" -> {
                                        var type1 = checkArguments(args, "value", "line")
                                        if (type1.isEmpty()) {
                                            itemStack.itemMeta =
                                                ItemManager.setItemLore(
                                                    itemStack,
                                                    args["line"]!!.toInt(),
                                                    args["value"]!!
                                                ).itemMeta
                                        } else {
                                            sender.sendMessage("缺少参数: $type1")
                                        }
                                    }

                                    "remove" -> {
                                        var type1 = checkArguments(args, "line")
                                        var type2 = checkArguments(args, "value")
                                        if (type1.isEmpty()) {
                                            itemStack.itemMeta =
                                                ItemManager.removeLore(itemStack, args["line"]!!.toInt()).itemMeta
                                        } else if (type2.isEmpty()) {
                                            itemStack.itemMeta =
                                                ItemManager.removeLore(itemStack, args["value"]!!.toInt()).itemMeta
                                        } else {
                                            sender.sendMessage("缺少参数: $type1/$type2")
                                        }
                                    }
                                }
                            }

                            "nbt" -> {
                                val tag = buildItem(itemStack).getItemTag()
                                when (context["operation"].lowercase()) {
                                    "info" -> {
                                        tag.keys.forEach { key ->
                                            var message =
                                                "§a$key §7-> §e${tag[key]} §7| §6type: §d${tag[key]?.type?.name}"

                                            message.component().sendTo(sender)
                                        }
                                    }

                                    "add" -> {
                                        if (args.containsKey("key") && args.containsKey("value")) {
                                            var key = args["key"]!!
                                            var value = args["value"]!!
                                            if ((value.toIntOrNull() ?: value) != value) tag.put(
                                                key,
                                                ItemTagData(value.toInt())
                                            )
                                            else if ((value.toByteOrNull() ?: value) != value) tag.put(
                                                key,
                                                ItemTagData(value.toByte())
                                            )
                                            else tag.put(key, ItemTagData(value))
                                            tag.saveTo(itemStack)
                                            sender.sendMessage("成功为物品添加 NBT $key -> $value")
                                        }
                                    }

                                    "remove" -> {
                                        if (args.containsKey("key")) {
                                            var key = args["key"]!!
                                            tag.remove(key)
                                            tag.saveTo(itemStack)
                                            sender.sendMessage("成功移除 NBT -> $key")
                                        }
                                    }
                                }
                            }

                            "display" -> {
                                when (context["operation"].lowercase()) {
                                    "set" -> {
                                        var type1 = checkArguments(args, "value")
                                        if (type1.isEmpty()) {
                                            itemStack.itemMeta =
                                                ItemManager.setDisplayName(
                                                    itemStack,
                                                    args["value"]!!
                                                ).itemMeta
                                        } else {
                                            sender.sendMessage("缺少参数: $type1")
                                        }
                                    }
                                }
                            }

                            "lib" -> {
                                when (context["operation"].lowercase()) {
                                    "save" -> {
                                        if (args.containsKey("key")) {
                                            ItemManager.save(args["key"]!!, player.inventory.itemInMainHand)
                                            sender.sendMessage("保存物品 -> ${args["key"]}")
                                        }
                                    }

                                    "give" -> {
                                        if (args.containsKey("key")) {
                                            var key = args["key"]
                                            if (ItemManager.items.containsKey(key)) {
                                                var args = getArgs(argument)
                                                var target = player
                                                var itemStack = ItemManager.items[key]?.itemStack!!.clone()
                                                if (args.containsKey("target") && Bukkit.getPlayer(args["target"]!!) != null) target =
                                                    Bukkit.getPlayer(args["target"]!!)!!
                                                if (args.containsKey("amount")) itemStack.amount =
                                                    args["amount"]!!.toInt()
                                                if (args.containsKey("slot")) {
                                                    target.inventory.setItem(args["slot"]!!.toInt(), itemStack)
                                                } else {
                                                    target.inventory.addItem(itemStack)
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            "map" -> {
                                when (context["operation"].lowercase()) {
                                    "seturl" -> {
                                        var returnItem = buildItem(itemStack).modifyMeta<MapMeta> {
                                            val mapView = Bukkit.createMap(player.world)
                                            mapView.addRenderer(
                                                CustomRenderer(
                                                    url = args.getOrDefault(
                                                        "url",
                                                        "https://img.picui.cn/free/2024/09/11/66e19b7d9ad32.png"
                                                    )
                                                )
                                            )
                                            this.mapView = mapView
                                        }
                                        player.inventory.setItemInMainHand(returnItem)
                                    }
                                }
                            }

                            "potion" -> {
                                when (context["operation"].lowercase()) {
                                    "add" -> {
                                        var returnItem = buildItem(itemStack) {
                                            potions.add(
                                                PotionEffect(
                                                    PotionEffectType.getByName(
                                                        args.getOrDefault("potion", "HEAL").uppercase()
                                                    )!!,
                                                    args.getOrDefault("duration", 1800).cint,
                                                    args.getOrDefault("amplifier", 1).cint
                                                )
                                            )
                                        }
                                        player.inventory.setItemInMainHand(returnItem)
                                    }

                                    "remove" -> {
                                        var returnItem = buildItem(itemStack) {
                                            potions.removeIf { element ->
                                                element.type == PotionEffectType.getByName(
                                                    args.getOrDefault("potion", "HEAL").uppercase()
                                                )!!
                                            }
                                        }
                                        player.inventory.setItemInMainHand(returnItem)
                                    }

                                    "set" -> {
                                        var returnItem = buildItem(itemStack) {
                                            potions.removeIf { element ->
                                                element.type == PotionEffectType.getByName(
                                                    args.getOrDefault("potion", "HEAL").uppercase()
                                                )!!
                                            }
                                            potions.add(
                                                PotionEffect(
                                                    PotionEffectType.getByName(
                                                        args.getOrDefault("potion", "HEAL").uppercase()
                                                    )!!,
                                                    args.getOrDefault("duration", 1800).cint,
                                                    args.getOrDefault("amplifier", 1).cint
                                                )
                                            )
                                        }
                                        player.inventory.setItemInMainHand(returnItem)
                                    }
                                }
                            }
                        }
                        history.targetItem = itemStack.clone()
                        PlayerManager.getPlayer(owner.uniqueId).addHistoryItem(history)
                    }
                }
            }
        }
    }

    fun checkArguments(map: HashMap<String, String>, vararg args: String): String {
        for (arg in args) {
            if (!map.containsKey(arg)) return arg
        }
        return ""
    }
    fun getArgs(argument: String): HashMap<String, String>{
        var args = HashMap<String, String>()
        argument.split(" ").forEach { s ->
            s.split("=").takeIf { it.size == 2 }?.let { (key, value) ->
                args[key] = value
            }
        }
        return args
    }
    @CommandBody
    val entity = subCommand {

    }
}