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
import views.html.components.Paragraph

class ParagraphSpec extends SpecBase with Matchers {
  "Paragraph component" - {
    "render content with base class when extraClasses are not given " in new Setup {
      running(app) {
        val doc = content()

        doc.getElementsByClass("govuk-body").isEmpty mustBe false
        doc.getElementsByClass("govuk-body").text() mustBe message
      }
    }
    "render content with base class and  extraClasses when extraClasses are given " in new Setup {
      running(app) {
        val doc = content(extraClasses = "class")

        doc.getElementsByClass("govuk-body").isEmpty mustBe false
        doc.getElementsByClass("class").isEmpty mustBe false
      }
    }
    "render content to bold with  base class when bold = true" in new Setup {
      running(app) {
        val doc = content(bold = true)

        doc.getElementsByClass("govuk-!-font-weight-bold").isEmpty mustBe false
      }
    }
  }


  trait Setup {

    val app: Application = applicationBuilder(userAnswers = None).build()

    val view: Paragraph = views.html.components.Paragraph()

    val message: String = "message"
    val bold: Boolean = false
    val baseClass: String = "govuk-body"
    val extraClasses: String = " "
    
    def content(
                 message: String = message,
                 bold: Boolean = bold,
                 baseClass: String = baseClass,
                 extraClasses: String = extraClasses,
                 args: Seq[Any] = Nil
               ): Document = {
      Jsoup.parse(
        view(
          message = message,
          bold = bold,
          baseClass = baseClass,
          extraClasses = extraClasses,
          args = args
        )(messages(app)).body
      )

    }

  }
}
