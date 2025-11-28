package hu.sarmin.yt2ig

import android.content.Context

data class ErrorMessage(val code: String, val params: List<String> = emptyList()) {
    fun toMessage(context: Context): String {
        val identifier = context.resources.getIdentifier(this.code, "string", context.packageName)
        return if (identifier != 0) {
            context.getString(identifier, params)
        } else {
            "Unknown error: ${this.code}"
        }
    }

    constructor(e: Exception) : this("error_any", listOf(e.message ?: "unknown error"))
}
