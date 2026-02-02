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
import models.SdltReturnTypes.{IN_PROGRESS_RETURNS_DUE_FOR_DELETION, SUBMITTED_RETURNS_DUE_FOR_DELETION}
import models.responses.UniversalStatus.{ACCEPTED, SUBMITTED}
import models.responses.{SdltDueForDeletionReturnViewModel, SdltInProgressDueForDeletionReturnViewModel, SdltReturnViewRow, SdltSubmittedDueForDeletionReturnViewModel}
import org.jsoup.Jsoup
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.twirl.api.Html
import utils.PaginationHelper
import views.html.manage.DueForDeletionReturnsView

class DueForDeletionReturnsViewSpec
  extends SpecBase
    with GuiceOneAppPerSuite
    with MockitoSugar {

  trait Setup extends PaginationHelper {

    val nonPaginatedInProgressRows: List[SdltReturnViewRow] =
      (1 to 3).toList.map { i =>
        SdltReturnViewRow(
          address = s"$i InProgress Street",
          purchaserName = s"InProgress Purchaser $i",
          utrn = "",
          agentReference = "",
          status = ACCEPTED
        )
      }

    val nonPaginatedSubmittedRows: List[SdltReturnViewRow] =
      (1 to 3).toList.map { i =>
        SdltReturnViewRow(
          address = s"$i Submitted Street",
          utrn = s"UTRN$i",
          purchaserName = s"Submitted Purchaser $i",
          agentReference = "",
          status = SUBMITTED
        )
      }

    val paginatedInProgressRows: List[SdltReturnViewRow] =
      (1 to 23).toList.map { i =>
        SdltReturnViewRow(
          address = s"$i InProgress Street",
          purchaserName = s"InProgress Purchaser $i",
          utrn = "",
          agentReference = "",
          status = ACCEPTED
        )
      }

    val paginatedSubmittedRows: List[SdltReturnViewRow] =
      (1 to 23).toList.map { i =>
        SdltReturnViewRow(
          address = s"$i Submitted Street",
          utrn = s"UTRN$i",
          purchaserName = s"Submitted Purchaser $i",
          agentReference = "",
          status = SUBMITTED
        )
      }

    val emptyInProgress =
      SdltInProgressDueForDeletionReturnViewModel(extractType = IN_PROGRESS_RETURNS_DUE_FOR_DELETION, rows = List.empty, totalRowCount = 0)
    val emptySubmitted =
      SdltSubmittedDueForDeletionReturnViewModel(extractType = SUBMITTED_RETURNS_DUE_FOR_DELETION, rows = List.empty, totalRowCount = 0)

    lazy val app: Application = new GuiceApplicationBuilder().build()

    implicit val appConfig: FrontendAppConfig =
      app.injector.instanceOf[FrontendAppConfig]

    implicit lazy val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

    implicit lazy val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
    implicit lazy val messages: Messages = MessagesImpl(Lang.defaultLang, messagesApi)

    val view: DueForDeletionReturnsView = app.injector.instanceOf[DueForDeletionReturnsView]

    def parseHtml(html: Html) = Jsoup.parse(html.toString)

    val inProgressUrlSelector: Int => String =
      (pageIndex: Int) => s"/manage/due-for-deletion?inProgressPage=$pageIndex#in-progress-returns"

    val submittedUrlSelector: Int => String =
      (pageIndex: Int) => s"/manage/due-for-deletion?submittedPage=$pageIndex#submitted-returns"
  }

  "DueForDeletionReturnsView" - {

    "render the correct page title, heading and caption" in new Setup {
      val inProgressVm =
        SdltInProgressDueForDeletionReturnViewModel(
          extractType = IN_PROGRESS_RETURNS_DUE_FOR_DELETION,
          rows = nonPaginatedInProgressRows,
          1)
      val submittedVm =
        SdltSubmittedDueForDeletionReturnViewModel(
          extractType = SUBMITTED_RETURNS_DUE_FOR_DELETION,
          rows = nonPaginatedSubmittedRows,
          1)
      val viewModel = SdltDueForDeletionReturnViewModel(
        inProgressSelectedPageIndex = Some(1),
        submittedSelectedPageIndex = Some(1),
        inProgressViewModel = inProgressVm,
        submittedViewModel = submittedVm
      )

      val html = view(viewModel, appConfig.startNewReturnUrl)
      val doc  = parseHtml(html)

      doc.title() must include(messages("manageReturns.dueDeletionReturns.title"))

      val heading = doc.select("h1.govuk-heading-l")
      heading.size() mustBe 1
      heading.text() mustBe messages("manageReturns.dueDeletionReturns.heading")

      val caption = doc.select(".govuk-caption-l")
      caption.size() mustBe 1
      caption.text() must include(messages("manageReturns.dueDeletionReturns.caption"))
    }

    "render both empty in-progress and submitted lists" in new Setup {
      val viewModel = SdltDueForDeletionReturnViewModel(
        inProgressSelectedPageIndex = Some(1),
        submittedSelectedPageIndex = Some(1),
        inProgressViewModel = emptyInProgress,
        submittedViewModel = emptySubmitted
      )
      val html = view(viewModel, appConfig.startNewReturnUrl)
      val doc = parseHtml(html)

      doc.select("#updates-and-deadlines-tabs").size() mustBe 0

      doc.select("table.govuk-table").size() mustBe 0

      doc.select(".govuk-body").text() must include(
        messages("manageReturns.dueDeletionReturns.noReturns.info")
      )

      val link = doc.select("a.govuk-link")
      link.text() must include(messages("manage.submittedReturnsOverview.noReturns.link"))
    }

    "render both non paginated in-progress and submitted lists" in new Setup {
      val inProgressVm = SdltInProgressDueForDeletionReturnViewModel(
          extractType = IN_PROGRESS_RETURNS_DUE_FOR_DELETION,
          rows = nonPaginatedInProgressRows,
          1)
      val submittedVm = SdltSubmittedDueForDeletionReturnViewModel(
          extractType = SUBMITTED_RETURNS_DUE_FOR_DELETION,
          rows = nonPaginatedSubmittedRows,
          1)

      val viewModel = SdltDueForDeletionReturnViewModel(
        inProgressSelectedPageIndex = Some(1),
        submittedSelectedPageIndex = Some(1),
        inProgressViewModel = inProgressVm,
        submittedViewModel = submittedVm
      )

      val html = view(viewModel, appConfig.startNewReturnUrl)
      val doc = parseHtml(html)

      doc.select("#updates-and-deadlines-tabs").size() mustBe 1

      val tabLabels = doc.select(".govuk-tabs__tab")
      tabLabels.size() mustBe 2

      tabLabels.get(0).text() mustBe messages("manageReturns.dueDeletionReturns.inProgressTab.heading")
      tabLabels.get(1).text() mustBe messages("manageReturns.dueDeletionReturns.submittedTab.heading")

      doc.select("#in-progress table.govuk-table").size() mustBe 1

      doc.select("#submitted table.govuk-table").size() mustBe 1

      val paginationPages = doc.select("li.govuk-pagination__item")
      val paginationInfo = doc.select("p.govuk-body")

      paginationPages.size() mustBe 0
      paginationInfo.text() mustNot include("Showing 1 to 10 of 23 records")
    }

    "render both paginated in-progress and submitted lists" in new Setup {
      val inProgressVm = SdltInProgressDueForDeletionReturnViewModel(
        extractType = IN_PROGRESS_RETURNS_DUE_FOR_DELETION,
        rows = paginatedInProgressRows,
        totalRowCount = paginatedInProgressRows.length)

      val submittedVm = SdltSubmittedDueForDeletionReturnViewModel(
        extractType = SUBMITTED_RETURNS_DUE_FOR_DELETION,
        rows = paginatedSubmittedRows,
        totalRowCount = paginatedSubmittedRows.length)

      val viewModel = SdltDueForDeletionReturnViewModel(
        inProgressSelectedPageIndex = Some(1),
        submittedSelectedPageIndex = Some(1),
        inProgressViewModel = inProgressVm,
        submittedViewModel = submittedVm
      )

      val html = view(viewModel, appConfig.startNewReturnUrl)
      val doc = parseHtml(html)

      doc.select("#updates-and-deadlines-tabs").size() mustBe 1

      val tabLabels = doc.select(".govuk-tabs__tab")
      tabLabels.size() mustBe 2

      tabLabels.get(0).text() mustBe messages("manageReturns.dueDeletionReturns.inProgressTab.heading")
      tabLabels.get(1).text() mustBe messages("manageReturns.dueDeletionReturns.submittedTab.heading")

      doc.select("#in-progress table.govuk-table").size() mustBe 1

      doc.select("#submitted table.govuk-table").size() mustBe 1

      val paginationPages = doc.select("li.govuk-pagination__item")
      val paginationInfo = doc.select("p.govuk-body")

      paginationPages.size() mustBe 6
      paginationInfo.text().split("Showing 1 to 10 of 23 records").length - 1 mustBe 2

    }

    "render empty submitted returns and non paginated in-progress list" in new Setup {
      val inProgressVm =
        SdltInProgressDueForDeletionReturnViewModel(
          extractType = IN_PROGRESS_RETURNS_DUE_FOR_DELETION,
          rows = nonPaginatedInProgressRows,
          1)

      val viewModel = SdltDueForDeletionReturnViewModel(
        inProgressSelectedPageIndex = Some(1),
        submittedSelectedPageIndex = Some(1),
        inProgressViewModel = inProgressVm,
        submittedViewModel = emptySubmitted
      )

      val html = view(viewModel, appConfig.startNewReturnUrl)
      val doc = parseHtml(html)

      doc.select("#updates-and-deadlines-tabs").size() mustBe 1

      val tabLabels = doc.select(".govuk-tabs__tab")
      tabLabels.size() mustBe 1
      tabLabels.first().text() mustBe messages("manageReturns.dueDeletionReturns.inProgressTab.heading")

      val inProgressHeaders =
        doc.select("#in-progress")
          .select("thead.govuk-table__head tr.govuk-table__row th.govuk-table__header")

      inProgressHeaders.size() mustBe 2
      inProgressHeaders.get(0).text() mustBe messages("manageReturns.dueDeletionReturns.inProgressTab.purchaser")
      inProgressHeaders.get(1).text() mustBe messages("manageReturns.dueDeletionReturns.inProgressTab.address")

      val inProgressRowsEls =
        doc.select("#in-progress")
          .select("tbody.govuk-table__body tr.govuk-table__row")

      inProgressRowsEls.size() mustBe nonPaginatedInProgressRows.size

      doc.select("#submitted").size() mustBe 0
    }

    "render empty in-progress returns and non paginated submitted list" in new Setup {
      val submittedVm =
        SdltSubmittedDueForDeletionReturnViewModel(
          extractType = SUBMITTED_RETURNS_DUE_FOR_DELETION,
          rows = nonPaginatedSubmittedRows,
          1)

      val viewModel = SdltDueForDeletionReturnViewModel(
        inProgressSelectedPageIndex = Some(1),
        submittedSelectedPageIndex = Some(1),
        inProgressViewModel = emptyInProgress,
        submittedViewModel = submittedVm
      )

      val html = view(viewModel, appConfig.startNewReturnUrl)
      val doc = parseHtml(html)

      doc.select("#updates-and-deadlines-tabs").size() mustBe 1

      val tabLabels = doc.select(".govuk-tabs__tab")
      tabLabels.size() mustBe 1
      tabLabels.first().text() mustBe messages("manageReturns.dueDeletionReturns.submittedTab.heading")

      val submittedHeaders =
        doc.select("#submitted")
          .select("thead.govuk-table__head tr.govuk-table__row th.govuk-table__header")

      submittedHeaders.size() mustBe 3
      submittedHeaders.get(0).text() mustBe messages("manageReturns.dueDeletionReturns.submittedTab.purchaser")
      submittedHeaders.get(1).text() mustBe messages("manageReturns.dueDeletionReturns.submittedTab.address")
      submittedHeaders.get(2).text() mustBe messages("manageReturns.dueDeletionReturns.submittedTab.utrn")

      val submittedRowsEls =
        doc.select("#submitted")
          .select("tbody.govuk-table__body tr.govuk-table__row")

      submittedRowsEls.size() mustBe nonPaginatedSubmittedRows.size

      doc.select("#in-progress").size() mustBe 0
    }

    "render empty submitted returns and paginated in-progress list" in new Setup {
      val inProgressVm =
        SdltInProgressDueForDeletionReturnViewModel(
          extractType = IN_PROGRESS_RETURNS_DUE_FOR_DELETION,
          rows = paginatedInProgressRows,
          totalRowCount = paginatedInProgressRows.length)

      val viewModel = SdltDueForDeletionReturnViewModel(
        inProgressSelectedPageIndex = Some(1),
        submittedSelectedPageIndex = Some(1),
        inProgressViewModel = inProgressVm,
        submittedViewModel = emptySubmitted
      )

      val html = view(viewModel, appConfig.startNewReturnUrl)
      val doc = parseHtml(html)

      doc.select("#updates-and-deadlines-tabs").size() mustBe 1

      val tabLabels = doc.select(".govuk-tabs__tab")
      tabLabels.size() mustBe 1
      tabLabels.first().text() mustBe messages("manageReturns.dueDeletionReturns.inProgressTab.heading")

      val inProgressHeaders =
        doc.select("#in-progress")
          .select("thead.govuk-table__head tr.govuk-table__row th.govuk-table__header")

      inProgressHeaders.size() mustBe 2
      inProgressHeaders.get(0).text() mustBe messages("manageReturns.dueDeletionReturns.inProgressTab.purchaser")
      inProgressHeaders.get(1).text() mustBe messages("manageReturns.dueDeletionReturns.inProgressTab.address")

      val inProgressRowsEls =
        doc.select("#in-progress")
          .select("tbody.govuk-table__body tr.govuk-table__row")

      inProgressRowsEls.size() mustBe paginatedInProgressRows.size

      doc.select("#submitted").size() mustBe 0

      val paginationPages = doc.select("li.govuk-pagination__item")
      val paginationInfo = doc.select("p.govuk-body")

      paginationPages.size() mustBe 3
      paginationInfo.text() must include("Showing 1 to 10 of 23 records")
    }

    "render empty in-progress returns and paginated submitted list" in new Setup {
      val submittedVm =
        SdltSubmittedDueForDeletionReturnViewModel(
          extractType = SUBMITTED_RETURNS_DUE_FOR_DELETION,
          rows = paginatedSubmittedRows,
          totalRowCount = paginatedSubmittedRows.length)

      val viewModel = SdltDueForDeletionReturnViewModel(
        inProgressSelectedPageIndex = Some(1),
        submittedSelectedPageIndex = Some(1),
        inProgressViewModel = emptyInProgress,
        submittedViewModel = submittedVm
      )

      val html = view(viewModel, appConfig.startNewReturnUrl)
      val doc = parseHtml(html)

      doc.select("#updates-and-deadlines-tabs").size() mustBe 1

      val tabLabels = doc.select(".govuk-tabs__tab")
      tabLabels.size() mustBe 1
      tabLabels.first().text() mustBe messages("manageReturns.dueDeletionReturns.submittedTab.heading")

      val submittedHeaders =
        doc.select("#submitted")
          .select("thead.govuk-table__head tr.govuk-table__row th.govuk-table__header")

      submittedHeaders.size() mustBe 3
      submittedHeaders.get(0).text() mustBe messages("manageReturns.dueDeletionReturns.submittedTab.purchaser")
      submittedHeaders.get(1).text() mustBe messages("manageReturns.dueDeletionReturns.submittedTab.address")
      submittedHeaders.get(2).text() mustBe messages("manageReturns.dueDeletionReturns.submittedTab.utrn")

      val submittedRowsEls =
        doc.select("#submitted")
          .select("tbody.govuk-table__body tr.govuk-table__row")

      submittedRowsEls.size() mustBe paginatedSubmittedRows.size

      doc.select("#in-progress").size() mustBe 0

      val paginationPages = doc.select("li.govuk-pagination__item")
      val paginationInfo = doc.select("p.govuk-body")

      paginationPages.size() mustBe 3
      paginationInfo.text() must include("Showing 1 to 10 of 23 records")
    }

    "render non paginated in-progress returns and paginated submitted list" in new Setup {
      val inProgressVm =
        SdltInProgressDueForDeletionReturnViewModel(
          extractType = IN_PROGRESS_RETURNS_DUE_FOR_DELETION,
          rows = nonPaginatedInProgressRows,
          1)

      val submittedVm =
        SdltSubmittedDueForDeletionReturnViewModel(
          extractType = SUBMITTED_RETURNS_DUE_FOR_DELETION,
          rows = paginatedSubmittedRows,
          totalRowCount = paginatedSubmittedRows.length)

      val viewModel = SdltDueForDeletionReturnViewModel(
        inProgressSelectedPageIndex = Some(1),
        submittedSelectedPageIndex = Some(1),
        inProgressViewModel = inProgressVm,
        submittedViewModel = submittedVm
      )

      val html = view(viewModel, appConfig.startNewReturnUrl)
      val doc = parseHtml(html)

      doc.select("#updates-and-deadlines-tabs").size() mustBe 1

      val tabLabels = doc.select(".govuk-tabs__tab")
      tabLabels.size() mustBe 2

      tabLabels.get(0).text() mustBe messages("manageReturns.dueDeletionReturns.inProgressTab.heading")
      tabLabels.get(1).text() mustBe messages("manageReturns.dueDeletionReturns.submittedTab.heading")

      doc.select("#in-progress table.govuk-table").size() mustBe 1

      doc.select("#submitted table.govuk-table").size() mustBe 1

      val paginationPages = doc.select("li.govuk-pagination__item")
      val paginationLabel = doc.select("li.govuk-pagination__item a.govuk-link").get(1)
      val paginationInfo = doc.select("p.govuk-body")

      paginationPages.size() mustBe 3
      paginationLabel.attr("href") must include("submittedIndex=2")
      paginationLabel.attr("href") mustNot include("inProgressIndex=2")

      paginationInfo.text() must include("Showing 1 to 10 of 23 records")
    }

    "render non paginated submitted returns and paginated in-progress list" in new Setup {
      val inProgressVm =
        SdltInProgressDueForDeletionReturnViewModel(
          extractType = IN_PROGRESS_RETURNS_DUE_FOR_DELETION,
          rows = paginatedInProgressRows,
          totalRowCount = paginatedInProgressRows.length)

      val submittedVm =
        SdltSubmittedDueForDeletionReturnViewModel(
          extractType = SUBMITTED_RETURNS_DUE_FOR_DELETION,
          rows = paginatedSubmittedRows,
          1)

      val viewModel = SdltDueForDeletionReturnViewModel(
        inProgressSelectedPageIndex = Some(1),
        submittedSelectedPageIndex = Some(1),
        inProgressViewModel = inProgressVm,
        submittedViewModel = submittedVm
      )

      val html = view(viewModel, appConfig.startNewReturnUrl)
      val doc = parseHtml(html)

      doc.select("#updates-and-deadlines-tabs").size() mustBe 1

      val tabLabels = doc.select(".govuk-tabs__tab")
      tabLabels.size() mustBe 2

      tabLabels.get(0).text() mustBe messages("manageReturns.dueDeletionReturns.inProgressTab.heading")
      tabLabels.get(1).text() mustBe messages("manageReturns.dueDeletionReturns.submittedTab.heading")

      doc.select("#in-progress table.govuk-table").size() mustBe 1

      doc.select("#submitted table.govuk-table").size() mustBe 1

      val paginationPages = doc.select("li.govuk-pagination__item")
      val paginationLabel = doc.select("li.govuk-pagination__item a.govuk-link").get(1)
      val paginationInfo = doc.select("p.govuk-body")

      paginationPages.size() mustBe 3
      paginationLabel.attr("href") must include("inProgressIndex=2")
      paginationLabel.attr("href") mustNot include("submittedIndex=2")

      paginationInfo.text() must include("Showing 1 to 10 of 23 records")
      
    }
  }
}
