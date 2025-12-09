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

package controllers.manage

import base.SpecBase
import config.FrontendAppConfig
import models.SdltReturnTypes.SUBMITTED_SUBMITTED_RETURNS
import models.responses.{SdltReturnViewRow, SdltSubmittedReturnViewModel, UniversalStatus}
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import play.api.Application
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.StampDutyLandTaxService
import uk.gov.hmrc.govukfrontend.views.Aliases.Pagination
import uk.gov.hmrc.http.HeaderCarrier
import utils.PaginationHelper
import views.html.manage.SubmittedReturnsView

import scala.concurrent.Future

class SubmittedReturnsControllerSpec extends SpecBase with MockitoSugar {

  trait Fixture extends PaginationHelper {
    val rowsPerPage: Int = 10

    val mockService: StampDutyLandTaxService = mock[StampDutyLandTaxService]

    val application: Application =
      applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[StampDutyLandTaxService].toInstance(mockService))
        .build()

    implicit val appConfig: FrontendAppConfig =
      application.injector.instanceOf[FrontendAppConfig]

    val emptyRows: List[SdltReturnViewRow] = Nil

    val nonPaginatedRows: List[SdltReturnViewRow] =
      (0 to 7).toList.map { index =>
        SdltReturnViewRow(
          address = s"$index Riverside Drive",
          agentReference = "ARN001",
          utrn = "UTRN003",
          purchaserName = "Brown",
          status = UniversalStatus.SUBMITTED
        )
      }

    val allPaginatedRows: List[SdltReturnViewRow] =
      (0 to 17).toList.map { index =>
        SdltReturnViewRow(
          address = s"$index Riverside Drive",
          agentReference = "ARN001",
          utrn = "UTRN003",
          purchaserName = "Brown",
          status = UniversalStatus.SUBMITTED_NO_RECEIPT
        )
      }

    val urlSelector: Int => String =
      page => controllers.manage.routes.SubmittedReturnsController.onPageLoad(Some(page)).url

