package network.roanoke.ttms

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.event.player.UseEntityCallback
import net.luckperms.api.LuckPerms
import net.luckperms.api.LuckPermsProvider
import net.minecraft.server.MinecraftServer
import network.roanoke.ttms.commands.TTMsCommand
import network.roanoke.ttms.events.UseNPCEvent
import network.roanoke.ttms.events.UseTMEvent
import network.roanoke.ttms.utils.TMsConfig
import java.util.*

class TTMs : ModInitializer {

    companion object {
        private lateinit var _serverInstance: MinecraftServer

        val serverInstance: MinecraftServer
            get() = _serverInstance

        var luckPerms: LuckPerms? = null

        private val _npcModePlayers: MutableMap<UUID, String> = mutableMapOf()
        val npcModePlayers: MutableMap<UUID, String>
            get() = _npcModePlayers

        private val _trNpcs: MutableList<UUID> = mutableListOf()
        val trNpcs: MutableList<UUID>
            get() = _trNpcs

        fun setTrNPCs(npcs: List<UUID>) {
            _trNpcs.clear()
            _trNpcs.addAll(npcs)
        }

        private val _tmNpcs: MutableList<UUID> = mutableListOf()
        val tmNpcs: MutableList<UUID>
            get() = _tmNpcs

        fun setTmNPCs(npcs: List<UUID>) {
            _tmNpcs.clear()
            _tmNpcs.addAll(npcs)
        }

        private lateinit var _tmsConfig: TMsConfig
        val tmsConfig: TMsConfig
            get() = _tmsConfig

        private const val _cooldown: Int = 1
        val cooldown: Int
            get() = _cooldown

        private var _counter: Int = 0
        val counter: Int
            get() = _counter

        var onCooldown: Boolean = false
    }


    override fun onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register { server: MinecraftServer ->
            _serverInstance = server

            try {
                luckPerms = LuckPermsProvider.get()
            } catch (e: Exception) {
                println("LuckPerms not found.")
            }
        }

        _tmsConfig = TMsConfig()

        TTMsCommand()

        UseEntityCallback.EVENT.register(UseTMEvent())
        UseEntityCallback.EVENT.register(UseNPCEvent())

        ServerTickEvents.START_SERVER_TICK.register {
            if (onCooldown) {
                if (counter > cooldown)  {
                    onCooldown = false
                    _counter = 0
                } else {
                    _counter++
                }
            }
        }
    }
}