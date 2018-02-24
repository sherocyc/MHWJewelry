package kkk.com.mhwjewelry

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import org.jetbrains.anko.db.*


class MySqlHelper(ctx: Context) : ManagedSQLiteOpenHelper(ctx, "mydb") {

    companion object {
        private var instance: MySqlHelper? = null
        @Synchronized
        fun getInstance(ctx: Context): MySqlHelper {
            if (instance == null) {
                instance = MySqlHelper(ctx.applicationContext)
            }
            return instance!!
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.createTable(JewelriesRecord::class.simpleName!!,
                true,
                "id" to INTEGER + PRIMARY_KEY + AUTOINCREMENT + UNIQUE,
                "jewelry1" to INTEGER,
                "jewelry2" to INTEGER,
                "jewelry3" to INTEGER
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    }

}

// Access property for Context
val Context.database: MySqlHelper
    get() = MySqlHelper.getInstance(applicationContext)