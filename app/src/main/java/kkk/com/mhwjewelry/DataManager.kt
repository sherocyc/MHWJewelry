package kkk.com.mhwjewelry

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import org.jetbrains.anko.db.RowParser
import org.jetbrains.anko.db.select
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class DataManager() {
    companion object {
        val jewelryInfos: ArrayList<JewelryInfo> = ArrayList();
        val jewelryInfoMap: HashMap<Long, JewelryInfo> = HashMap();
        fun init(context: Context){
            val dbPath = (context.getFilesDir()?.getAbsolutePath() + "/databases/" + "jewelries.db")
            if (!File(dbPath).exists()) {
                File(context.getFilesDir()?.getAbsolutePath() + "/databases/").mkdir();
                try {
                    val outStream = FileOutputStream(dbPath)
                    val inStream = context.getAssets().open("jewelries.db")
                    val buffer = ByteArray(1024)
                    var readBytes = inStream.read(buffer)
                    while (readBytes != -1) {
                        outStream.write(buffer, 0, readBytes)
                        readBytes = inStream.read(buffer)
                    }
                    inStream.close()
                    outStream.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            val db = SQLiteDatabase.openOrCreateDatabase(File(dbPath), null)

            if (jewelryInfos.isEmpty()) {
                jewelryInfos.addAll(db.select("jewelries").parseList(object : RowParser<JewelryInfo> {
                    override fun parseRow(columns: Array<Any?>): JewelryInfo {
                        return JewelryInfo(columns[0] as Long, columns[1] as String, columns[2] as Long, columns[3] as Long, columns[4] as String);
                    }
                }))
                jewelryInfos.forEach { jewelryInfoMap[it.id] = it }
            }
            db.close();
        }
    }

}