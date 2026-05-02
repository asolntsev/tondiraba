import com.codeborne.selenide.Configuration
import com.codeborne.selenide.Selenide
import com.codeborne.selenide.WebDriverRunner
import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.Playwright
import org.openqa.selenium.chrome.ChromeOptions
import org.slf4j.LoggerFactory

private const val URL = "https://tondirabaicehall.ee/veebikalender/"

fun main() {
  val log = LoggerFactory.getLogger("Benchmark")
  val parser = Parser()

  val pwTotalStart = System.currentTimeMillis()
  var pwOpen = 0L
  var pwContent = 0L
  var pwParse = 0L
  var pwDays = 0
  var pwSlots = 0

  Playwright.create().use { pw ->
    val openStart = System.currentTimeMillis()
    val browser = pw.chromium().launch(
      BrowserType.LaunchOptions()
        .setHeadless(true)
        .setArgs(listOf("--no-sandbox", "--disable-gpu"))
    )
    val page = browser.newPage()
    page.navigate(URL)
    pwOpen = System.currentTimeMillis() - openStart

    val contentStart = System.currentTimeMillis()
    val source = page.content()
    pwContent = System.currentTimeMillis() - contentStart

    val parseStart = System.currentTimeMillis()
    val days = parser.read(source.byteInputStream())
    pwParse = System.currentTimeMillis() - parseStart
    pwDays = days.size
    pwSlots = days.sumOf { it.slots.size }

    browser.close()
  }
  val pwTotal = System.currentTimeMillis() - pwTotalStart

  log.info("Playwright: parsed {} days, {} matching slots", pwDays, pwSlots)
  log.info("Playwright timings — open(launch+navigate): {} ms, content: {} ms, parse: {} ms, total: {} ms",
    pwOpen, pwContent, pwParse, pwTotal)

  val seTotalStart = System.currentTimeMillis()
  System.setProperty("webdriver.httpclient.connectionTimeout", "1")
  Configuration.headless = true
  Configuration.browserCapabilities = ChromeOptions().addArguments("--no-sandbox", "--disable-gpu")

  val openStart = System.currentTimeMillis()
  Selenide.open(URL)
  val seOpen = System.currentTimeMillis() - openStart

  val sourceStart = System.currentTimeMillis()
  val source = WebDriverRunner.source()!!
  val seSource = System.currentTimeMillis() - sourceStart

  val parseStart = System.currentTimeMillis()
  val days = parser.read(source.byteInputStream())
  val seParse = System.currentTimeMillis() - parseStart
  val seDays = days.size
  val seSlots = days.sumOf { it.slots.size }

  Selenide.closeWebDriver()
  val seTotal = System.currentTimeMillis() - seTotalStart

  log.info("Selenide: parsed {} days, {} matching slots", seDays, seSlots)
  log.info("Selenide timings — open(launch+navigate): {} ms, source: {} ms, parse: {} ms, total: {} ms",
    seOpen, seSource, seParse, seTotal)

  log.info("=== Summary ===")
  log.info("Playwright total: {} ms ({} days, {} slots)", pwTotal, pwDays, pwSlots)
  log.info("Selenide total:   {} ms ({} days, {} slots)", seTotal, seDays, seSlots)
  val diff = seTotal - pwTotal
  val faster = if (diff > 0) "Playwright" else "Selenide"
  log.info("{} is faster by {} ms", faster, kotlin.math.abs(diff))
}
