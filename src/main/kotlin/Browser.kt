import com.codeborne.selenide.Configuration
import com.codeborne.selenide.Selenide
import com.codeborne.selenide.WebDriverRunner
import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.Playwright
import org.openqa.selenium.chrome.ChromeOptions

fun fetchWithPlaywright(url: String): String =
  Playwright.create().use { pw ->
    val browser = pw.chromium().launch(
      BrowserType.LaunchOptions()
        .setHeadless(true)
        .setArgs(listOf("--no-sandbox", "--disable-gpu"))
    )
    val source = browser.newPage().also { it.navigate(url) }.content()
    browser.close()
    source
  }

fun fetchWithSelenide(url: String): String {
  System.setProperty("webdriver.httpclient.connectionTimeout", "1")
  Configuration.headless = true
  Configuration.pageLoadStrategy = "eager"
  Configuration.browserCapabilities = ChromeOptions().addArguments("--no-sandbox", "--disable-gpu")
  Selenide.open(url)
  val source = WebDriverRunner.source()!!
  Selenide.closeWebDriver()
  return source
}
