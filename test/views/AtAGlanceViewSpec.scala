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
import models.manage.AtAGlanceViewModel
import models.responses.{SdltInProgressReturnViewModel, SdltReturnViewRow, SdltSubmittedReturnViewModel}
import org.jsoup.Jsoup
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.twirl.api.Html
import models.SdltReturnTypes.{IN_PROGRESS_RETURNS, SUBMITTED_SUBMITTED_RETURNS}
import models.responses.UniversalStatus.{ACCEPTED, SUBMITTED}
import views.html.manage.AtAGlanceView

class AtAGlanceViewSpec extends SpecBase with GuiceOneAppPerSuite with MockitoSugar {


  trait Setup {

    implicit lazy val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
    implicit val appConfig: FrontendAppConfig = new FrontendAppConfig(app.configuration)

    implicit lazy val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
    implicit lazy val messages: Messages = MessagesImpl(Lang.defaultLang, messagesApi)

    def parseHtml(html: Html) = Jsoup.parse(html.toString)

    val view: AtAGlanceView = app.injector.instanceOf[AtAGlanceView]

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

    val submittedPaginatedViewModel = SdltSubmittedReturnViewModel(
      extractType = SUBMITTED_SUBMITTED_RETURNS,
      rows = paginatedData.map(_.copy(status = ACCEPTED)),
      totalRowCount = paginatedData.length,
      selectedPageIndex = 1
    )

    val inProgressPaginatedViewModel = SdltInProgressReturnViewModel(
      extractType = IN_PROGRESS_RETURNS,
      rows = paginatedData,
      totalRowCount = paginatedData.length,
      selectedPageIndex = 1
    )

    val viewModel = AtAGlanceViewModel(
      storn = "STN001",
      inProgressReturns = inProgressPaginatedViewModel,
      submittedReturns = submittedPaginatedViewModel,
      dueForDeletionReturnsTotal = paginatedData.length,
      agentsCount = paginatedData.length
    )
  }

  "AtAGlanceView" - {
    "render the page with correct title and heading" in new Setup {
      val html = view(viewModel)
      val doc = parseHtml(html)

      val heading = doc.select("h1.govuk-heading-xl")

      heading.size() mustBe 1
      heading.text() mustBe messages("manage.homepage.heading")
      doc.title() must include(messages("manage.homepage.title"))
    }

    "render the page with correct storn number" in new Setup {
      val html = view(viewModel)
      val doc = parseHtml(html)

      val paragraph = doc.select("p.govuk-body")

      paragraph.size() mustBe 1
      paragraph.text() must include("STN001")
    }

    "render the page with the titles for each service" in new Setup {
      val html = view(viewModel)
      val doc = parseHtml(html)

      val titles = doc.select("h2.govuk-heading-m")

      titles.size() mustBe 4
      titles.text() must include(messages("manage.homepage.manageReturns.title"))
      titles.text() must include(messages("manage.homepage.manageAgents.title"))
      titles.text() must include(messages("manage.homepage.helpAndContact.title"))
      titles.text() must include(messages("manage.homepage.feedback.title"))
    }

    "render the page with title links to each service" in new Setup {
      val html = view(viewModel)
      val doc = parseHtml(html)

      val link = doc.select("li[class='govuk-!-margin-bottom-2'] a.govuk-link").text()

      link must include(messages("manage.homepage.manageReturns.returnsInProgress.withNum", 18))
      link must include(messages("manage.homepage.manageReturns.submittedReturns.withNum", 18))
      link must include(messages("manage.homepage.manageReturns.returnsDueForDeletion.withNum", 18))
      link must include(messages("manage.homepage.manageReturns.startNewReturn"))
      link must include(messages("manage.homepage.manageAgents.agentDetails.withNum", 18))
      link must include(messages("manage.homepage.manageAgents.addNewAgent"))
      link must include(messages("manage.homepage.helpAndContact.help"))
      link must include(messages("manage.homepage.helpAndContact.contactHMRC"))
      link must include(messages("manage.homepage.helpAndContact.usefulLinks"))
      link must include(messages("manage.homepage.feedback.leaveFeedback"))
    }
  }
}