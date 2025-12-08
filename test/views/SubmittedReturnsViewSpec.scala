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

import models.responses.UniversalStatus.{SUBMITTED, SUBMITTED_NO_RECEIPT}
import models.responses.{SdltReturnViewRow, SdltSubmittedReturnViewModel}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.twirl.api.Html
import utils.PaginationHelper
import views.html.manage.SubmittedReturnsView
import base.SpecBase
import models.SdltReturnTypes.SUBMITTED_SUBMITTED_RETURNS
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import uk.gov.hmrc.govukfrontend.views.Aliases.Pagination

class SubmittedReturnsViewSpec
  extends SpecBase
    with GuiceOneAppPerSuite
    with MockitoSugar {

  lazy val submittedRoute: String =
    controllers.manage.routes.SubmittedReturnsController.onPageLoad(None).url

  trait Setup extends PaginationHelper {

    val emptyData: List[SdltReturnViewRow] = Nil
    val emptyViewModel = SdltSubmittedReturnViewModel(
      extractType = SUBMITTED_SUBMITTED_RETURNS,
      rows = emptyData,
      totalRowCount = 0
    )

    val paginatedData: List[SdltReturnViewRow] =
      (0 to 17).toList.map(i =>
        SdltReturnViewRow(
          address        = s"$i Riverside Drive",
          utrn           = s"UTRN-$i",
          purchaserName  = s"Buyer-$i",
          status         = SUBMITTED,
          agentReference = "Agent"
        )
      )

    val paginatedViewModel = SdltSubmittedReturnViewModel(
        extractType = SUBMITTED_SUBMITTED_RETURNS,
        rows = paginatedData,
        totalRowCount = paginatedData.length
      )

    val nonPaginatedData: List[SdltReturnViewRow] =
      (0 to 7).toList.map(i =>
        SdltReturnViewRow(
          address        = s"$i Riverside Drive",
          utrn           = s"UTRN-$i",
          purchaserName  = s"Buyer-$i",
          status         = SUBMITTED_NO_RECEIPT,
          agentReference = "Agent"
        )
      )

    val nonPaginatedViewModel = SdltSubmittedReturnViewModel(
      extractType = SUBMITTED_SUBMITTED_RETURNS,
      rows = nonPaginatedData,
      totalRowCount = nonPaginatedData.length
    )

    lazy val app: Application = new GuiceApplicationBuilder().build()

    implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
    implicit val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
    implicit val messages: Messages = MessagesImpl(Lang.defaultLang, messagesApi)

    val view: SubmittedReturnsView = app.injector.instanceOf[SubmittedReturnsView]

    val urlSelector: Int => String =
      page => controllers.manage.routes.SubmittedReturnsController.onPageLoad(Some(page)).url

    def htmlDoc(html: Html): Document = Jsoup.parse(html.toString)
  }

  "SubmittedReturnsView" - {

    "must render headers correctly" in new Setup {
      val pageIndex: Int = 1
      val totalRowCount: Int = paginatedData.length
      val totalPages: Int = getPageCount(totalRowCount)

      val paginator: Option[Pagination] =
        Option.when(totalRowCount > 0 && totalPages > 1)(
          Pagination(
            items = Some(
              paginationItems(
                currentPage = pageIndex,
                totalPages  = totalPages,
                urlSelector = urlSelector
              )
            ),
            previous      = generatePreviousLink(pageIndex, totalPages, urlSelector(pageIndex - 1)),
            next          = generateNextLink(pageIndex, totalPages, urlSelector(pageIndex + 1)),
            landmarkLabel = None,
            classes       = "",
            attributes    = Map.empty
          )
        )

      val paginationText: Option[String] =
        Option.unless(totalRowCount <= 10 || pageIndex <= 0) {
          val total = totalRowCount
          val start = (pageIndex - 1) * 10 + 1
          val end   = math.min(pageIndex * 10, total)
          messages("manageReturns.inProgressReturns.paginationInfo", start, end, total)
        }

      val html: Html = view(paginatedViewModel, paginator, paginationText)
      val doc: Document = htmlDoc(html)

      val headers: Elements =
        doc.select("thead.govuk-table__head tr.govuk-table__row th.govuk-table__header")

      headers.size() mustBe 3
      headers.get(0).text() mustBe messages("manage.submittedReturnsOverview.summary.purchaser")
      headers.get(1).text() mustBe messages("manage.submittedReturnsOverview.summary.address")
      headers.get(2).text() mustBe messages("manage.submittedReturnsOverview.summary.utrn")
    }

    "must render pagination when needed" in new Setup {
      val pageIndex: Int      = 1
      val totalRowCount: Int  = paginatedData.length
      val totalPages: Int = getPageCount(totalRowCount)

      val paginator: Option[Pagination] =
        Option.when(totalRowCount > 0 && totalPages > 1)(
          Pagination(
            items = Some(
              paginationItems(
                currentPage = pageIndex,
                totalPages  = totalPages,
                urlSelector = urlSelector
              )
            ),
            previous      = generatePreviousLink(pageIndex, totalPages, urlSelector(pageIndex - 1)),
            next          = generateNextLink(pageIndex, totalPages, urlSelector(pageIndex + 1)),
            landmarkLabel = None,
            classes       = "",
            attributes    = Map.empty
          )
        )

      val paginationText: Option[String] =
        Option.unless(totalRowCount <= 10 || pageIndex <= 0) {
          val total = totalRowCount
          val start = (pageIndex - 1) * 10 + 1
          val end   = math.min(pageIndex * 10, total)
          messages("manageReturns.inProgressReturns.paginationInfo", start, end, total)
        }

      val html: Html = view(paginatedViewModel, paginator, paginationText)
      val doc: Document = htmlDoc(html)

      doc.select(".govuk-body").text() must include(messages("manage.submittedReturnsOverview.nonZeroReturns.info"))

      paginator must not be empty
      doc.select(".govuk-pagination").size() mustBe 1
    }

    "must not render pagination when not needed" in new Setup {
      val pageIndex: Int      = 1
      val totalRowCount: Int = nonPaginatedData.length
      val totalPages: Int = getPageCount(totalRowCount)

      val paginator: Option[Pagination] =
        Option.when(totalRowCount > 0 && totalPages > 1)(
          Pagination(
            items = Some(
              paginationItems(
                currentPage = pageIndex,
                totalPages  = totalPages,
                urlSelector = urlSelector
              )
            ),
            previous      = generatePreviousLink(pageIndex, totalPages, urlSelector(pageIndex - 1)),
            next          = generateNextLink(pageIndex, totalPages, urlSelector(pageIndex + 1)),
            landmarkLabel = None,
            classes       = "",
            attributes    = Map.empty
          )
        )

      val paginationText: Option[String] =
        Option.unless(totalRowCount <= 10 || pageIndex <= 0) {
          val total = totalRowCount
          val start = (pageIndex - 1) * 10 + 1
          val end   = math.min(pageIndex * 10, total)
          messages("manageReturns.inProgressReturns.paginationInfo", start, end, total)
        }

      val html: Html = view(nonPaginatedViewModel, paginator, paginationText)
      val doc: Document = htmlDoc(html)

      doc.select(".govuk-body").text() must include(messages("manage.submittedReturnsOverview.nonZeroReturns.info"))
      paginator mustBe None
      doc.select(".govuk-pagination").size() mustBe 0
    }

    "must render the 'no returns' message when rows are empty" in new Setup {
      val pageIndex: Int      = 1
      val totalRowCount: Int = emptyData.length
      val totalPages: Int = getPageCount(totalRowCount)

      val paginator: Option[Pagination] =
        Option.when(totalRowCount > 0 && totalPages > 1)(
          Pagination(
            items = Some(
              paginationItems(
                currentPage = pageIndex,
                totalPages  = totalPages,
                urlSelector = urlSelector
              )
            ),
            previous      = generatePreviousLink(pageIndex, totalPages, urlSelector(pageIndex - 1)),
            next          = generateNextLink(pageIndex, totalPages, urlSelector(pageIndex + 1)),
            landmarkLabel = None,
            classes       = "",
            attributes    = Map.empty
          )
        )

      val paginationText: Option[String] =
        Option.unless(totalRowCount <= 10 || pageIndex <= 0) {
          val total = totalRowCount
          val start = (pageIndex - 1) * 10 + 1
          val end   = math.min(pageIndex * 10, total)
          messages("manageReturns.inProgressReturns.paginationInfo", start, end, total)
        }

      val html: Html = view(emptyViewModel, paginator, paginationText)
      val doc: Document = htmlDoc(html)

      doc.select(".govuk-body").text() must include(messages("manage.submittedReturnsOverview.noReturns.info"))
      paginator mustBe None
      doc.select(".govuk-pagination").size() mustBe 0
    }
  }
}
