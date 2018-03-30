package kkk.com.mhwjewelry

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_taskhistory.*
import org.jetbrains.anko.db.RowParser
import org.jetbrains.anko.db.select
import java.util.*

class TaskHistoryActivity : AppCompatActivity() {

    val adapter: TaskHistoryAdapter
        get() = TaskHistoryAdapter(loadData())


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_taskhistory)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = adapter;
    }

    fun loadData(): List<TaskHistory> {
        var result: List<TaskHistory> = ArrayList<TaskHistory>()
        database.use {
            result = select(TaskHistory::class.simpleName!!).parseList(object : RowParser<TaskHistory> {
                override fun parseRow(columns: Array<Any?>): TaskHistory {
                    return TaskHistory(columns[0] as Long,
                            columns[1] as Long,
                            columns[2] as Long,
                            columns[3] as Long)
                }
            })
        }
        return result;
    }

    class TaskHistoryViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
        val time: TextView = itemView!!.findViewById(R.id.time)
        val type: TextView = itemView!!.findViewById(R.id.type)
        val stepLength: TextView = itemView!!.findViewById(R.id.stepLength)

        fun setData(data: TaskHistory) {
            time.text = Date(data.time).toString()
            type.text = when (data.type) {
                0.toLong() -> "任务"
                1.toLong() -> "炼金"
                else -> ""
            }
            stepLength.text = data.stepLength.toString()
        }
    }

    class TaskHistoryAdapter(val datas: List<TaskHistory>) : RecyclerView.Adapter<TaskHistoryViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): TaskHistoryViewHolder =
                TaskHistoryViewHolder(LayoutInflater.from(parent?.context).inflate(R.layout.taskhistory_item, parent, false))

        override fun getItemCount(): Int = datas.size

        override fun onBindViewHolder(holder: TaskHistoryViewHolder?, position: Int) {
            holder?.setData(datas[position])
        }
    }
}