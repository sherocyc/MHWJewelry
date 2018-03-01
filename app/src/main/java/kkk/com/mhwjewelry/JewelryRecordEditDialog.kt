package kkk.com.mhwjewelry

import android.app.Dialog
import android.content.ContentValues
import android.content.Context
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import kkk.com.mhwjewelry.DataManager.Companion.jewelryInfoMap
import kkk.com.mhwjewelry.DataManager.Companion.jewelryInfos
import kotlinx.android.synthetic.main.dialog_data_insert.*
import org.jetbrains.anko.db.insert

class JewelryRecordEditDialog(context: Context?, val updateRecord: JewelriesRecord? = null) : Dialog(context, R.style.CustomDialog), View.OnClickListener, View.OnFocusChangeListener {
    var id1: Long? = 0;
    var id2: Long? = 0;
    var id3: Long? = 0;

    var currentEditText: EditText? = null
    val wishList: HashSet<Long> = HashSet<Long>()

    override fun create() {
        super.create()
        setContentView(R.layout.dialog_data_insert);
        ok.setOnClickListener(this)
        val names = jewelryInfos.map { it.name }
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

            val input = (view as AutoCompleteTextView).text.toString().toUpperCase()

            if(!input.isBlank()) {
                val info = jewelryInfos.maxBy { stringCompare(it.name, input) }
                view.setText(info?.name)
                when (view) {
                    edit1 -> id1 = info?.id
                    edit2 -> id2 = info?.id
                    edit3 -> id3 = info?.id
                }
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
                        update(JewelriesRecord::class.simpleName!!,
                                contentValues, "id=?", arrayOf(updateRecord.id.toString()))
                    }
                    dismiss();
                }
            }
        }
    }
}