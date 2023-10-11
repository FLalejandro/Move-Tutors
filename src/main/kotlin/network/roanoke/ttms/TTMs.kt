package network.roanoke.ttms

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.event.player.UseEntityCallback
import net.minecraft.server.MinecraftServer
import network.roanoke.ttms.commands.TTMsCommand
import network.roanoke.ttms.events.UseEntityEvent
import network.roanoke.ttms.utils.TMsConfig

class TTMs : ModInitializer {

    companion object {
        private lateinit var _serverInstance: MinecraftServer

        val serverInstance: MinecraftServer
            get() = _serverInstance

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
        }

        _tmsConfig = TMsConfig()

        TTMsCommand()

        UseEntityCallback.EVENT.register(UseEntityEvent())

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