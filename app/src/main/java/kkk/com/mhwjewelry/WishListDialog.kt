package kkk.com.mhwjewelry

import android.app.Dialog
import android.content.Context
import android.view.View
import android.widget.ArrayAdapter
import kkk.com.mhwjewelry.DataManager.Companion.wishList
import kotlinx.android.synthetic.main.dialog_wishlist.*

class WishListDialog(context: Context?) : Dialog(context, R.style.CustomDialog), View.OnClickListener {


    override fun create() {
        super.create()
        setContentView(R.layout.dialog_wishlist)
        add.setOnClickListener(this)

        listView.adapter = ArrayAdapter(context, R.layout.predict_item, wishList.map { DataManager.jewelryInfoMap[it]?.name })
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


    override fun onClick(v: View?) {
        when (v) {
            add -> {
                val input = edit.text.toString();
                val info = DataManager.jewelryInfos.maxBy { stringCompare(it.name, input) }
                wishList.add(info!!.id)
                context.getSharedPreferences("jewl", Context.MODE_MULTI_PROCESS).edit().putStringSet("wishList",wishList.map { it.toString() }.toSet()).apply()
                listView.adapter = ArrayAdapter(context, R.layout.predict_item, wishList.map { DataManager.jewelryInfoMap[it]?.name })
            }
        }
    }
}