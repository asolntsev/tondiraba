import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.io.InputStream

class Parser {
  private companion object {
    private val TYPES_OF_INTEREST = arrayOf("Vabajää", "MTÜ Eesti Uisuliit", "Ilu. vabajää", "Iluuisutamisklubi Talveunistus")
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
      .filter { it.type in TYPES_OF_INTEREST }
  }

}
