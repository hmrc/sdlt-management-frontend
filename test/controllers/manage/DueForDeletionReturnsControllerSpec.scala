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
import generators.ModelGenerators
import models.manage.ReturnSummary
import models.requests.DataRequest
import models.responses.PaginatedInProgressReturnsViewModel
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.Application
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.StampDutyLandTaxService
import uk.gov.hmrc.govukfrontend.views.Aliases.Pagination
import uk.gov.hmrc.http.HeaderCarrier
import utils.PaginationHelper
import viewmodels.manage.PaginatedSubmittedReturnsViewModel
import views.html.manage.DueForDeletionReturnsView

import scala.concurrent.Future

class DueForDeletionReturnsControllerSpec extends SpecBase with MockitoSugar with ModelGenerators {

  trait Fixture extends PaginationHelper {
    val mockService: StampDutyLandTaxService = mock[StampDutyLandTaxService]

    val application: Application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
      .overrides(bind[StampDutyLandTaxService].toInstance(mockService))
      .build()

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

    lazy val DueForDeletionReturnsControllerRoute = controllers.manage.routes.DueForDeletionReturnsController.onPageLoad(Some(1), Some(1)).url
    def onwardRoute = Call("GET", "/stamp-duty-land-tax-management/manage-returns/returns-due-for-deletion?inProgressIndex=1&submittedIndex=1")
  }

