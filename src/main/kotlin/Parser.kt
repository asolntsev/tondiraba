import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.io.InputStream

class Parser {
  private companion object {
    private val TYPES_OF_INTEREST = arrayOf("Vabajää", "Ilu. vabajää", "EstIce Club MTÜ")

    // Club names vary: "Iluuisutamisklubi Talveunistus", "Talveunistus IK", "Juna IK/ Talveunistus IK", ...
    private val CLUBS_OF_INTEREST = arrayOf("Talveunistus")

    private fun isOfInterest(type: String) =
      type in TYPES_OF_INTEREST || CLUBS_OF_INTEREST.any { type.contains(it, ignoreCase = true) }
  }

  fun read(sourceHtml: InputStream): List<Day> {
    val html = Jsoup.parse(sourceHtml, "UTF-8", "https://tondirabaicehall.ee/veebikalender/")
    val days = html.select(".CCALdayCol")
    return days.map { Day(parseDay(it), parseSlots(it)) }.filter { it.date.isNotBlank() }
  }

  private fun parseDay(element: Element): String {
    return element.select(".CCALcolHeader .date").text()
  }

  private fun parseSlots(element: Element): List<Slot> {
    return element.select(".CCALhour .CCALinterval .CCALevent")
      .map { Slot(it.attr("data-name"), it.attr("data-start"), it.attr("data-resource")) }
      .filter { isOfInterest(it.type) }
  }

}
