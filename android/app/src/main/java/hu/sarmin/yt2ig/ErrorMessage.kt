package hu.sarmin.yt2ig

import android.content.Context

fun Parsing.Error.toMessage(context: Context): String {
    val identifier = context.resources.getIdentifier(this.code(), "string", context.packageName)
    return if (identifier != 0) {
        context.getString(identifier)
    } else {
        "Unknown error: ${this.code()}"
    }
}
