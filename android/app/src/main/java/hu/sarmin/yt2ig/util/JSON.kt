package hu.sarmin.yt2ig.util

import org.json.JSONObject

fun JSONObject.getStringOrNull(key: String): String? {
    val segments = key.trim()
        .split('.')
        .mapNotNull { it.trim().takeIf { s -> s.isNotBlank() } }
        .ifEmpty { throw IllegalArgumentException("Key cannot be empty") }

    var current: JSONObject? = this
    for ((index, segment) in segments.withIndex()) {
        current = when {
            current == null -> return null
            index == segments.lastIndex -> return current.optString(segment).takeIf { it.isNotEmpty() }
            else -> current.optJSONObject(segment)
        }
    }

    throw IllegalStateException()
}
