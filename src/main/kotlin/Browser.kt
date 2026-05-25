import com.codeborne.selenide.Condition.exist
import com.codeborne.selenide.Configuration
import com.codeborne.selenide.Selenide
import com.codeborne.selenide.Selenide.`$`
import com.codeborne.selenide.WebDriverRunner
import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import com.microsoft.playwright.options.WaitUntilState
import org.openqa.selenium.chrome.ChromeOptions
import java.time.Duration

// The calendar shell ships in the initial HTML, but rows are populated by an XHR
// to compucash5.com that completes after DOMContentLoaded. Wait for an event row
// so we don't capture the empty shell.
private const val CALENDAR_RENDERED_MARKER = ".CCALevent"

fun fetchWithPlaywright(url: String): String =
  Playwright.create().use { pw ->
    val browser = pw.chromium().launch(
      BrowserType.LaunchOptions()
        .setHeadless(true)
        .setArgs(listOf("--no-sandbox", "--disable-gpu"))
    )
    val page = browser.newPage()
    page.navigate(url, Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED))
    page.waitForSelector(CALENDAR_RENDERED_MARKER)
    val source = page.content()
    browser.close()
    source
  }

fun fetchWithSelenide(url: String): String {
  System.setProperty("webdriver.httpclient.connectionTimeout", "1")
  Configuration.headless = true
  Configuration.pageLoadStrategy = "eager"
  Configuration.browserCapabilities = ChromeOptions().addArguments("--no-sandbox", "--disable-gpu")
  Selenide.open(url)
  `$`(CALENDAR_RENDERED_MARKER).should(exist, Duration.ofSeconds(30))
  val source = WebDriverRunner.source()!!
  Selenide.closeWebDriver()
  return source
}
