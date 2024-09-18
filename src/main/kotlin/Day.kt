import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class Day(val date: String, val slots: List<Slot>) {
  companion object {
    private val format = Json { prettyPrint = true }
    fun deserialize(json: String): Day = format.decodeFromString(json)
  }

  fun serialize(): String {
    return format.encodeToString(this)
  }
}

@Serializable
data class Slot(val type: String, val start: String, val resource: String)