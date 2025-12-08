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
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.{Application, inject}
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import play.api.test.FakeRequest
import uk.gov.hmrc.govukfrontend.views.Aliases.Pagination
import views.html.manage.SubmittedReturnsView
import utils.PaginationHelper
import base.SpecBase
import models.responses.{SdltReturnViewRow}
import org.jsoup.Jsoup
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.AnyContentAsEmpty
import play.twirl.api.Html

class SubmittedReturnsViewSpec extends SpecBase with GuiceOneAppPerSuite with MockitoSugar {

  lazy val SubmittedControllerRoute = controllers.manage.routes.SubmittedReturnsController.onPageLoad(None).url

  trait Setup extends PaginationHelper {

    val expectedEmptyData: List[SdltReturnViewRow] = List[SdltReturnViewRow]()

    val expectedDataPagination: List[SdltReturnViewRow] =
      (0 to 17).toList.map(index =>
        SdltReturnViewRow(
          address = s"$index Riverside Drive",
          utrn = "UTRN003",
          purchaserName = "Brown",
          status = SUBMITTED,
          agentReference = ""
        )
      )

    val expectedDataNoPagination: List[SdltReturnViewRow] =
      (0 to 7).toList.map(index =>
        SdltReturnViewRow(
          address = s"$index Riverside Drive",
          utrn = "UTRN003",
          purchaserName = "Brown",
          status = SUBMITTED_NO_RECEIPT,
          agentReference = ""
        )
      )

    lazy val app: Application = new GuiceApplicationBuilder().build()

    implicit lazy val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

    implicit lazy val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
    implicit lazy val messages: Messages = MessagesImpl(Lang.defaultLang, messagesApi)

    def parseHtml(html: Html) = Jsoup.parse(html.toString)

    val view: SubmittedReturnsView = app.injector.instanceOf[SubmittedReturnsView]

    val urlSelector: Int => String = (selectedPageIndex: Int) => controllers.manage.routes.SubmittedReturnsController.onPageLoad(Some(selectedPageIndex)).url

  }

  "SubmittedReturnsView" - {
    "render the page with correct table headings" in new Setup {

      val selectedPageIndex: Int = 1
      val expectedNumberOfPages: Int = 2
      val paginator: Option[Pagination] = createPagination(selectedPageIndex, expectedDataPagination.length, urlSelector)
      val paginationText: Option[String] = getPaginationInfoText(selectedPageIndex, expectedDataPagination)

      val html = view(expectedDataPagination, paginator, paginationText)
      val doc = parseHtml(html)

      val headers = doc.select("thead.govuk-table__head tr.govuk-table__row th.govuk-table__header")

      headers.size() mustBe 3
      headers.get(0).text() mustBe messages("manage.submittedReturnsOverview.summary.purchaser")
      headers.get(1).text() mustBe messages("manage.submittedReturnsOverview.summary.address")
      headers.get(2).text() mustBe messages("manage.submittedReturnsOverview.summary.utrn")
    }

    "render the page with pagination" in new Setup {

      val selectedPageIndex: Int = 1
      val expectedNumberOfPages: Int = 2
      val paginator: Option[Pagination] = createPagination(selectedPageIndex, expectedDataPagination.length, urlSelector)
      val paginationText: Option[String] = getPaginationInfoText(selectedPageIndex, expectedDataPagination)

      val html = view(expectedDataPagination, paginator, paginationText)
      val doc = parseHtml(html)

      doc.select(".govuk-body").text() must include(messages("manage.submittedReturnsOverview.nonZeroReturns.info"))
      doc.select(".govuk-pagination").size mustBe 1
      doc.select(".govuk-pagination__item").size mustBe expectedNumberOfPages
    }

    "render without pagination" in new Setup {
      val selectedPageIndex: Int = 1
      val expectedNumberOfPages: Int = 0
      val paginator: Option[Pagination] = createPagination(selectedPageIndex, expectedDataNoPagination.length, urlSelector)
      val paginationText: Option[String] = getPaginationInfoText(selectedPageIndex, expectedDataNoPagination)

      val html = view(expectedDataNoPagination, paginator, paginationText)
      val doc = parseHtml(html)

      doc.select(".govuk-body").text() must include(messages("manage.submittedReturnsOverview.nonZeroReturns.info"))
      doc.select(".govuk-pagination").size mustBe 0
      doc.select(".govuk-pagination__item").size mustBe expectedNumberOfPages
    }

    "render no table when there are no submitted returns" in new Setup {
      val selectedPageIndex: Int = 1
      val expectedNumberOfPages: Int = 0
      val paginator: Option[Pagination] = createPagination(selectedPageIndex, expectedEmptyData.length, urlSelector)
      val paginationText: Option[String] = getPaginationInfoText(selectedPageIndex, expectedEmptyData)

      val html = view(expectedEmptyData, paginator, paginationText)
      val doc = parseHtml(html)

      doc.select(".govuk-body").text() must include(messages("manage.submittedReturnsOverview.noReturns.info"))
      doc.select(".govuk-pagination").size mustBe 0
      doc.select(".govuk-pagination__item").size mustBe expectedNumberOfPages
    }
  }
}
