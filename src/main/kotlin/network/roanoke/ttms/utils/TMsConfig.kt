package network.roanoke.ttms.utils

import com.cobblemon.mod.common.api.types.ElementalTypes
import com.google.gson.*
import com.google.gson.reflect.TypeToken
import net.fabricmc.loader.api.FabricLoader
import network.roanoke.ttms.TTMs
import java.io.*
import java.util.*

class TMsConfig {
    private lateinit var _tmsMoveData: List<MoveData>
    val tmsMoveData: List<MoveData>
        get() = _tmsMoveData

    private lateinit var _trsMoveData: List<MoveData>
    val trsMoveData: List<MoveData>
        get() = _trsMoveData

    private lateinit var _customModelData: MutableMap<String, Int>
    val customModelData: MutableMap<String, Int>
        get() = _customModelData

    init {
        createFolders()

        loadMoves()
        loadCustomModelData()

        if (npcFileExists("tr") && npcFileExists("tm"))
            loadNPCs()
    }

    private fun createFolders() {
        val folderPath = FabricLoader.getInstance().configDir.resolve("TTMs")
        val folder = folderPath.toFile()
        if (!folder.exists()) {
            folder.mkdir()
        }
    }

    private fun getCMDFile(): File {
        val savePath = FabricLoader.getInstance().configDir.resolve("TTMs/custom_model_data.json")
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

    private fun loadStartingCMD(): JsonObject? {
        val jsonStream: InputStream? = TTMs::class.java.getResourceAsStream("/custom_model_data.json")
        return jsonStream?.use {
            InputStreamReader(it).use { reader ->
                JsonParser.parseReader(reader).asJsonObject
            }
        }
    }

    fun loadCustomModelData() {
        val gson = Gson()
        val file = getCMDFile()

        try {
            FileReader(file).use {
                val typeToken = object : TypeToken<Map<String, Int>>() {}.type
                _customModelData = gson.fromJson(it, typeToken)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        println("Loaded ${_customModelData.size} Custom Model Data Numbers")
        loadedAllTypes()
    }

    fun loadedAllTypes(): Boolean {
        ElementalTypes.all().forEach { type ->
            if (!_customModelData.containsKey(type.name.lowercase())) {
                println("Missing Custom Model Data for ${type.name}")
                return false
            }
        }
        return true
    }

    private fun getMovesFile(type: String): File {
        val savePath = FabricLoader.getInstance().configDir.resolve("TTMs/$type.json")
        val saveFile = savePath.toFile()
        if (!saveFile.exists()) {
            if (saveFile.createNewFile()) {
                loadStartingMoves().let { list ->
                    FileWriter(saveFile).use {
                        GsonBuilder().setPrettyPrinting().create().toJson(list, it)
                    }
                }
            }
        }
        return saveFile
    }

    private fun loadStartingMoves(): JsonArray? {
        val jsonStream: InputStream? = TTMs::class.java.getResourceAsStream("/tms.json")
        return jsonStream?.use {
            InputStreamReader(it).use { reader ->
                JsonParser.parseReader(reader).asJsonArray
            }
        }
    }

    private fun loadMoves(type: String) {
        val gson = Gson()
        val file = getMovesFile(type)

        try {
            FileReader(file).use {
                val typeToken = object : TypeToken<List<MoveData>>() {}.type
                when (type) {
                    "trs" -> _trsMoveData = gson.fromJson(it, typeToken)
                    "tms" -> _tmsMoveData = gson.fromJson(it, typeToken)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (type == "trs")
            println("Loaded ${_trsMoveData.size} TR Moves")
        else
            println("Loaded ${_tmsMoveData.size} TM Moves")
    }

    fun loadMoves() {
        loadMoves("trs")
        loadMoves("tms")
    }

    private fun getNPCFile(type: String): File {
        val savePath = FabricLoader.getInstance().configDir.resolve("TTMs/${type}_npcs.json")
        val saveFile = savePath.toFile()
        saveFile.createNewFile()
        return saveFile
    }

    private fun npcFileExists(type: String): Boolean {
        val savePath = FabricLoader.getInstance().configDir.resolve("TTMs/${type}_npcs.json")
        val saveFile = savePath.toFile()
        return saveFile.exists()
    }

    fun saveNPCs() {
        saveNPCs("tr")
        saveNPCs("tm")
    }

    private fun saveNPCs(type: String) {
        val gson = GsonBuilder().setPrettyPrinting().create()
        val file = getNPCFile(type)
        FileWriter(file).use {
            when (type) {
                "tr" -> gson.toJson(TTMs.trNpcs, it)
                "tm" -> gson.toJson(TTMs.tmNpcs, it)
            }
        }
    }

    private fun loadNPCs() {
        loadNPCs("tr")
        loadNPCs("tm")
    }

    private fun loadNPCs(type: String) {
        val gson = Gson()
        val file = getNPCFile(type)
        FileReader(file).use { reader ->
            val uuidStrings = gson.fromJson(reader, Array<String>::class.java)
            val uuidList = uuidStrings.map { UUID.fromString(it) }
            when (type) {
                "tr" -> TTMs.setTrNPCs(uuidList)
                "tm" -> TTMs.setTmNPCs(uuidList)
            }
        }
    }

}