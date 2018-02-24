package kkk.com.mhwjewelry

import android.app.Dialog
import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.*
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

    var currentIndex: Long = -1
        get()=field
        set(value: Long) {
            field = value
            getSharedPreferences("jewl", Context.MODE_MULTI_PROCESS).edit().putLong("missonIndex", currentIndex).apply()
        }


    var currentStepType = StepType.A
        get()=field
        set(value: StepType) {
            field = value
            getSharedPreferences("jewl", Context.MODE_MULTI_PROCESS).edit().putInt("stepmode", currentStepType.index).apply()
        }

    companion object {
        val jewelryInfos: ArrayList<JewelryInfo> = ArrayList();
        val jewelryInfoMap: HashMap<Long, JewelryInfo> = HashMap();

    }


    enum class StepType(val index: Int, val steplength: Int) {
        A(0, 2),
        B(1, 1),
        C(2, 1);

        companion object {
            fun fromIndex(index: Int): StepType = when (index) {
                0 -> A
                1 -> B
                2 -> C
                else -> A
            }
        }

        operator fun plus(value: Int): StepType {
            return StepType.fromIndex(index + value);
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        btnMisson.setOnClickListener(this)
        btnAlchemy.setOnClickListener(this)
        currentIndex = getSharedPreferences("jewl", Context.MODE_MULTI_PROCESS).getLong("missonIndex", 1)
        currentStepType = StepType.fromIndex(getSharedPreferences("jewl", Context.MODE_MULTI_PROCESS).getInt("stepmode", 0))

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
        db.close();

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        loadData();
    }

    override fun onClick(v: View?) {
        when (v) {
            btnMisson -> {
                currentIndex = currentIndex + 1
                currentStepType = currentStepType + 1
                loadData()
            }
            btnAlchemy -> {
                currentIndex = currentIndex + 1
                loadData()
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
                                (columns[0] as Long) < currentIndex -> JewelriesRecord.PASSED
                                (columns[0] as Long) == currentIndex -> JewelriesRecord.CURRENT
                                (columns[0] as Long) == currentIndex + currentStepType.steplength -> JewelriesRecord.NEXT
                                else -> JewelriesRecord.AVAILABLE
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
        val indicator: ImageView get() = itemView.findViewById(R.id.indicator)
        val passedIndicator: View get() = itemView.findViewById(R.id.passedIndicator)

        var jewelriesRecord: JewelriesRecord? = null
        val context get() = itemView.context

        init {
            itemView?.setOnLongClickListener {
                val builder = AlertDialog.Builder(context)
                val dialog = builder.setItems(arrayOf("修改"), object : DialogInterface.OnClickListener {
                    override fun onClick(p0: DialogInterface?, index: Int) {
                        when (index) {
                            0 -> {
                                var dialog = DataInsertDialog(context, jewelriesRecord);
                                dialog.setOnDismissListener {
                                    (context as MainActivity).loadData()
                                }
                                dialog.create()
                            }
                        }
                    }
                }).create()
                dialog.show()
                true
            }
        }

        fun setData(jewelriesRecord: JewelriesRecord) {
            this.jewelriesRecord = jewelriesRecord
            id.text = jewelriesRecord.id.toString()
            jewelry1.text = jewelryInfoMap[jewelriesRecord.jewelry1]?.name
            jewelry2.text = jewelryInfoMap[jewelriesRecord.jewelry2]?.name
            jewelry3.text = jewelryInfoMap[jewelriesRecord.jewelry3]?.name
            when (jewelriesRecord.status) {
                JewelriesRecord.PASSED -> {
                    passedIndicator.visibility = View.VISIBLE
                    indicator.setImageResource(0)
                }
                JewelriesRecord.CURRENT -> {
                    passedIndicator.visibility = View.GONE
                    indicator.setImageResource(R.mipmap.arrow_red)
                }
                JewelriesRecord.NEXT -> {
                    passedIndicator.visibility = View.GONE
                    indicator.setImageResource(R.mipmap.arrow_gray)
                }

                else -> {
                    passedIndicator.visibility = View.GONE
                    indicator.setImageResource(0)
                }
            }
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

    class DataInsertDialog(context: Context?, val updateRecord: JewelriesRecord? = null) : Dialog(context, R.style.CustomDialog), View.OnClickListener, View.OnFocusChangeListener {
        var id1: Long? = 0;
        var id2: Long? = 0;
        var id3: Long? = 0;

        var currentEditText: EditText? = null
        override fun create() {
            super.create()
            setContentView(R.layout.dialog_data_insert);
            ok.setOnClickListener(this)
            val names = jewelryInfos.map { it.name };
            if (updateRecord != null) {
                id1 = updateRecord.jewelry1
                id2 = updateRecord.jewelry2
                id3 = updateRecord.jewelry3

                edit1.setText(jewelryInfoMap[updateRecord.jewelry1]?.name)
                edit2.setText(jewelryInfoMap[updateRecord.jewelry2]?.name)
                edit3.setText(jewelryInfoMap[updateRecord.jewelry3]?.name)
            }
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
                        if (currentEditText != null)
                            onFocusChange(currentEditText, false)
                        if (updateRecord == null)
                            insert(JewelriesRecord::class.simpleName!!,
                                    "jewelry1" to id1,
                                    "jewelry2" to id2,
                                    "jewelry3" to id3)
                        else {
                            val contentValues = ContentValues()
                            contentValues.put("jewelry1", id1)
                            contentValues.put("jewelry2", id2)
                            contentValues.put("jewelry3", id3)
                            var result = update(JewelriesRecord::class.simpleName!!,
                                    contentValues, "id=?", arrayOf(updateRecord.id.toString()))
                        }
                        dismiss();
                    }
                }
            }
        }

    }
}
