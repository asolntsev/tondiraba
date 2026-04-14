import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets

class Notifier {
  private val token: String? = System.getenv("TELEGRAM_BOT_TOKEN")
  private val chatIds: List<String> = (System.getenv("TELEGRAM_CHAT_ID") ?: "")
    .split(",")
    .map { it.trim() }
    .filter { it.isNotEmpty() }

  fun send(message: String) {
    val token = this.token?.takeIf { it.isNotBlank() } ?: return
    if (chatIds.isEmpty()) return

    val text = message.take(MAX_LENGTH)
    val errors = mutableListOf<String>()
    chatIds.forEach { chatId ->
      runCatching { sendTo(token, chatId, text) }
        .onFailure { errors.add("$chatId: ${it.message}") }
    }
    check(errors.isEmpty()) { "Telegram sendMessage failed: ${errors.joinToString("; ")}" }
  }

  private fun sendTo(token: String, chatId: String, text: String) {
    val body = "chat_id=${encode(chatId)}&text=${encode(text)}"
    val request = HttpRequest.newBuilder()
      .uri(URI("https://api.telegram.org/bot$token/sendMessage"))
      .header("Content-Type", "application/x-www-form-urlencoded")
      .POST(HttpRequest.BodyPublishers.ofString(body))
      .build()
    val response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString())
    check(response.statusCode() in 200..299) {
      "${response.statusCode()} ${response.body()}"
    }
  }

  private fun encode(s: String) = URLEncoder.encode(s, StandardCharsets.UTF_8)

  private companion object {
    const val MAX_LENGTH = 4000
  }
}
