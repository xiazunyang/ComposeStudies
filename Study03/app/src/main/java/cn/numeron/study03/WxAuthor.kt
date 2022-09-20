package cn.numeron.study03

data class WxAuthor(val id: Long, val name: String) {
    override fun toString(): String = name
}