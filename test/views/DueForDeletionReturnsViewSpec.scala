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
import generators.ModelGenerators
import models.responses.PaginatedInProgressReturnsViewModel
import org.jsoup.Jsoup
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.{Application, inject}
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.Aliases.Pagination
import utils.PaginationHelper
import viewmodels.manage.PaginatedSubmittedReturnsViewModel
import views.html.manage.DueForDeletionReturnsView


class DueForDeletionReturnsViewSpec extends SpecBase with GuiceOneAppPerSuite with MockitoSugar with ModelGenerators {

  lazy val DueForDeletionReturnsControllerRoute = controllers.manage.routes.DueForDeletionReturnsController.onPageLoad(Some(1), Some(1)).url

  trait Setup extends PaginationHelper {

    lazy val app: Application = new GuiceApplicationBuilder().build()

    implicit lazy val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

    implicit lazy val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
    implicit lazy val messages: Messages = MessagesImpl(Lang.defaultLang, messagesApi)

    def parseHtml(html: Html) = Jsoup.parse(html.toString)

    val view: DueForDeletionReturnsView = app.injector.instanceOf[DueForDeletionReturnsView]

    val inProgressUrlSelector: Int => String = (inProgressIndex: Int) =>
      s"${
        controllers.manage.routes.DueForDeletionReturnsController.onPageLoad(
          Some(inProgressIndex),
          Some(1)
        ).url
      }#in-progress-returns"

    val submittedUrlSelector: Int => String = (submittedIndex: Int) =>
      s"${
        controllers.manage.routes.DueForDeletionReturnsController.onPageLoad(
          Some(1),
          Some(submittedIndex)
        ).url
      }#submitted-returns"

  }

  "DueForDeletionReturnsView" - {
    "render the page with correct table headings for each tab" in new Setup {

      val selectedPageIndex: Int = 1
      val paginator: Option[Pagination] = createPagination(selectedPageIndex, dataPaginationInProgressHalfPagination.length, inProgressUrlSelector)
      val paginationText: Option[String] = getPaginationInfoText(selectedPageIndex, dataPaginationInProgressHalfPagination)

      val paginatedInProgressModel = PaginatedInProgressReturnsViewModel(dataPaginationInProgressHalfPagination, paginator, paginationText)
      val submittedModel = PaginatedSubmittedReturnsViewModel(dataNoPaginationSubmittedHalfPagination, None, None)

      val html = view(paginatedInProgressModel, submittedModel)
      val doc = parseHtml(html)

      val headers = doc.select("thead.govuk-table__head tr.govuk-table__row th.govuk-table__header")

      headers.size() mustBe 5
      headers.get(0).text() mustBe messages("manageReturns.dueDeletionReturns.inProgressTab.purchaser")
      headers.get(1).text() mustBe messages("manageReturns.dueDeletionReturns.inProgressTab.address")

      headers.get(2).text() mustBe messages("manageReturns.dueDeletionReturns.submittedTab.purchaser")
      headers.get(3).text() mustBe messages("manageReturns.dueDeletionReturns.submittedTab.address")
      headers.get(4).text() mustBe messages("manageReturns.dueDeletionReturns.submittedTab.utrn")
    }

    "render the page with both tabs paginated" in new Setup {

      val selectedPageIndex: Int = 1
      val expectedNumberOfPages: Int = 4

      val inProgressPaginator: Option[Pagination] = createPagination(selectedPageIndex, dataInProgressPaginationFullPagination.length, inProgressUrlSelector)
      val inProgressPaginationText: Option[String] = getPaginationInfoText(selectedPageIndex, dataInProgressPaginationFullPagination)

      val submittedPaginator: Option[Pagination] = createPagination(selectedPageIndex, dataSubmittedPaginationFullPagination.length, submittedUrlSelector)
      val submittedPaginationText: Option[String] = getPaginationInfoText(selectedPageIndex, dataSubmittedPaginationFullPagination)

      val paginatedInProgressModel = PaginatedInProgressReturnsViewModel(dataInProgressPaginationFullPagination, inProgressPaginator, inProgressPaginationText)
      val paginatedSubmittedModel = PaginatedSubmittedReturnsViewModel(dataSubmittedPaginationFullPagination, submittedPaginator, submittedPaginationText)

      val html = view(paginatedInProgressModel, paginatedSubmittedModel)
      val doc = parseHtml(html)


      doc.select(".govuk-body").text() must include(messages("manageReturns.dueDeletionReturns.nonZeroReturns.info"))
      doc.select(".govuk-pagination").size mustBe 2
      doc.select(".govuk-pagination__item").size mustBe expectedNumberOfPages
    }

    "render the page with one tab paginated" in new Setup {

      val selectedPageIndex: Int = 1
      val expectedNumberOfPages: Int = 2

      val inProgressPaginator: Option[Pagination] = createPagination(selectedPageIndex, dataPaginationInProgressHalfPagination.length, inProgressUrlSelector)
      val inProgressPaginationText: Option[String] = getPaginationInfoText(selectedPageIndex, dataNoPaginationSubmittedHalfPagination)

      val paginatedInProgressModel = PaginatedInProgressReturnsViewModel(dataInProgressPaginationFullPagination, inProgressPaginator, inProgressPaginationText)
      val submittedModel = PaginatedSubmittedReturnsViewModel(dataSubmittedPaginationFullPagination, None, None)

      val html = view(paginatedInProgressModel, submittedModel)
      val doc = parseHtml(html)


      doc.select(".govuk-body").text() must include(messages("manageReturns.dueDeletionReturns.nonZeroReturns.info"))
      doc.select(".govuk-pagination").size mustBe 1
      doc.select(".govuk-pagination__item").size mustBe expectedNumberOfPages
    }

    "render without pagination" in new Setup {
      val selectedPageIndex: Int = 1
      val expectedNumberOfPages: Int = 0

      val inProgressModel = PaginatedInProgressReturnsViewModel(dataNoPaginationInProgressEmptyPagination, None, None)
      val submittedModel = PaginatedSubmittedReturnsViewModel(dataNoPaginationSubmittedEmptyPagination, None, None)

      val html = view(inProgressModel, submittedModel)
      val doc = parseHtml(html)

      doc.select(".govuk-body").text() must include(messages("manageReturns.dueDeletionReturns.nonZeroReturns.info"))
      doc.select(".govuk-pagination").size mustBe 0
      doc.select(".govuk-pagination__item").size mustBe expectedNumberOfPages
    }

    "render no tabs when there are no returns due for deletion" in new Setup {
      val selectedPageIndex: Int = 1
      val expectedNumberOfPages: Int = 0

      val emptyInProgressModel = PaginatedInProgressReturnsViewModel(List.empty, None, None)
      val emptySubmittedModel = PaginatedSubmittedReturnsViewModel(List.empty, None, None)

      val html = view(emptyInProgressModel, emptySubmittedModel)
      val doc = parseHtml(html)

      doc.select(".govuk-body").text() must include(messages("manageReturns.dueDeletionReturns.noReturns.info"))
      doc.select(".govuk-tabs").size() mustBe 0
      doc.select(".govuk-pagination__item").size mustBe expectedNumberOfPages
    }
  }
}
