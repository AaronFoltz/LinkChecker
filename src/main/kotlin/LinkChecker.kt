import mu.KotlinLogging
import org.jsoup.Jsoup
import java.net.SocketTimeoutException

fun main(args: Array<String>) {
    LinkChecker("http://www.aaronfoltz.com").execute()
}

class LinkChecker(val baseUrl: String = "http://hckrnews.com/") {
    companion object {
        private val logger = KotlinLogging.logger {}
        private val checked = mutableMapOf<String, Result>()
    }

    fun execute(url: String = baseUrl) {
        processUrl(url);
        logFailures()
    }

    private fun processUrl(url: String) {
        if (!checked.containsKey(url)) {
            try {
                val response = Jsoup.connect(url).followRedirects(true).ignoreHttpErrors(true).ignoreContentType(true)
                        .timeout(5000).execute()
                val status = response.statusCode()

                checked.put(url, Result(url, status))

                if (200 == status) {
                    logger.info { "✔\t\t$url -\t $status" }

                    // Increase depth only with internal links...do not dig into external links.  Only parse HTML
                    if (url.contains(baseUrl) && response.contentType().startsWith("text/")) {
                        val doc = response.parse()
                        val anchors = doc.select("a[href]")
                        val anchorsToCheck = anchors.map { it.attr("abs:href") }.filter { it.isNotBlank() && !checked.containsKey(it) }

                        logger.info { "\tFound ${anchorsToCheck.size} unique anchors" }
                        anchorsToCheck.forEach { processUrl(it) }
                    }
                } else {
                    logger.warn { "✗\t\t$url -\t $status" }
                }
            } catch (ste: SocketTimeoutException) {
                checked.put(url, Result(url, -1))
                logger.warn { "✗\t\t$url -\t Timed Out" }
            } catch (t: Throwable) {
                checked.put(url, Result(url, -1))
                logger.warn { "ERROR on $url\n$t" }
            }
        }
    }

    private fun logFailures() {
        logger.warn { "--------------------------------------------------" }
        logger.warn { "Failures:" }
        logger.warn { "--------------------------------------------------" }
        checked.filter { entry -> entry.value.result == 404 }.forEach { logger.warn { it.key } }
        logger.warn { "--------------------------------------------------" }
    }
}
