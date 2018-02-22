package kkk.com.mhwjewelry

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import org.jetbrains.anko.db.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class MySqlHelper(ctx: Context) : ManagedSQLiteOpenHelper(ctx, "mydb") {

    companion object {
        private var instance: MySqlHelper? = null
        private var context: Context? = null
        @Synchronized
        fun getInstance(ctx: Context): MySqlHelper {
            if (instance == null) {
                context = ctx.applicationContext;
                instance = MySqlHelper(ctx.applicationContext)
            }
            return instance!!
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        if (context == null) return;
        val dbPath = (context!!.getFilesDir()?.getAbsolutePath() + "/databases/" + "jewelries.db")
        if (!File(dbPath).exists()) {
            File(context!!.getFilesDir()?.getAbsolutePath() + "/databases/").mkdir();
            try {
                val outStream = FileOutputStream(dbPath)
                val inStream = context!!.getAssets().open("jewelries.db")
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
        SQLiteDatabase.openOrCreateDatabase(File(dbPath), null);
        db.createTable(JewelriesRecord::class.simpleName!!,
                true,
                "_id" to INTEGER + PRIMARY_KEY + AUTOINCREMENT + UNIQUE,
                "jewelry1" to TEXT,
                "jewelry2" to TEXT,
                "jewelry3" to TEXT
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    }

}

// Access property for Context
val Context.database: MySqlHelper
    get() = MySqlHelper.getInstance(applicationContext)