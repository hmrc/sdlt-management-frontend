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
import org.jsoup.Jsoup
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import uk.gov.hmrc.govukfrontend.views.viewmodels.pagination.Pagination
import utils.PaginationHelper
import viewmodels.manage.deletedReturns._
import views.html.manage.DueForDeletionReturnsView
import play.twirl.api.Html

class DueForDeletionReturnsViewSpec
  extends SpecBase
    with GuiceOneAppPerSuite
    with MockitoSugar {

  trait Setup extends PaginationHelper {

    val inProgressRows: List[SdltDeletedInProgressReturnViewModel] =
      (1 to 3).toList.map { i =>
        SdltDeletedInProgressReturnViewModel(
          address = s"$i InProgress Street",
          purchaserName = s"InProgress Purchaser $i"
        )
      }

    val submittedRows: List[SdltDeletedSubmittedReturnsViewModel] =
      (1 to 3).toList.map { i =>
        SdltDeletedSubmittedReturnsViewModel(
          address = s"$i Submitted Street",
          utrn = s"UTRN$i",
          purchaserName = s"Submitted Purchaser $i"
        )
      }

    val emptyInProgress: PaginatedDeletedInProgressReturnsViewModel =
      PaginatedDeletedInProgressReturnsViewModel(Nil, None, None)

    val emptySubmitted: PaginatedDeletedSubmittedReturnsViewModel =
      PaginatedDeletedSubmittedReturnsViewModel(Nil, None, None)

    lazy val app: Application = new GuiceApplicationBuilder().build()

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
        PaginatedDeletedInProgressReturnsViewModel(inProgressRows, None, None)
      val submittedVm =
        PaginatedDeletedSubmittedReturnsViewModel(submittedRows, None, None)

      val html = view(inProgressVm, submittedVm)
      val doc  = parseHtml(html)

      doc.title() must include(messages("manageReturns.dueDeletionReturns.title"))

      val heading = doc.select("h1.govuk-heading-l")
      heading.size() mustBe 1
      heading.text() mustBe messages("manageReturns.dueDeletionReturns.heading")

      val caption = doc.select(".govuk-caption-l")
      caption.size() mustBe 1
      caption.text() must include(messages("manageReturns.dueDeletionReturns.caption"))
    }

    "show the 'no returns' message and link when both in-progress and submitted lists are empty" in new Setup {
      val html = view(emptyInProgress, emptySubmitted)
      val doc = parseHtml(html)

      doc.select("#updates-and-deadlines-tabs").size() mustBe 0

      doc.select("table.govuk-table").size() mustBe 0

      doc.select(".govuk-body").text() must include(
        messages("manageReturns.dueDeletionReturns.noReturns.info")
      )

      val link = doc.select("a.govuk-link")
      link.text() must include(messages("manage.submittedReturnsOverview.noReturns.link"))
    }

    "render only the in-progress tab and table when only in-progress data is present" in new Setup {
      val inProgressVm =
        PaginatedDeletedInProgressReturnsViewModel(inProgressRows, None, None)
      val submittedVm = emptySubmitted

      val html = view(inProgressVm, submittedVm)
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

      inProgressRowsEls.size() mustBe inProgressRows.size

      doc.select("#submitted").size() mustBe 0
    }

    "render only the submitted tab and table when only submitted data is present" in new Setup {
      val inProgressVm = emptyInProgress
      val submittedVm =
        PaginatedDeletedSubmittedReturnsViewModel(submittedRows, None, None)

      val html = view(inProgressVm, submittedVm)
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

      submittedRowsEls.size() mustBe submittedRows.size

      doc.select("#in-progress").size() mustBe 0
    }

    "render both in-progress and submitted tabs and tables when both have data" in new Setup {
      val inProgressVm =
        PaginatedDeletedInProgressReturnsViewModel(inProgressRows, None, None)
      val submittedVm =
        PaginatedDeletedSubmittedReturnsViewModel(submittedRows, None, None)

      val html = view(inProgressVm, submittedVm)
      val doc = parseHtml(html)

      doc.select("#updates-and-deadlines-tabs").size() mustBe 1

      val tabLabels = doc.select(".govuk-tabs__tab")
      tabLabels.size() mustBe 2

      tabLabels.get(0).text() mustBe messages("manageReturns.dueDeletionReturns.inProgressTab.heading")
      tabLabels.get(1).text() mustBe messages("manageReturns.dueDeletionReturns.submittedTab.heading")

      doc.select("#in-progress table.govuk-table").size() mustBe 1

      doc.select("#submitted table.govuk-table").size() mustBe 1
    }
  }
}
