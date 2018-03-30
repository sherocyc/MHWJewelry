package kkk.com.mhwjewelry

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import kkk.com.mhwjewelry.DataManager.Companion.jewelryInfoMap
import kkk.com.mhwjewelry.DataManager.Companion.wishList
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import org.jetbrains.anko.db.RowParser
import org.jetbrains.anko.db.insert
import org.jetbrains.anko.db.select

class MainActivity : AppCompatActivity(), View.OnClickListener, View.OnLongClickListener {

    val adapter: JewlriesAdapter = JewlriesAdapter()

    var currentIndex: Long = -1
        set(value) {
            field = value
            getSharedPreferences("jewl", Context.MODE_PRIVATE).edit().putLong("missonIndex", currentIndex).apply()
        }


    var currentStepType = StepType.A
        set(value) {
            field = value
            getSharedPreferences("jewl", Context.MODE_PRIVATE).edit().putInt("stepmode", currentStepType.index).apply()
        }


    enum class StepType(val index: Int, val steplength: Int) {
        A(0, 2),
        B(1, 1),
        C(2, 1);

        companion object {
            fun fromIndex(index: Int): StepType = when {
                index < 0 -> C
                index == 0 -> A
                index == 1 -> B
                index == 2 -> C
                else -> A
            }
        }

        operator fun plus(value: Int): StepType {
            return StepType.fromIndex(index + value)
        }

        operator fun minus(value: Int): StepType {
            return StepType.fromIndex(index - value)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        btnMisson.setOnClickListener(this)
//        btnMisson.setOnLongClickListener(this)
        btnAlchemy.setOnClickListener(this)
//        btnAlchemy.setOnLongClickListener(this)

        currentIndex = getSharedPreferences("jewl", Context.MODE_PRIVATE).getLong("missonIndex", 1)
        currentStepType = StepType.fromIndex(getSharedPreferences("jewl", Context.MODE_PRIVATE).getInt("stepmode", 0))

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        loadData()
        recyclerView.scrollToPosition(currentIndex.toInt());
    }

    override fun onClick(v: View?) {
        when (v) {
            btnMisson -> {
                currentIndex = currentIndex + currentStepType.steplength
                Toast.makeText(this, "任务推进 进度+" + currentStepType.steplength, Toast.LENGTH_SHORT).show()
                database.use{
                    insert(TaskHistory::class.simpleName!!,
                            "time" to System.currentTimeMillis(),
                            "type" to 0,
                            "stepLength" to currentStepType.steplength)
                }

                currentStepType = currentStepType + 1
                loadData()
                recyclerView.scrollToPosition(currentIndex.toInt())
            }
            btnAlchemy -> {
                currentIndex = currentIndex + 1
//                currentStepType = currentStepType + 1
                Toast.makeText(this, "执行炼金 进度+1", Toast.LENGTH_SHORT).show()
                database.use{
                    insert(TaskHistory::class.simpleName!!,
                            "time" to System.currentTimeMillis(),
                            "type" to 1,
                            "stepLength" to 1)
                }

                loadData()
                recyclerView.scrollToPosition(currentIndex.toInt())
            }
        }
    }

    override fun onLongClick(v: View?): Boolean {
        when (v) {
            btnMisson -> {
                currentStepType = currentStepType - 1
                currentIndex = currentIndex - currentStepType.steplength
                Toast.makeText(this, "任务推进回溯 进度 -" + currentStepType.steplength, Toast.LENGTH_SHORT).show()
                loadData()
                recyclerView.scrollToPosition(currentIndex.toInt())
            }
            btnAlchemy -> {
                currentIndex = currentIndex - 1
//                currentStepType = currentStepType - 1
                Toast.makeText(this, "执行炼金回溯 进度-1", Toast.LENGTH_SHORT).show()
                loadData()
                recyclerView.scrollToPosition(currentIndex.toInt())
            }
        }
        return true
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
                val dialog = JewelryRecordEditDialog(this)
                dialog.setOnDismissListener {
                    loadData()
                    recyclerView.scrollToPosition(recyclerView.adapter.itemCount);
                }
                dialog.create()
                true
            }
            R.id.action_wish_list -> {
                val dialog = WishListDialog(this)
                dialog.setOnDismissListener {
                    loadData()
                }
                dialog.create()
                true
            }
            R.id.stepType -> {
                val builder = AlertDialog.Builder(this)
                builder.setTitle(R.string.step_type)
                builder.setItems(arrayOf("A(+2)", "B(+1)", "C(+1)"), { dialogInterface, i ->
                    currentStepType = StepType.fromIndex(i)
                    loadData()
                })

                builder.show()
                true
            }
            R.id.taskHistory ->{
                startActivity(Intent(this,TaskHistoryActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun loadData() {
        var result: List<JewelriesRecord> = ArrayList<JewelriesRecord>()
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
                                val dialog = JewelryRecordEditDialog(context, jewelriesRecord)
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

        fun getShowName(id: Long): String? = when (id) {
            0.toLong() -> {
                "N/A"
            }
            else -> jewelryInfoMap[id]?.name
        }

        fun setData(jewelriesRecord: JewelriesRecord) {
            this.jewelriesRecord = jewelriesRecord
            id.text = jewelriesRecord.id.toString()
            jewelry1.text = getShowName(jewelriesRecord.jewelry1)
            jewelry2.text = getShowName(jewelriesRecord.jewelry2)
            jewelry3.text = getShowName(jewelriesRecord.jewelry3)
            if (wishList.contains(jewelriesRecord.jewelry1)) {
                jewelry1.setTextColor(Color.parseColor("#FF0000"))
            } else {
                jewelry1.setTextColor(Color.parseColor("#000000"))
            }

            if (wishList.contains(jewelriesRecord.jewelry2)) {
                jewelry2.setTextColor(Color.parseColor("#FF0000"))
            } else {
                jewelry2.setTextColor(Color.parseColor("#000000"))
            }

            if (wishList.contains(jewelriesRecord.jewelry3)) {
                jewelry3.setTextColor(Color.parseColor("#FF0000"))
            } else {
                jewelry3.setTextColor(Color.parseColor("#000000"))
            }

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

    class JewlriesAdapter : RecyclerView.Adapter<JewelriesViewHolder>() {
        val datas: ArrayList<JewelriesRecord> = ArrayList()

        fun setDatas(datas: Collection<JewelriesRecord>) {
            this.datas.clear()
            this.datas.addAll(datas)
        }

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): JewelriesViewHolder =
                JewelriesViewHolder(LayoutInflater.from(parent?.context).inflate(R.layout.jewelrylist_item, parent, false))

        override fun getItemCount(): Int = datas.count()

        override fun onBindViewHolder(holder: JewelriesViewHolder?, position: Int) {
            holder?.setData(datas[position])
        }

    }

}
