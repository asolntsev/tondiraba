import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets

class Notifier {
  private val token: String? = System.getenv("TELEGRAM_BOT_TOKEN")
  private val chatId: String? = System.getenv("TELEGRAM_CHAT_ID")

  fun send(message: String) {
    if (token.isNullOrBlank() || chatId.isNullOrBlank()) return

    val body = "chat_id=${encode(chatId)}&text=${encode(message.take(MAX_LENGTH))}"
    val request = HttpRequest.newBuilder()
      .uri(URI("https://api.telegram.org/bot$token/sendMessage"))
      .header("Content-Type", "application/x-www-form-urlencoded")
      .POST(HttpRequest.BodyPublishers.ofString(body))
      .build()
    val response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString())
    check(response.statusCode() in 200..299) {
      "Telegram sendMessage failed: ${response.statusCode()} ${response.body()}"
    }
  }

  private fun encode(s: String) = URLEncoder.encode(s, StandardCharsets.UTF_8)

  private companion object {
    const val MAX_LENGTH = 4000
  }
}
