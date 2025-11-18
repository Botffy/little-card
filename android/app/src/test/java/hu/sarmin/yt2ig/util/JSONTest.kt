package hu.sarmin.yt2ig.util

import com.google.common.truth.Truth.assertThat
import org.json.JSONObject
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class JSONTest {

    @Test
    fun `returns string value for simple key`() {
        val json = JSONObject("""{"name": "John"}""")

        assertThat(json.getStringOrNull("name")).isEqualTo("John")
    }

    @Test
    fun `returns string value for nested key`() {
        val json = JSONObject("""{"user": {"name": "John", "age": 30}}""")

        assertThat(json.getStringOrNull("user.name")).isEqualTo("John")
    }

    @Test
    fun `returns string value for deeply nested key`() {
        val json = JSONObject("""{"data": {"user": {"profile": {"name": "John"}}}}""")

        assertThat(json.getStringOrNull("data.user.profile.name")).isEqualTo("John")
    }

    @Test
    fun `returns null when intermediate object is missing`() {
        val json = JSONObject("""{"user": {"age": 30}}""")

        assertThat(json.getStringOrNull("user.profile.name")).isNull()
    }

    @Test
    fun `returns null when key does not exist`() {
        val json = JSONObject("""{"name": "John"}""")

        assertThat(json.getStringOrNull("email")).isNull()
    }

    @Test
    fun `returns null when value is empty string`() {
        val json = JSONObject("""{"name": ""}""")

        assertThat(json.getStringOrNull("name")).isNull()
    }

    @Test
    fun `handles whitespace in key path`() {
        val json = JSONObject("""{"user": {"name": "John"}}""")

        assertThat(json.getStringOrNull(" user . name ")).isEqualTo("John")
    }

    @Test
    fun `throws IllegalArgumentException for empty key`() {
        val json = JSONObject("""{"name": "John"}""")

        assertThrows<IllegalArgumentException> { json.getStringOrNull("") }
        assertThrows<IllegalArgumentException> { json.getStringOrNull("   ") }
    }
}

