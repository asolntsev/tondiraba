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
          Slot("MTÜ Eesti Uisuliit", "16.09.2024 12:00", "Peaareen"),
          Slot("Ilu. vabajää", "16.09.2024 13:00", "Peaareen"),
          Slot("Vabajää", "16.09.2024 13:20", "Hall 2"),
          Slot("Iluuisutamisklubi Talveunistus", "16.09.2024 14:30", "Hall 1"),
          Slot("MTÜ Eesti Uisuliit", "16.09.2024 17:10", "Peaareen"),
          Slot("MTÜ Eesti Uisuliit", "16.09.2024 18:40", "Peaareen"),
        )
      )
    )
  }
}