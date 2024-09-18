import com.codeborne.selenide.Configuration
import com.codeborne.selenide.Selenide.open
import com.codeborne.selenide.WebDriverRunner
import java.io.File

fun main() {

  Configuration.headless = true
  open("https://tondirabaicehall.ee/veebikalender/")

  val parser = Parser()
  val days = parser.read(WebDriverRunner.source().byteInputStream())

  val dir = File(".days")
  dir.mkdirs()

  days.forEach{day ->
    val dayFile = File(dir, "${day.date}.json")
    val previous: Day? = dayFile.takeIf { it.exists() }?.readText()?.let { Day.deserialize(it) }
    if (previous == null) {
      println("NEW DAY: ${day}")
      dayFile.writeText(day.serialize())
    }
    else if (previous != day) {
      println("ALERT! CHANGE\nfrom ${previous.serialize()}\nto ${day.serialize()}")
      dayFile.writeText(day.serialize())
    }
  }
}