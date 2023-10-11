package network.roanoke.ttms.utils

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import net.fabricmc.loader.api.FabricLoader
import network.roanoke.ttms.TTMs
import java.io.*

class TMsConfig {
    private val _moveData: List<MoveData>
    val moveData: List<MoveData>
        get() = _moveData

    init {
        createFolders()
        _moveData = loadMoveData() ?: emptyList()
        println("Loaded ${_moveData.size} TMs")
    }

    private fun createFolders() {
        val folderPath = FabricLoader.getInstance().configDir.resolve("ttms")
        val folder = folderPath.toFile()
        if (!folder.exists()) {
            folder.mkdir()
        }
    }

    private fun getMoveDataFile(): File {
        val savePath = FabricLoader.getInstance().configDir.resolve("ttms/tms.json")
        val saveFile = savePath.toFile()
        if (!saveFile.exists()) {
            if (saveFile.createNewFile()) {
                loadStartingMoveData().let { list ->
                    FileWriter(saveFile).use {
                        GsonBuilder().setPrettyPrinting().create().toJson(list, it)
                    }
                }
            }
        }
        return saveFile
    }

    private fun loadStartingMoveData(): JsonArray? {
        val jsonStream: InputStream? = TTMs::class.java.getResourceAsStream("/tms.json")
        return jsonStream?.use {
            InputStreamReader(it).use { reader ->
                JsonParser.parseReader(reader).asJsonArray
            }
        }
    }

    private fun loadMoveData(): List<MoveData>? {
        val gson = Gson()
        val file = getMoveDataFile()

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
}