package cn.numeron.study01

fun interface MviErrorTextExtractor {

    fun extract(throwable: Throwable): String

}