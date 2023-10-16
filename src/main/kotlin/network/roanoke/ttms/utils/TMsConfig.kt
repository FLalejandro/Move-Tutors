package network.roanoke.ttms.utils

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import net.fabricmc.loader.api.FabricLoader
import network.roanoke.ttms.TTMs
import java.io.*
import java.util.*

class TMsConfig {
    private val _moveData: List<MoveData>
    val moveData: List<MoveData>
        get() = _moveData

    init {
        createFolders()
        _moveData = loadTMs() ?: emptyList()
        println("Loaded ${_moveData.size} TMs")

        if (npcFileExists())
            loadNPCs()
    }

    private fun createFolders() {
        val folderPath = FabricLoader.getInstance().configDir.resolve("ttms")
        val folder = folderPath.toFile()
        if (!folder.exists()) {
            folder.mkdir()
        }
    }

    private fun getTMsFile(): File {
        val savePath = FabricLoader.getInstance().configDir.resolve("ttms/tms.json")
        val saveFile = savePath.toFile()
        if (!saveFile.exists()) {
            if (saveFile.createNewFile()) {
                loadStartingTMs().let { list ->
                    FileWriter(saveFile).use {
                        GsonBuilder().setPrettyPrinting().create().toJson(list, it)
                    }
                }
            }
        }
        return saveFile
    }

    private fun loadStartingTMs(): JsonArray? {
        val jsonStream: InputStream? = TTMs::class.java.getResourceAsStream("/tms.json")
        return jsonStream?.use {
            InputStreamReader(it).use { reader ->
                JsonParser.parseReader(reader).asJsonArray
            }
        }
    }

    private fun loadTMs(): List<MoveData>? {
        val gson = Gson()
        val file = getTMsFile()

        try {
            FileReader(file).use {
                val typeToken = object : TypeToken<List<MoveData>>() {}.type
                return gson.fromJson(it, typeToken)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }

    private fun getNPCFile(): File {
        val savePath = FabricLoader.getInstance().configDir.resolve("ttms/npcs.json")
        val saveFile = savePath.toFile()
        saveFile.createNewFile()
        return saveFile
    }

    private fun npcFileExists(): Boolean {
        val savePath = FabricLoader.getInstance().configDir.resolve("ttms/npcs.json")
        val saveFile = savePath.toFile()
        return saveFile.exists()
    }

    fun saveNPCs() {
        val gson = GsonBuilder().setPrettyPrinting().create()
        val file = getNPCFile()

        FileWriter(file).use {
            gson.toJson(TTMs.npcs, it)
        }
    }

    private fun loadNPCs() {
        val gson = Gson()
        val file = getNPCFile()
        FileReader(file).use { reader ->
            val uuidStrings = gson.fromJson(reader, Array<String>::class.java)
            val uuidList = uuidStrings.map { UUID.fromString(it) }
            TTMs.setNPCs(uuidList)
        }
    }

}