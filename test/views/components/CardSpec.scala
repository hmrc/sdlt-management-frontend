/*
 * Copyright 2026 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package views.components

import base.SpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers.{should, shouldBe}
import play.api.Application
import play.api.test.Helpers.running
import views.html.components
import views.html.components.Card

class CardSpec extends SpecBase with Matchers {

  "Card Component " - {
    "render the title " in new Setup {
      running(app) {
        val doc = content()
        doc.getElementsByClass("govuk-heading-m").isEmpty mustBe false
        doc.getElementsByClass("govuk-heading-m").text() mustBe "testTitle"
      }
    }

    "render the correct number of links " in new Setup {
      running(app) {
        content().getElementsByTag("a").size() mustBe 1
      }
    }

    "render link text and href correctly" in new Setup {
      running(app) {
        val link = content().getElementsByTag("a").first()

        link.text() mustBe "link text"
        link.attr("href") mustBe "/test-link"
      }

    }

    "should not open the links in a new tab by default" in new Setup {
      running(app) {
        val link = content().getElementsByTag("a").first()

        link.hasAttr("target") mustBe false
        link.hasAttr("rel") mustBe false
      }
    }

    "should  open the links in a new tab by when isNewTab = true " in new Setup {
      running(app) {
        val link = content(newTab = true).getElementsByTag("a").first()

        link.attr("target") mustBe "_blank"
        link.attr("rel") mustBe "noreferrer noopener"
      }

    }

    "should not render no links when links list is empty" in new Setup {
      running(app) {
        content(links = Seq.empty)
          .getElementsByTag("li").isEmpty mustBe true
      }

    }

  }

  trait Setup {

    val app: Application = applicationBuilder(userAnswers = None).build()

    val title: String = "testTitle"
    val testLinks: Seq[(String, String)] = Seq(("link text", "/test-link"))
    val isNewTab: Boolean = false

    val view: Card = views.html.components.Card()

    def content(title: String = title,
                links: Seq[(String, String)] = testLinks,
                newTab: Boolean = isNewTab
               ): Document =
      Jsoup.parse(
        view(
          title = title,
          links = links,
          isNewTab = newTab
        )(messages(app)).body
      )

  }
}



