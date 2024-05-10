package network.roanoke.ttms.utils

import com.cobblemon.mod.common.api.types.ElementalTypes
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
    private lateinit var _moveData: List<MoveData>
    val moveData: List<MoveData>
        get() = _moveData

    private lateinit var _customModelData: MutableMap<String, Int>
    val customModelData: MutableMap<String, Int>
        get() = _customModelData

    init {
        createFolders()

        loadTMs()
        loadCustomModelData()

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

    private fun getCMDFile(): File {
        val savePath = FabricLoader.getInstance().configDir.resolve("ttms/custom_model_data.json")
        val saveFile = savePath.toFile()
        if (!saveFile.exists()) {
            if (saveFile.createNewFile()) {
                loadStartingCMD().let { list ->
                    FileWriter(saveFile).use {
                        GsonBuilder().setPrettyPrinting().create().toJson(list, it)
                    }
                }
            }
        }
        return saveFile
    }

    private fun loadStartingCMD(): JsonArray? {
        val jsonStream: InputStream? = TTMs::class.java.getResourceAsStream("/custom_model_data.json")
        return jsonStream?.use {
            InputStreamReader(it).use { reader ->
                JsonParser.parseReader(reader).asJsonArray
            }
        }
    }

    fun loadCustomModelData() {
        val gson = Gson()
        val file = getCMDFile()

        try {
            FileReader(file).use {
                val typeToken = object : TypeToken<MutableMap<String, Int>>() {}.type
                _customModelData = gson.fromJson(it, typeToken)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        println("Loaded ${_customModelData.size} Custom Model Data Numbers")
        loadedAllTypes()
    }

    public fun loadedAllTypes(): Boolean {
        ElementalTypes.all().forEach { type ->
            if (!_customModelData.containsKey(type.name.lowercase())) {
                println("Missing Custom Model Data for ${type.name}")
                return false
            }
        }
        return true
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

    fun loadTMs() {
        val gson = Gson()
        val file = getTMsFile()

        try {
            FileReader(file).use {
                val typeToken = object : TypeToken<List<MoveData>>() {}.type
                _moveData = gson.fromJson(it, typeToken)
                println("Loaded ${_moveData.size} TMs")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
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