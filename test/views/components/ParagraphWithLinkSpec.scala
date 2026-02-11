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
import play.api.test.Helpers.*
import org.scalatest.matchers.must.Matchers
import play.api.Application
import views.html.components.ParagraphWithLink

class ParagraphWithLinkSpec extends SpecBase with Matchers {
  "ParagraphWithLink component" - {
    "render content with message when base class is not given " in new Setup {
      running(app) {
        val doc = paragraphWithLink(message)

        doc.body must include("""<a href="/someURL" id="Title">Test</a>""")
      }
    }
    "render a link when message contains anchor html" in new Setup {
      running(app) {
        val html = paragraphWithLink(message)
        val doc = Jsoup.parse(html.body)

        val link = doc.select("#Title")

        link.size() mustBe 1
        link.text() mustBe "Test"
        link.attr("href") mustBe "/someURL"
      }
    }
    "render content with base class" in new Setup {
      running(app) {
        val html = paragraphWithLink(message, baseClass)
        val doc = Jsoup.parse(html.body)

        doc.getElementsByClass("govuk-body").isEmpty mustBe false
        doc.getElementsByClass("govuk-body").text() mustBe "Test"
      }
    }
    "render content to bold with base class when isBold = true" in new Setup {
      running(app) {
        val html = paragraphWithLink(message, isBold = isBold)
        val doc = Jsoup.parse(html.body)

        doc.select("p.govuk-body strong").isEmpty mustBe false
      }
    }
    "render content with id and base class" in new Setup {
      running(app) {
        val html = paragraphWithLink(message, id = id)
        val doc = Jsoup.parse(html.body)

        doc.select("#Title").isEmpty mustBe false
      }
    }
  }


  trait Setup {

    val app: Application = applicationBuilder(userAnswers = None).build()
    val paragraphWithLink: ParagraphWithLink =
      app.injector.instanceOf[ParagraphWithLink]

    val message: String = """<a href="/someURL" id="Title">Test</a>"""
    val baseClass: String = "govuk-body"
    val isBold: Boolean = true
    val id: Option[String] = Some("#Title")
  }
}
