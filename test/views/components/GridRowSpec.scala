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
import play.api.Application
import play.api.test.Helpers.running
import play.twirl.api.Html
import views.html.components.GridRow

class GridRowSpec extends SpecBase with Matchers {

  "GridRow component" - {
    "render multiple cards inside the GridRow" in new Setup {
      running(app) {
        val doc = content()

        doc.getElementsByClass("govuk-grid-row").isEmpty mustBe false
        doc.getElementsByClass("card").size() mustBe 2
        doc.text() must include("Card1")
        doc.text() must include("Card2")
      }
    }
  }


  trait Setup {

    val app: Application = applicationBuilder(userAnswers = None).build()

    val view: GridRow = views.html.components.GridRow()

    val card1: Html = Html("<div class='card'>Card1</div>")
    val card2: Html = Html("<div class='card'>Card2</div>")

    def content(cards: Html*): Document = {
      Jsoup.parse(
        view(
          card1,
          card2
        )(messages(app)).body
      )

    }

  }

}
