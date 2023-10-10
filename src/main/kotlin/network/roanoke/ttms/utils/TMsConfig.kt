package network.roanoke.ttms.utils

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import net.fabricmc.loader.api.FabricLoader
import network.roanoke.ttms.TTMs
import java.io.*

class TMsConfig {

    private lateinit var _tmsData: TmsData
    val tmsData: TmsData
        get() = _tmsData

    init {
        createFolders()
        _tmsData = loadTmsData()!!
    }

    private fun createFolders() {
        val folderPath = FabricLoader.getInstance().configDir.resolve("ttms")
        val folder = folderPath.toFile()
        if (!folder.exists())
            folder.mkdir()
    }

    private fun getTmsFile(): File {
        val savePath = FabricLoader.getInstance().configDir.resolve("ttms/tms.json")
        val saveFile = savePath.toFile()
        if (!saveFile.exists()) {
            if (saveFile.createNewFile()) {
                loadStartingTms().let { array ->
                    FileWriter(saveFile).use {
                        GsonBuilder().setPrettyPrinting().create().toJson(array, it)
                    }
                }
            }
        }
        return saveFile
    }

    private fun loadStartingTms(): TmsData? {
        val jsonStream: InputStream? = TTMs::class.java.getResourceAsStream("/tms.json")
        return jsonStream?.use {
            InputStreamReader(it).use { reader ->
                val gson = Gson()
                gson.fromJson(reader, TmsData::class.java)
            }
        }
    }



    private fun loadTmsData(): TmsData? {
        val gson = Gson()
        val file = getTmsFile()

        try {
            FileReader(file).use {
                val typeToken = object : TypeToken<TmsData>() {}.type
                return gson.fromJson(it, typeToken)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }



}