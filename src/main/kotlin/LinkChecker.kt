import com.gargoylesoftware.htmlunit.WebClient
import com.gargoylesoftware.htmlunit.html.HtmlAnchor
import com.gargoylesoftware.htmlunit.html.HtmlPage

fun main(args: Array<String>) {
    WebClient().use({ webClient ->
        val page: HtmlPage = webClient.getPage("http://htmlunit.sourceforge.net")

        // Grab all anchors
        val anchors: List<HtmlAnchor> = page.getByXPath("//a")
        print(anchors)
    })
}
