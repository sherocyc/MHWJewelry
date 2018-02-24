package kkk.com.mhwjewelry

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import kkk.com.mhwjewelry.DataManager.Companion.jewelryInfoMap
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import org.jetbrains.anko.db.RowParser
import org.jetbrains.anko.db.select

class MainActivity : AppCompatActivity(), View.OnClickListener {
    val adapter: JewlriesAdapter = JewlriesAdapter()

    var currentIndex: Long = -1
        get() = field
        set(value) {
            field = value
            getSharedPreferences("jewl", Context.MODE_MULTI_PROCESS).edit().putLong("missonIndex", currentIndex).apply()
        }


    var currentStepType = StepType.A
        get() = field
        set(value) {
            field = value
            getSharedPreferences("jewl", Context.MODE_MULTI_PROCESS).edit().putInt("stepmode", currentStepType.index).apply()
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
                var dialog = JewelryRecordEditDialog(this);
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
                                var dialog = JewelryRecordEditDialog(context, jewelriesRecord);
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

}
