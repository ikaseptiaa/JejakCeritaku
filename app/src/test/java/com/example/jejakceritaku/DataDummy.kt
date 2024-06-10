package com.example.jejakceritaku

import com.example.jejakceritaku.data.response.ListStoryItem

object DataDummy {
    fun generateDummyStoryResponse(): List<ListStoryItem> {
            val items: MutableList<ListStoryItem> = arrayListOf()
            for (i in 0..100) {
                val quote = ListStoryItem(
                    i.toString(),
                    "author + $i",
                    "quote $i",
                )
                items.add(quote)
            }
            return items
    }
}