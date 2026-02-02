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
import views.html.components.H2

class H2Spec extends SpecBase with Matchers{

  "H2 component" - {
    "render  <h2> text with default class" in new Setup {
      running(app) {
        val doc = content()

        doc.getElementsByClass(defaultClass).isEmpty mustBe false
        doc.text() mustBe message
      }
    }
    "render  <h2> text with custom Class" in new Setup {
      running(app) {
        val customClass = "class"
        val doc = content(classes = customClass)

        doc.getElementsByClass(customClass).isEmpty mustBe false
        doc.text() mustBe message
      }
    }
    "render an <h2> when id is provided" in new Setup {
      running(app) {
        val id = Some("value")
        val doc = content(id = id)

        val h2 = doc.select("h2").first()
        h2.attr("id") mustBe "value"
      }
    }
  }


  trait Setup {

    val app: Application = applicationBuilder(userAnswers = None).build()

    val view: H2 = views.html.components.H2()

    val message:String = "message"
    val defaultClass: String = "govuk-heading-m"

    def content(
               msg:String = message,
               classes:String = defaultClass,
               id:Option[String] = None
               ):Document = {
      Jsoup.parse(
        view(
          msg = msg,
          classes = classes,
          id = id
        )(messages(app)).body
      )

    }

  }

}

