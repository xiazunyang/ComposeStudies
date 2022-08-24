package cn.numeron.study02

fun interface MviErrorTextExtractor {

    fun extract(throwable: Throwable): String

}