    lazy val submittedControllerRoute: String =
      controllers.manage.routes.SubmittedReturnsController.onPageLoad(None).url
  }

  "SubmittedReturnsController" - {

    "must return OK and the correct view for a GET request with no returns" in new Fixture {

      val viewModel = SdltSubmittedReturnViewModel(
        extractType = SUBMITTED_SUBMITTED_RETURNS,
        rows = emptyRows,
        totalRowCount = 0,
        selectedPageIndex = 1
      )

      when(
        mockService.getReturnsByTypeViewModel(
          any(),
          eqTo(SUBMITTED_SUBMITTED_RETURNS),
          any()
        )(any[HeaderCarrier])
      ).thenReturn(Future.successful(viewModel))

      running(application) {
        val request = FakeRequest(GET, submittedControllerRoute)
        val result = route(application, request).value
        val view = application.injector.instanceOf[SubmittedReturnsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(viewModel, appConfig.startNewReturnUrl)(request, messages(application)).toString

        verify(mockService, times(1))
          .getReturnsByTypeViewModel(any(), eqTo(SUBMITTED_SUBMITTED_RETURNS), any())(any[HeaderCarrier])
      }
    }

    "must return OK and the correct view for a GET request with no pagination required" in new Fixture {

      val viewModel = SdltSubmittedReturnViewModel(
        extractType = SUBMITTED_SUBMITTED_RETURNS,
        rows = nonPaginatedRows,
        totalRowCount = nonPaginatedRows.length,
        selectedPageIndex = 1
      )

      when(
        mockService.getReturnsByTypeViewModel(
          any(),
          eqTo(SUBMITTED_SUBMITTED_RETURNS),
          any()
        )(any[HeaderCarrier])
      ).thenReturn(Future.successful(viewModel))

      running(application) {
        val request = FakeRequest(GET, submittedControllerRoute)
        val result = route(application, request).value
        val view = application.injector.instanceOf[SubmittedReturnsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(viewModel, appConfig.startNewReturnUrl)(request, messages(application)).toString

        verify(mockService, times(1))
          .getReturnsByTypeViewModel(any(), eqTo(SUBMITTED_SUBMITTED_RETURNS), any())(any[HeaderCarrier])
      }
    }

    "must return OK and the correct view for a GET request with pagination (page 1)" in new Fixture {
      val selectedPageIndex = 1
      val totalRowCount = allPaginatedRows.length
      val pageOneRows = allPaginatedRows.take(rowsPerPage)

      val viewModel = SdltSubmittedReturnViewModel(
        extractType = SUBMITTED_SUBMITTED_RETURNS,
        rows = pageOneRows,
        totalRowCount = totalRowCount,
        selectedPageIndex = 1
      )

      when(
        mockService.getReturnsByTypeViewModel(
          any(),
          eqTo(SUBMITTED_SUBMITTED_RETURNS),
          any()
        )(any[HeaderCarrier])
      ).thenReturn(Future.successful(viewModel))

      running(application) {
        implicit val messagesApi = messages(application)

        val totalPages = getPageCount(totalRowCount)

        val request = FakeRequest(GET, submittedControllerRoute)
        val result = route(application, request).value
        val view = application.injector.instanceOf[SubmittedReturnsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(viewModel, appConfig.startNewReturnUrl)(request, messages(application)).toString

        verify(mockService, times(1))
          .getReturnsByTypeViewModel(any(), eqTo(SUBMITTED_SUBMITTED_RETURNS), any())(any[HeaderCarrier])
      }
    }

    "must return OK and the correct view for a GET request with pagination (page 2)" in new Fixture {
      val selectedPageIndex = 2
      val totalRowCount = allPaginatedRows.length
      val pageTwoRows = allPaginatedRows.drop(rowsPerPage)


      val viewModel = SdltSubmittedReturnViewModel(
        extractType = SUBMITTED_SUBMITTED_RETURNS,
        rows = pageTwoRows,
        totalRowCount = totalRowCount,
        selectedPageIndex = 2
      )

      when(
        mockService.getReturnsByTypeViewModel(
          any(),
          eqTo(SUBMITTED_SUBMITTED_RETURNS),
          any()
        )(any[HeaderCarrier])
      ).thenReturn(Future.successful(viewModel))

      running(application) {
        implicit val messagesApi = messages(application)

        val request = FakeRequest(
          GET,
          submittedControllerRoute + s"?paginationIndex=$selectedPageIndex"
        )

        val result = route(application, request).value
        val view = application.injector.instanceOf[SubmittedReturnsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(viewModel, appConfig.startNewReturnUrl)(request, messages(application)).toString

        verify(mockService, times(1))
          .getReturnsByTypeViewModel(any(), eqTo(SUBMITTED_SUBMITTED_RETURNS), any())(any[HeaderCarrier])
      }
    }

    "must redirect to JourneyRecovery when pagination index is out of scope" in new Fixture {
      val invalidPageIndex = 100
      val totalRowCount = allPaginatedRows.length

      val viewModel = SdltSubmittedReturnViewModel(
        extractType = SUBMITTED_SUBMITTED_RETURNS,
        rows = allPaginatedRows.take(rowsPerPage),
        totalRowCount = totalRowCount,
        selectedPageIndex = invalidPageIndex
      )

      when(
        mockService.getReturnsByTypeViewModel(
          any(),
          eqTo(SUBMITTED_SUBMITTED_RETURNS),
          any()
        )(any[HeaderCarrier])
      ).thenReturn(Future.successful(viewModel))

      running(application) {
        val request = FakeRequest(
          GET,
          submittedControllerRoute + s"?paginationIndex=$invalidPageIndex"
        )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.routes.JourneyRecoveryController.onPageLoad().url

        verify(mockService, times(1))
          .getReturnsByTypeViewModel(any(), eqTo(SUBMITTED_SUBMITTED_RETURNS), any())(any[HeaderCarrier])
      }
    }

    "must redirect to JourneyRecovery if an error occurs during returns retrieval" in new Fixture {
      when(
        mockService.getReturnsByTypeViewModel(
          any(),
          eqTo(SUBMITTED_SUBMITTED_RETURNS),
          any()
        )(any[HeaderCarrier])
      ).thenReturn(Future.failed(new RuntimeException("boom")))

      running(application) {
        val request = FakeRequest(GET, submittedControllerRoute)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.routes.SystemErrorController.onPageLoad().url

        verify(mockService, times(1))
          .getReturnsByTypeViewModel(any(), eqTo(SUBMITTED_SUBMITTED_RETURNS), any())(any[HeaderCarrier])
      }
    }

  }
}
