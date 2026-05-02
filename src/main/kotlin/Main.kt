import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

private const val URL = "https://tondirabaicehall.ee/veebikalender/"

fun main() {
  val log = LoggerFactory.getLogger("Main")

  timedFetch(log, "Playwright") { fetchWithPlaywright(URL) }
  val source = timedFetch(log, "Selenide") { fetchWithSelenide(URL) }
    ?: error("Selenide fetch failed — see log above")

  val parser = Parser()
  val days = parser.read(source.byteInputStream())

  if (days.isNotEmpty() && days.all { it.slots.isEmpty() }) {
    log.warn("Timetable looks empty for all {} days — skipping update to avoid wiping stored schedule", days.size)
    return
  }

  val dir = File(".days")
  dir.mkdirs()

  val changes = mutableListOf<String>()

  days.forEach { day ->
    val dayFile = File(dir, "${day.date}.json")
    val previous: Day? = dayFile.takeIf { it.exists() }?.readText()?.let { Day.deserialize(it) }
    if (previous == null) {
      log.info("Look! A new day has come: {}", day)
      dayFile.writeText(day.serialize())
      changes.add(formatNewDay(day))
    }
    else if (previous != day) {
      log.info("Look! Schedule has changed for {}\nfrom {}\nto {}", day.date, previous.serialize(), day.serialize())
      dayFile.writeText(day.serialize())
      changes.add(formatDiff(previous, day))
    }
  }

  if (changes.isNotEmpty()) {
    Notifier().send(changes.joinToString("\n\n") + "\n\n" + Quotes.random())
  }
}

private fun timedFetch(log: Logger, name: String, fetch: () -> String): String? {
  val start = System.currentTimeMillis()
  return try {
    val source = fetch()
    log.info("{} OK in {} ms ({} chars)", name, System.currentTimeMillis() - start, source.length)
    source
  } catch (e: Exception) {
    log.warn("{} FAILED after {} ms: {}", name, System.currentTimeMillis() - start, e.toString())
    null
  }
}

private val inputDate = DateTimeFormatter.ofPattern("dd.MM.yyyy")
private val outputDate = DateTimeFormatter.ofPattern("EEE d MMM", Locale.ENGLISH)

private fun formatNewDay(day: Day): String = buildString {
  append(formatDate(day.date))
  day.slots.forEach { append("\n  + ${formatSlot(it)}") }
}

private fun formatDiff(previous: Day, current: Day): String = buildString {
  append(formatDate(current.date))
  (current.slots - previous.slots.toSet()).forEach { append("\n  + ${formatSlot(it)}") }
  (previous.slots.toSet() - current.slots.toSet()).forEach { append("\n  - ${formatSlot(it)}") }
}

private fun formatDate(date: String): String =
  LocalDate.parse(date, inputDate).format(outputDate)

private fun formatSlot(slot: Slot): String {
  val time = slot.start.substringAfter(' ')
  return "$time  ${slot.type}  (${slot.resource})"
}
