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
import views.html.components.Link

class LinkSpec extends SpecBase with Matchers {
  "Link Component " - {
    "must render linkUrl correctly with linkTextKey" in new Setup{
      running(app) {
        val doc = content()

        doc.getElementsByTag("a").isEmpty mustBe false
        
        val link = doc.getElementsByTag("a").first()
        link.text() mustBe linkText
        link.attr("href") mustBe linkTextUrl
      }
    }


    "must render for paragraph component with prefixText and linkUrl if prefixTextKey is nonEmpty" in new Setup {
      running(app) {
        val prefixTextKey = "nonEmptyValue"
        val doc = content(prefixTextKey = prefixTextKey)

        doc.getElementsByTag("p").isEmpty mustBe false
        doc.getElementsByTag("p").text() mustBe s"$prefixTextKey $linkText"
      }

    }

    "must render for paragraph component with linkUrl and suffixText if suffixTextKey is nonEmpty" in new Setup {
      running(app) {
        val suffixTextKey = "nonEmptyValue"
        val doc = content(suffixTextKey= suffixTextKey)

        doc.getElementsByTag("p").isEmpty mustBe false
        doc.getElementsByTag("p").text() mustBe s"$linkText $suffixTextKey"
      }

    }

    "must render extra classes when supplied along with the default classes " in new Setup {
      running(app) {
        val extraClasses= "classes"
        val doc = content(extraClasses = extraClasses)

        doc.getElementsByClass("classes").isEmpty mustBe false
        doc.getElementsByClass("govuk-body").isEmpty mustBe false

      }

    }

    "must not open the link in new tab is isNewTab = false" in new Setup {
      running(app){
        val link = content().getElementsByTag("a").first()

        link.hasAttr("target") mustBe false
        link.hasAttr("rel") mustBe false
      }

    }

    "must open the link in new tab is isNewTab = true" in new Setup {
      running(app) {
        val link = content(newTab = true).getElementsByTag("a").first()

        link.attr("target") mustBe "_blank"
        link.attr("rel") mustBe "noreferrer noopener"
      }

    }

  }
  trait Setup {

    val app: Application = applicationBuilder(userAnswers = None).build()

    val linkText:String = "LinkText"
    val linkTextUrl: String = "https://www.gov.uk/find-hmrc-contacts/technical-support-with-hmrc-online-services"
    val isNewTab: Boolean = false
    val linkFullStop: Boolean = false

    val view: Link = views.html.components.Link()

    def content(linkText: String = linkText,
                linkUrl:String = linkTextUrl,
                prefixTextKey:String = "",
                suffixTextKey:String = "",
                extraClasses:String = "",
                newTab: Boolean = isNewTab,
                linkFullStop:Boolean = linkFullStop
               ): Document =
      Jsoup.parse(
        view(
          linkTextKey = linkText,
          linkUrl = linkUrl,
          prefixTextKey = prefixTextKey,
          suffixTextKey = suffixTextKey,
          extraClasses = extraClasses,
          isNewTab = newTab,
          linkFullStop = linkFullStop
        )(messages(app)).body
      )

  }

}
