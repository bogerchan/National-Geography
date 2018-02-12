package cc.bogerchan.geographic.dao

/**
 * Created by Boger Chan on 2018/1/28.
 */

data class NGCardData(val id: Int, val title: String, var imgUrl: String, val cardType: Int, var cardElements: MutableList<NGCardElementData>?)

data class NGCardElementData(val id: Int, val title: String, val content: String, val author: String, val imgUrl: String, val elementType: Int)