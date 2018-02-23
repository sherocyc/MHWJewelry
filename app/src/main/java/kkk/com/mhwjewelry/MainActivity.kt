package kkk.com.mhwjewelry

import android.app.Dialog
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
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

class MainActivity : AppCompatActivity(), View.OnClickListener {

    val adapter: JewlriesAdapter = JewlriesAdapter()

    var currentIndex: Int = getSharedPreferences("jewl", Context.MODE_MULTI_PROCESS).getInt("missonIndex", 1);

    val PASSED = 1;
    val AVAILABLE = 1;

    companion object {
        val jewelryInfos: ArrayList<JewelryInfo> = ArrayList();
        val jewelryInfoMap: HashMap<Long, JewelryInfo> = HashMap();

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        btnMisson.setOnClickListener(this)
        btnAlchemy.setOnClickListener(this)

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

        if (jewelryInfos.isEmpty()) {
            jewelryInfos.addAll(db.select("jewelries").parseList(object : RowParser<JewelryInfo> {
                override fun parseRow(columns: Array<Any?>): JewelryInfo {
                    return JewelryInfo(columns[0] as Long, columns[1] as String, columns[2] as Long, columns[3] as Long, columns[4] as String);
                }
            }))
            jewelryInfos.forEach { jewelryInfoMap[it.id] = it }
        }


        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        loadData();
    }

    override fun onClick(v: View?) {
        when (v) {
            btnMisson -> {

            }
            btnAlchemy -> {

            }
        }
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
                var dialog = DataInsertDialog(this);
                dialog.setOnDismissListener {
                    loadData()
                }
                dialog.create()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun loadData() {
        var result: List<JewelriesRecord> = ArrayList<JewelriesRecord>();
        database.use {
            result = select(JewelriesRecord::class.simpleName!!).parseList(object : RowParser<JewelriesRecord> {
                override fun parseRow(columns: Array<Any?>): JewelriesRecord {
                    return JewelriesRecord(columns[0] as Long,
                            columns[1] as Long,
                            columns[2] as Long,
                            columns[3] as Long,
                            when {
                                (columns[0] as Long) < currentIndex -> PASSED
                                else -> AVAILABLE
                            })
                }
            })
        }

        adapter.setDatas(result)
        adapter.notifyDataSetChanged()
    }


    class JewelriesViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
        val jewelry1: TextView get() = itemView.findViewById(R.id.jewelry1)
        val jewelry2: TextView get() = itemView.findViewById(R.id.jewelry2)
        val jewelry3: TextView get() = itemView.findViewById(R.id.jewelry3)
        val id: TextView get() = itemView.findViewById(R.id.id)
        var jewelriesRecord: JewelriesRecord? = null;
        fun setData(jewelriesRecord: JewelriesRecord) {
            this.jewelriesRecord = jewelriesRecord
            id.text = jewelriesRecord.id.toString()
            jewelry1.text = jewelryInfoMap[jewelriesRecord.jewelry1]?.name
            jewelry2.text = jewelryInfoMap[jewelriesRecord.jewelry2]?.name
            jewelry3.text = jewelryInfoMap[jewelriesRecord.jewelry3]?.name
        }
    }

    class JewlriesAdapter() : RecyclerView.Adapter<JewelriesViewHolder>() {
        val datas: ArrayList<JewelriesRecord> = ArrayList();

        fun setDatas(datas: Collection<JewelriesRecord>) {
            this.datas.clear()
            this.datas.addAll(datas)
        }

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): JewelriesViewHolder =
                JewelriesViewHolder(LayoutInflater.from(parent?.context).inflate(R.layout.jewelrylist_item, parent, false))

        override fun getItemCount(): Int = datas.count()

        override fun onBindViewHolder(holder: JewelriesViewHolder?, position: Int) {
            holder?.setData(datas[position]);
        }

    }

    class DataInsertDialog(context: Context?) : Dialog(context, R.style.CustomDialog), View.OnClickListener, View.OnFocusChangeListener {
        var id1: Long? = 0;
        var id2: Long? = 0;
        var id3: Long? = 0;

        var currentEditText: EditText? = null

        override fun create() {
            super.create()
            setContentView(R.layout.dialog_data_insert);
            ok.setOnClickListener(this)
            val names = jewelryInfos.map { it.name };
            edit1.setAdapter(ArrayAdapter(context, R.layout.predict_item, names))
            edit1.onFocusChangeListener = this
            edit2.setAdapter(ArrayAdapter(context, R.layout.predict_item, names))
            edit2.onFocusChangeListener = this
            edit3.setAdapter(ArrayAdapter(context, R.layout.predict_item, names))
            edit3.onFocusChangeListener = this
            setCancelable(false)
            show()
        }

        fun stringCompare(source: CharSequence, target: CharSequence): Int {
            var score: Int = 0;
            val minSize = Math.min(source.length, target.length);
            for (i in 0 until minSize) {
                if (source[i].equals(target[i])) score++
            }
            return score;
        }

        override fun onFocusChange(view: View?, hasFocus: Boolean) {
            if (!hasFocus) {
                currentEditText = null;
                val input = (view as AutoCompleteTextView).text.toString();
                val info = jewelryInfos.maxBy { stringCompare(it.name, input) }
                view.setText(info?.name)
                when (view) {
                    edit1 -> id1 = info?.id
                    edit2 -> id2 = info?.id
                    edit3 -> id3 = info?.id
                }
            } else {
                currentEditText = view as EditText;
            }
        }

        override fun onClick(view: View?) {
            when (view?.id) {
                R.id.ok -> {
                    context.database.use {
                        onFocusChange(currentEditText, false)
                        insert(JewelriesRecord::class.simpleName!!,
                                "jewelry1" to id1,
                                "jewelry2" to id2,
                                "jewelry3" to id3)
                        dismiss();
                    }
                }
            }
        }

    }
}
