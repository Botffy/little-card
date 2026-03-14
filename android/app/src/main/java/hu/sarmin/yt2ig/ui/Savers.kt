package hu.sarmin.yt2ig.ui

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.listSaver
import hu.sarmin.yt2ig.Parsing
import hu.sarmin.yt2ig.ParsingSaver

val ErrorStateSaver = listSaver<MutableState<Pair<String, Parsing.Error>?>, Any?>(
    save = { state ->
        val pair = state.value
        if (pair == null) emptyList()
        else listOf(pair.first, ParsingSaver.save(pair.second))
    },
    restore = { list ->
        val pair = if (list.isEmpty()) null
        else {
            val text = list[0] as String
            val parsing = (ParsingSaver.restore(list[1] as String) as? Parsing.Error)
                ?: Parsing.Error.InvalidUrl
            Pair(text, parsing)
        }
        mutableStateOf(pair)
    }
)
