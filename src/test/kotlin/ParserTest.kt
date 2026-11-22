import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ParserTest {
  private val parser = Parser()

  @Test
  fun check() {
    val days = parser.read(javaClass.getResourceAsStream("/source.html")!!)
    assertThat(days).hasSize(7)
    assertThat(days.map { it.date }).containsExactly("16.09.2024", "17.09.2024", "18.09.2024", "19.09.2024", "20.09.2024", "21.09.2024", "22.09.2024")
    assertThat(days[0]).isEqualTo(
      Day(
        "16.09.2024", listOf(
          Slot("Vabajää", "16.09.2024 07:00", "Hall 1"),
          Slot("Vabajää", "16.09.2024 08:00", "Hall 1"),
          Slot("Vabajää", "16.09.2024 09:00", "Peaareen"),
          Slot("Vabajää", "16.09.2024 10:00", "Peaareen"),
          Slot("Vabajää", "16.09.2024 10:50", "Peaareen"),
          Slot("Ilu. vabajää", "16.09.2024 13:00", "Peaareen"),
          Slot("Vabajää", "16.09.2024 13:20", "Hall 2"),
          Slot("Iluuisutamisklubi Talveunistus", "16.09.2024 14:30", "Hall 1"),
        )
      )
    )
  }

  @Test
  fun tracksTalveunistusUnderAnyName() {
    val names = listOf(
      "Talveunistus IK",
      "Juna IK/ Talveunistus IK",
      "Talveunistus IK/ Tallinna Iluuisutamiskool",
      "Tallinna Iluuisutamiskool/ Talveunistus",
      "Iluuisutamisklubi Talveunistus",
      "Tallinna Iluuisutamiskool",
      "Juna IK",
      "Eesti Uisuliit",
      "Laste vabahoki (4-14 a.v)",
    )

    val days = parser.read(html("23.11.2026", names).byteInputStream())

    assertThat(days.single().slots.map { it.type }).containsExactly(
      "Talveunistus IK",
      "Juna IK/ Talveunistus IK",
      "Talveunistus IK/ Tallinna Iluuisutamiskool",
      "Tallinna Iluuisutamiskool/ Talveunistus",
      "Iluuisutamisklubi Talveunistus",
    )
  }

  @Test
  fun tracksExactTypesOnly() {
    val names = listOf("Vabajää", "Ilu. vabajää", "EstIce Club MTÜ", "Täisk. vabahoki", "Vabajää lastele")

    val days = parser.read(html("23.11.2026", names).byteInputStream())

    assertThat(days.single().slots.map { it.type }).containsExactly("Vabajää", "Ilu. vabajää", "EstIce Club MTÜ")
  }

  private fun html(date: String, names: List<String>): String {
    val events = names.withIndex().joinToString("\n") { (i, name) ->
      """<div class="CCALevent" data-name="$name" data-start="$date ${"%02d".format(8 + i)}:00" data-resource="Hall 1"></div>"""
    }
    return """
      <html><body>
        <div class="CCALdayCol">
          <div class="CCALcolHeader"><span class="date">$date</span></div>
          <div class="CCALhour"><div class="CCALinterval">
            $events
          </div></div>
        </div>
      </body></html>
    """
  }
}
