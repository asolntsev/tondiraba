import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy

class DayTest {
  @Test
  fun toJson() {
    assertThat(Day("31.12.2023", listOf(Slot("Vaba", "09:00", "Main hall"))).serialize())
      .isEqualTo(
        """
        {
            "date": "31.12.2023",
            "slots": [
                {
                    "type": "Vaba",
                    "start": "09:00",
                    "resource": "Main hall"
                }
            ]
        }""".trimIndent()
      )
  }
}