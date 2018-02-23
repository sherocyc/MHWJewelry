package kkk.com.mhwjewelry

import android.app.Dialog
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.dialog_data_insert.*
import org.jetbrains.anko.db.RowParser
import org.jetbrains.anko.db.insert
import org.jetbrains.anko.db.select
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class MainActivity : AppCompatActivity(), RowParser<JewelriesRecord> {
    val adapter: JewlriesAdapter
        get() = JewlriesAdapter(loadData())
    val jewelryInfos = loadJewelryInfo();

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    fun loadJewelryInfo(): List<JewelryInfo> {
        val dbPath = (getFilesDir()?.getAbsolutePath() + "/databases/" + "jewelries.db")
        if (!File(dbPath).exists()) {
            File(getFilesDir()?.getAbsolutePath() + "/databases/").mkdir();
            try {
                val outStream = FileOutputStream(dbPath)
                val inStream = getAssets().open("jewelries.db")
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
        return db.select("jewelries").parseList(object : RowParser<JewelryInfo> {
            override fun parseRow(columns: Array<Any?>): JewelryInfo {
                return JewelryInfo(columns[0] as Long, columns[1] as String, columns[2] as Long, columns[3] as Long, columns[4] as String);
            }
        });
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_add_record -> {
                DataInsertDialog(this).create()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun loadData(): List<JewelriesRecord> {
        var result: List<JewelriesRecord> = ArrayList<JewelriesRecord>();
        database.use {
            result = select(JewelriesRecord::class.simpleName!!).parseList(this@MainActivity);
        }
        return result;
    }

    override fun parseRow(columns: Array<Any?>): JewelriesRecord {
        return JewelriesRecord(columns[0] as Long, columns[1] as String, columns[2] as String, columns[3] as String)
    }

    class JewelriesViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
        val jewelry1: TextView get() = itemView.findViewById(R.id.jewelry1)
        val jewelry2: TextView get() = itemView.findViewById(R.id.jewelry2)
        val jewelry3: TextView get() = itemView.findViewById(R.id.jewelry3)
        val id: TextView get() = itemView.findViewById(R.id.id)
        fun setData(jewelriesRecord: JewelriesRecord) {
            id.text = jewelriesRecord.id.toString()
            jewelry1.text = jewelriesRecord.jewelry1
            jewelry2.text = jewelriesRecord.jewelry2
            jewelry3.text = jewelriesRecord.jewelry3
        }
    }

    class JewlriesAdapter(var datas: List<JewelriesRecord>) : RecyclerView.Adapter<JewelriesViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): JewelriesViewHolder =
                JewelriesViewHolder(LayoutInflater.from(parent?.context).inflate(R.layout.jewelrylist_item, parent, false));

        override fun getItemCount(): Int = datas.count()

        override fun onBindViewHolder(holder: JewelriesViewHolder?, position: Int) {
            holder?.setData(datas[position]);
        }

    }

    class DataInsertDialog(context: Context?) : Dialog(context), View.OnClickListener {
        override fun onClick(p0: View?) {
            when (p0) {
                ok -> finish()
            }
        }

        fun finish() {
            context.database.use {
                insert(JewelriesRecord::class.simpleName!!,
                        "jewelry1" to edit1.text.toString(),
                        "jewelry2" to edit2.text.toString(),
                        "jewelry3" to edit3.text.toString())
            }
            dismiss();
        }

        override fun create() {
            super.create()
            setContentView(R.layout.dialog_data_insert);
            ok.setOnClickListener(this)
            setCancelable(false)
            show()
        }

    }
}
