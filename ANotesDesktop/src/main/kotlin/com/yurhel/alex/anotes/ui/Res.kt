package com.yurhel.alex.anotes.ui

import androidx.compose.ui.res.useResource
import java.io.InputStreamReader
import java.util.*

class Res {

    private val listLang = listOf("be")
    private val thisLocale: String = Locale.getDefault().language
    val strings = mutableMapOf<String, String>()

    init {
        var pr = ""
        for (i in listLang) {
            if (thisLocale == i) pr = "-$i"
        }
        val res = useResource("values$pr/strings.xml") { inputStream ->
            InputStreamReader(inputStream).readLines()
        }

        for (i in res) {
            if ("<string" in i) {
                val key = i.substring(
                    i.indexOfFirst { it == '"' } + 1,
                    i.indexOfLast { it == '"' }
                )
                val value = i.substring(
                    i.indexOfFirst { it == '>' } + 1,
                    i.indexOfLast { it == '<' }
                )
                strings[key] = value
            }
        }
    }
}