  "DueForDeletionReturnsController " - {

    "must return OK and the correct view for a GET request with data" in new Fixture {

      when(mockService.getReturnsDueForDeletion(any[HeaderCarrier], any[DataRequest[_]]))
        .thenReturn(Future.successful(noPaginationInProgressAndSubmitted))

      running(application) {

        val request = FakeRequest(GET, DueForDeletionReturnsControllerRoute)

        val result = route(application, request).value
        val view = application.injector.instanceOf[DueForDeletionReturnsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(PaginatedInProgressReturnsViewModel(dataNoPaginationInProgressEmptyPagination, None, None),
          PaginatedSubmittedReturnsViewModel(dataNoPaginationSubmittedEmptyPagination, None, None))(request, messages(application)).toString

        verify(mockService, times(1)).getReturnsDueForDeletion(any[HeaderCarrier], any[DataRequest[_]])

      }
    }

    "must return OK and the correct view for a GET request without data" in new Fixture {

      when(mockService.getReturnsDueForDeletion(any[HeaderCarrier], any[DataRequest[_]]))
        .thenReturn(Future.successful(emptyReturnSummary))

      running(application) {

        val request = FakeRequest(GET, DueForDeletionReturnsControllerRoute)

        val result = route(application, request).value
        val view = application.injector.instanceOf[DueForDeletionReturnsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(PaginatedInProgressReturnsViewModel(List.empty, None, None),
          PaginatedSubmittedReturnsViewModel(List.empty, None, None))(request, messages(application)).toString

        verify(mockService, times(1)).getReturnsDueForDeletion(any[HeaderCarrier], any[DataRequest[_]])

      }
    }

    "must return OK and the correct view for a GET request when one tab does not have pagination" in new Fixture {
      val inProgressPageIndex: Int = 1
      val inProgressPaginator: Option[Pagination] =
        createPagination(inProgressPageIndex, dataPaginationInProgressHalfPagination.length, inProgressUrlSelector)(messages(application))
      val inProgressPaginationText: Option[String] = getPaginationInfoText(inProgressPageIndex, dataPaginationInProgressHalfPagination)(messages(application))

      when(mockService.getReturnsDueForDeletion(any[HeaderCarrier], any[DataRequest[_]]))
        .thenReturn(Future.successful(paginatedInProgressNoPaginationSubmitted))

      running(application) {

        val request = FakeRequest(GET, DueForDeletionReturnsControllerRoute)

        val result = route(application, request).value
        val view = application.injector.instanceOf[DueForDeletionReturnsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(PaginatedInProgressReturnsViewModel(dataPaginationInProgressHalfPagination, inProgressPaginator, inProgressPaginationText),
         PaginatedSubmittedReturnsViewModel(dataNoPaginationSubmittedHalfPagination, None, None))(request, messages(application)).toString

        verify(mockService, times(1)).getReturnsDueForDeletion(any[HeaderCarrier], any[DataRequest[_]])
      }
    }

    "must return OK and the correct view for a GET request when both tabs do not have pagination" in new Fixture {

      when(mockService.getReturnsDueForDeletion(any[HeaderCarrier], any[DataRequest[_]]))
        .thenReturn(Future.successful(noPaginationInProgressAndSubmitted))

      running(application) {

        val request = FakeRequest(GET, DueForDeletionReturnsControllerRoute)

        val result = route(application, request).value
        val view = application.injector.instanceOf[DueForDeletionReturnsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(PaginatedInProgressReturnsViewModel(dataNoPaginationInProgressEmptyPagination, None, None),
          PaginatedSubmittedReturnsViewModel(dataNoPaginationSubmittedEmptyPagination, None, None))(request, messages(application)).toString

        verify(mockService, times(1)).getReturnsDueForDeletion(any[HeaderCarrier], any[DataRequest[_]])
      }
    }

    "must return OK and the correct view for a GET request when both tabs have pagination" in new Fixture {
      val pageIndex: Int = 1
      val inProgressPaginator: Option[Pagination] =
        createPagination(pageIndex, dataInProgressPaginationFullPagination.length, inProgressUrlSelector)(messages(application))
      val submittedPaginator: Option[Pagination] =
        createPagination(pageIndex, dataSubmittedPaginationFullPagination.length, submittedUrlSelector)(messages(application))

      val inProgressPaginationText: Option[String] = getPaginationInfoText(pageIndex, dataInProgressPaginationFullPagination)(messages(application))
      val submittedPaginationText: Option[String] = getPaginationInfoText(pageIndex, dataSubmittedPaginationFullPagination)(messages(application))

      when(mockService.getReturnsDueForDeletion(any[HeaderCarrier], any[DataRequest[_]]))
        .thenReturn(Future.successful(paginatedInProgressAndSubmitted))

      running(application) {

        val request = FakeRequest(GET, DueForDeletionReturnsControllerRoute)

        val result = route(application, request).value
        val view = application.injector.instanceOf[DueForDeletionReturnsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(PaginatedInProgressReturnsViewModel(dataInProgressPaginationFullPagination, inProgressPaginator, inProgressPaginationText),
          PaginatedSubmittedReturnsViewModel(dataSubmittedPaginationFullPagination, submittedPaginator, submittedPaginationText))(request, messages(application)).toString

        verify(mockService, times(1)).getReturnsDueForDeletion(any[HeaderCarrier], any[DataRequest[_]])
      }
    }

    "must return OK and the correct view for a GET request with pagination Page 2" in new Fixture {
      val inProgressPageIndex: Int = 2
      val inProgressPaginator: Option[Pagination] =
        createPagination(inProgressPageIndex, dataPaginationInProgressHalfPagination.length, inProgressUrlSelector)(messages(application))

      val inProgressPaginationText: Option[String] = getPaginationInfoText(inProgressPageIndex, dataPaginationInProgressHalfPagination)(messages(application))

      when(mockService.getReturnsDueForDeletion(any[HeaderCarrier], any[DataRequest[_]]))
        .thenReturn(Future.successful(paginatedInProgressNoPaginationSubmitted))

      running(application) {

        val request = FakeRequest(GET, controllers.manage.routes.DueForDeletionReturnsController.onPageLoad(Some(2), Some(1)).url)

        val result = route(application, request).value
        val view = application.injector.instanceOf[DueForDeletionReturnsView]


        status(result) mustEqual OK
        contentAsString(result) mustEqual view(PaginatedInProgressReturnsViewModel(dataPaginationInProgressHalfPagination, inProgressPaginator, inProgressPaginationText),
          PaginatedSubmittedReturnsViewModel(dataNoPaginationSubmittedHalfPagination, None, None))(request, messages(application)).toString

        verify(mockService, times(1)).getReturnsDueForDeletion(any[HeaderCarrier], any[DataRequest[_]])
      }

    }

    "must redirect to pageIndex=1 for a GET request when pageindex is out of scope" in new Fixture {
      val invalidPageIndex: Int = 100
      val inProgressPaginator: Option[Pagination] =
        createPagination(invalidPageIndex, dataPaginationInProgressHalfPagination.length, inProgressUrlSelector)(messages(application))

      val inProgressPaginationText: Option[String] = getPaginationInfoText(invalidPageIndex, dataPaginationInProgressHalfPagination)(messages(application))

      when(mockService.getReturnsDueForDeletion(any[HeaderCarrier], any[DataRequest[_]]))
        .thenReturn(Future.successful(paginatedInProgressNoPaginationSubmitted))

      running(application) {

        val request = FakeRequest(GET, controllers.manage.routes.DueForDeletionReturnsController.onPageLoad(Some(100), Some(1)).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustBe onwardRoute.url

        verify(mockService, times(1)).getReturnsDueForDeletion(any[HeaderCarrier], any[DataRequest[_]])
      }

    }

    "redirect to JourneyRecovery if error occurs during returns retrieval" in new Fixture {

      when(mockService.getReturnsDueForDeletion(any[HeaderCarrier], any[DataRequest[_]]))
        .thenReturn(Future.successful(new Error("Error")))

      running(application) {

        val request = FakeRequest(GET, DueForDeletionReturnsControllerRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }

}
