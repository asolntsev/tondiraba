object Quotes {
  private val all: List<String> = javaClass.getResource("/quotes.txt")!!
    .readText()
    .lineSequence()
    .map { it.trim() }
    .filter { it.isNotEmpty() && !it.startsWith("#") }
    .toList()

  fun random(): String = all.random()
}
