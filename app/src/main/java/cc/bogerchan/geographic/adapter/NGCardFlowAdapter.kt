package cc.bogerchan.geographic.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import cc.bogerchan.geographic.R
import cc.bogerchan.geographic.dao.NGCardData
import com.facebook.drawee.view.SimpleDraweeView
import java.util.*

/**
 * Created by Boger Chan on 2018/1/31.
 */
class NGCardFlowAdapter : RecyclerView.Adapter<NGCardFlowAdapter.ViewHolder>() {

    var cardData: List<NGCardData> = Collections.emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val sdvImage: SimpleDraweeView by lazy { itemView.findViewById<SimpleDraweeView>(R.id.sdv_item_card_flow) }

        val tvText: TextView by lazy { itemView.findViewById<TextView>(R.id.gtv_item_card_flow) }
    }

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        holder?.apply {
            sdvImage.setImageURI(cardData[position].imgUrl)
            tvText.text = cardData[position].title
        }
    }

    override fun getItemCount() = cardData.size

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int)
            = ViewHolder(LayoutInflater.from(parent!!.context).inflate(R.layout.item_card_flow, parent, false))
}