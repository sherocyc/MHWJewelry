package kkk.com.mhwjewelry

import android.app.Dialog
import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kkk.com.mhwjewelry.DataManager.Companion.wishList
import kotlinx.android.synthetic.main.dialog_wishlist.*

class WishListDialog(context: Context?) : Dialog(context, R.style.CustomDialog), View.OnClickListener {

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var id: Long = 0
            set(value) {
                field = value
                itemView.findViewById<TextView>(R.id.name).text = DataManager.jewelryInfoMap[value]?.name
            }
        init{
            itemView.findViewById<View>(R.id.btnDelete).setOnClickListener {
                wishList.remove(id)
                context.getSharedPreferences("jewl", Context.MODE_PRIVATE).edit().putStringSet("wishList", wishList.map { it.toString() }.toSet()).apply()
                recyclerView.adapter.notifyDataSetChanged()
            }
        }
    }
    override fun create() {
        super.create()
        setContentView(R.layout.dialog_wishlist)
        add.setOnClickListener(this)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = object : RecyclerView.Adapter<VH?>() {

            override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): VH? =
                    VH(LayoutInflater.from(parent?.context).inflate(R.layout.wishlist_item, parent, false))

            override fun getItemCount(): Int =
                    DataManager.wishList.size


            override fun onBindViewHolder(holder: VH?, position: Int) {
                holder?.id = DataManager.wishList.toList().sorted()[position]
            }
        }

        show()
    }

    fun stringCompare(source: CharSequence, target: CharSequence): Int {
        var score = 0
        val minSize = Math.min(source.length, target.length)
        for (i in 0 until minSize) {
            if (source[i].equals(target[i])) score++
        }
        return score
    }


    override fun onClick(v: View?) {
        when (v) {
            add -> {
                val input = edit.text.toString()
                val info = DataManager.jewelryInfos.maxBy { stringCompare(it.name, input) }
                wishList.add(info!!.id)
                context.getSharedPreferences("jewl", Context.MODE_PRIVATE).edit().putStringSet("wishList", wishList.map { it.toString() }.toSet()).apply()
                recyclerView.adapter.notifyDataSetChanged()
            }
        }
    }
}