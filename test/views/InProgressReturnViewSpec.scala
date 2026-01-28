/*
 * Copyright 2025 HM Revenue & Customs
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

package views

import base.SpecBase
import config.FrontendAppConfig
import models.SdltReturnTypes.IN_PROGRESS_RETURNS
import models.responses.UniversalStatus.SUBMITTED
import models.responses.{SdltInProgressReturnViewModel, SdltReturnViewRow}
import org.jsoup.Jsoup
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.twirl.api.Html
import views.html.InProgressReturnView

class InProgressReturnViewSpec extends SpecBase with GuiceOneAppPerSuite with MockitoSugar {

  trait Setup {

    implicit lazy val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
    implicit val appConfig: FrontendAppConfig = new FrontendAppConfig(app.configuration)

    implicit lazy val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
    implicit lazy val messages: Messages = MessagesImpl(Lang.defaultLang, messagesApi)

    def parseHtml(html: Html) = Jsoup.parse(html.toString)

    val view: InProgressReturnView = app.injector.instanceOf[InProgressReturnView]

    val paginatedData: List[SdltReturnViewRow] =
      (0 to 17).toList.map(i =>
        SdltReturnViewRow(
          address = s"$i Riverside Drive",
          utrn = s"UTRN-$i",
          purchaserName = s"Buyer-$i",
          status = SUBMITTED,
          agentReference = "Agent"
        )
      )

    val viewModel = SdltInProgressReturnViewModel(
      extractType = IN_PROGRESS_RETURNS,
      rows = paginatedData,
      totalRowCount = paginatedData.length,
      selectedPageIndex = 1
    )
  }

  "InProgressReturnView" - {
    "render the page with correct title and heading and caption" in new Setup {
      val html = view(viewModel, appConfig.startNewReturnUrl)
      val doc = parseHtml(html)

      val heading = doc.select("h1.govuk-heading-l")
      val caption = doc.select("div.govuk-grid-column-two-thirds span.govuk-caption-l")

      heading.size() mustBe 1
      caption.size() mustBe 1
      heading.text() mustBe messages("manageReturns.inProgressReturns.SomeReturns")
      caption.text() mustBe messages("manageReturns.inProgressReturns.span.manage")
      doc.title() must include(messages("manageReturns.inProgressReturns.title"))
    }

    "render the page with description and pagination info" in new Setup {
      val html = view(viewModel, appConfig.startNewReturnUrl)
      val doc = parseHtml(html)

      val paragraphs = doc.select("p.govuk-body")

      paragraphs.text() must include(messages("manageReturns.inProgressReturns.description"))
      paragraphs.text() must include ("Showing 1 to 10 of 18 records")
    }

    "render the page with each table header" in new Setup {
      val html = view(viewModel, appConfig.startNewReturnUrl)
      val doc = parseHtml(html)

      val headers = doc.select("th.govuk-table__header")

      headers.size() mustBe 4
      headers.text() must include(messages("manageReturns.inProgressReturns.summary.purchaser"))
      headers.text() must include(messages("manageReturns.inProgressReturns.summary.address"))
      headers.text() must include(messages("manageReturns.inProgressReturns.summary.agentsref"))
      headers.text() must include(messages("manageReturns.inProgressReturns.summary.status"))
    }

    "render the page with in-progress returns" in new Setup {
      val html = view(viewModel, appConfig.startNewReturnUrl)
      val doc = parseHtml(html)

      val returns = doc.select("td.govuk-table__cell")

      returns.text() must include ("Buyer-1")
      returns.text() must include ("Riverside Drive")
      returns.text() must include ("Agent")
    }
  }
}