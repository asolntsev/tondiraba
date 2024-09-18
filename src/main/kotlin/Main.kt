import com.codeborne.selenide.Configuration
import com.codeborne.selenide.Selenide.open
import com.codeborne.selenide.WebDriverRunner
import org.slf4j.LoggerFactory
import java.io.File

fun main() {
  val log = LoggerFactory.getLogger("Main")

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
      log.info("Look! A new day has come: {}", day)
      dayFile.writeText(day.serialize())
    }
    else if (previous != day) {
      log.info("Look! Schedule has changed for {}\nfrom {}\nto {}", day.date, previous.serialize(), day.serialize())
      dayFile.writeText(day.serialize())
    }
  }
}