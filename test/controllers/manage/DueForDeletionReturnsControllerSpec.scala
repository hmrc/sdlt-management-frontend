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
import models.requests.DataRequest
import models.responses.SdltInProgressReturnViewRow
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.Application
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.StampDutyLandTaxService
import uk.gov.hmrc.govukfrontend.views.Aliases.Pagination
import uk.gov.hmrc.http.HeaderCarrier
import utils.PaginationHelper
import viewmodels.manage.SdltSubmittedReturnsViewModel
import viewmodels.manage.deletedReturns.{
  PaginatedDeletedInProgressReturnsViewModel,
  PaginatedDeletedSubmittedReturnsViewModel,
  SdltDeletedInProgressReturnViewModel,
  SdltDeletedSubmittedReturnsViewModel
}
import views.html.manage.DueForDeletionReturnsView

import scala.concurrent.Future

class DueForDeletionReturnsControllerSpec extends SpecBase with MockitoSugar with ModelGenerators {

  trait Fixture extends PaginationHelper {
    val mockService: StampDutyLandTaxService = mock[StampDutyLandTaxService]

    val application: Application =
      applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[StampDutyLandTaxService].toInstance(mockService))
        .build()

    val inProgressUrlSelector: Int => String = inProgressIndex =>
      s"${
        controllers.manage.routes.DueForDeletionReturnsController
          .onPageLoad(Some(inProgressIndex), Some(1))
          .url
      }#in-progress-returns"

    val submittedUrlSelector: Int => String = submittedIndex =>
      s"${
        controllers.manage.routes.DueForDeletionReturnsController
          .onPageLoad(Some(1), Some(submittedIndex))
          .url
      }#submitted-returns"

    lazy val dueForDeletionRoute: String =
      controllers.manage.routes.DueForDeletionReturnsController.onPageLoad(Some(1), Some(1)).url

    def onwardRoute: Call =
      Call("GET", "/stamp-duty-land-tax-management/manage-returns/returns-due-for-deletion?inProgressIndex=1&submittedIndex=1")

    private def toDeletedInProgress(rows: List[SdltInProgressReturnViewRow]): List[SdltDeletedInProgressReturnViewModel] =
      rows.map { r =>
        SdltDeletedInProgressReturnViewModel(
          purchaserName = r.purchaserName,
          address       = r.address
        )
      }

    private def toDeletedSubmitted(rows: List[SdltSubmittedReturnsViewModel]): List[SdltDeletedSubmittedReturnsViewModel] =
      rows.map { r =>
        SdltDeletedSubmittedReturnsViewModel(
          purchaserName = r.purchaserName,
          address       = r.address,
          utrn          = r.utrn
        )
      }

    def deletedNoPaginationInProgressEmptyPagination: List[SdltDeletedInProgressReturnViewModel] =
      toDeletedInProgress(dataNoPaginationInProgressEmptyPagination)

    def deletedNoPaginationSubmittedEmptyPagination: List[SdltDeletedSubmittedReturnsViewModel] =
      toDeletedSubmitted(dataNoPaginationSubmittedEmptyPagination)

    def deletedPaginationInProgressHalfPagination: List[SdltDeletedInProgressReturnViewModel] =
      toDeletedInProgress(dataPaginationInProgressHalfPagination)

    def deletedNoPaginationSubmittedHalfPagination: List[SdltDeletedSubmittedReturnsViewModel] =
      toDeletedSubmitted(dataNoPaginationSubmittedHalfPagination)

    def deletedInProgressPaginationFullPagination: List[SdltDeletedInProgressReturnViewModel] =
      toDeletedInProgress(dataInProgressPaginationFullPagination)

    def deletedSubmittedPaginationFullPagination: List[SdltDeletedSubmittedReturnsViewModel] =
      toDeletedSubmitted(dataSubmittedPaginationFullPagination)
  }

  "DueForDeletionReturnsController" - {

    "must return OK and the correct view for a GET request with data and no pagination" in new Fixture {

      when(mockService.getInProgressReturnsDueForDeletion(any[HeaderCarrier], any[DataRequest[_]]))
        .thenReturn(Future.successful(noPaginationInProgressAndSubmitted))

      when(mockService.getSubmittedReturnsDueForDeletion(any[HeaderCarrier], any[DataRequest[_]]))
        .thenReturn(Future.successful(noPaginationInProgressAndSubmitted))

      running(application) {
        val request = FakeRequest(GET, dueForDeletionRoute)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[DueForDeletionReturnsView]

        val inProgressViewModel =
          PaginatedDeletedInProgressReturnsViewModel(deletedNoPaginationInProgressEmptyPagination, None, None)
        val submittedViewModel =
          PaginatedDeletedSubmittedReturnsViewModel(deletedNoPaginationSubmittedEmptyPagination, None, None)

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(inProgressViewModel, submittedViewModel)(request, messages(application)).toString

        verify(mockService, times(1)).getInProgressReturnsDueForDeletion(any[HeaderCarrier], any[DataRequest[_]])
        verify(mockService, times(1)).getSubmittedReturnsDueForDeletion(any[HeaderCarrier], any[DataRequest[_]])
      }
    }

    "must return OK and the correct view for a GET request when both tabs have no data" in new Fixture {

      when(mockService.getInProgressReturnsDueForDeletion(any[HeaderCarrier], any[DataRequest[_]]))
        .thenReturn(Future.successful(emptyReturnSummary))

      when(mockService.getSubmittedReturnsDueForDeletion(any[HeaderCarrier], any[DataRequest[_]]))
        .thenReturn(Future.successful(emptyReturnSummary))

      running(application) {
        val request = FakeRequest(GET, dueForDeletionRoute)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[DueForDeletionReturnsView]

        val inProgressViewModel =
          PaginatedDeletedInProgressReturnsViewModel(Nil, None, None)
        val submittedViewModel =
          PaginatedDeletedSubmittedReturnsViewModel(Nil, None, None)

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(inProgressViewModel, submittedViewModel)(request, messages(application)).toString

        verify(mockService, times(1)).getInProgressReturnsDueForDeletion(any[HeaderCarrier], any[DataRequest[_]])
        verify(mockService, times(1)).getSubmittedReturnsDueForDeletion(any[HeaderCarrier], any[DataRequest[_]])
      }
    }

    "must return OK and the correct view when in-progress has pagination and submitted does not" in new Fixture {
      val selectedPageIndex = 1

      val inProgressPaginator: Option[Pagination] =
        createPagination(selectedPageIndex, deletedPaginationInProgressHalfPagination.length, inProgressUrlSelector)(messages(application))
      val inProgressPaginationText: Option[String] =
        getPaginationInfoText(selectedPageIndex, deletedPaginationInProgressHalfPagination)(messages(application))

      val inProgressRowsForSelectedPage: List[SdltDeletedInProgressReturnViewModel] =
        getSelectedPageRows(deletedPaginationInProgressHalfPagination, selectedPageIndex)
      val submittedRowsForSelectedPage: List[SdltDeletedSubmittedReturnsViewModel] =
        getSelectedPageRows(deletedNoPaginationSubmittedHalfPagination, selectedPageIndex)

      when(mockService.getInProgressReturnsDueForDeletion(any[HeaderCarrier], any[DataRequest[_]]))
        .thenReturn(Future.successful(paginatedInProgressNoPaginationSubmitted))

      when(mockService.getSubmittedReturnsDueForDeletion(any[HeaderCarrier], any[DataRequest[_]]))
        .thenReturn(Future.successful(paginatedInProgressNoPaginationSubmitted))

      running(application) {
        val request = FakeRequest(GET, dueForDeletionRoute)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[DueForDeletionReturnsView]

        val inProgressViewModel =
          PaginatedDeletedInProgressReturnsViewModel(inProgressRowsForSelectedPage, inProgressPaginator, inProgressPaginationText)
        val submittedViewModel =
          PaginatedDeletedSubmittedReturnsViewModel(submittedRowsForSelectedPage, None, None)

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(inProgressViewModel, submittedViewModel)(request, messages(application)).toString

        verify(mockService, times(1)).getInProgressReturnsDueForDeletion(any[HeaderCarrier], any[DataRequest[_]])
        verify(mockService, times(1)).getSubmittedReturnsDueForDeletion(any[HeaderCarrier], any[DataRequest[_]])
      }
    }

    "must return OK and the correct view when both tabs have no pagination" in new Fixture {
      val selectedPageIndex = 1

      val inProgressRowsForSelectedPage: List[SdltDeletedInProgressReturnViewModel] =
        getSelectedPageRows(deletedNoPaginationInProgressEmptyPagination, selectedPageIndex)
      val submittedRowsForSelectedPage: List[SdltDeletedSubmittedReturnsViewModel] =
        getSelectedPageRows(deletedNoPaginationSubmittedEmptyPagination, selectedPageIndex)

      when(mockService.getInProgressReturnsDueForDeletion(any[HeaderCarrier], any[DataRequest[_]]))
        .thenReturn(Future.successful(noPaginationInProgressAndSubmitted))

      when(mockService.getSubmittedReturnsDueForDeletion(any[HeaderCarrier], any[DataRequest[_]]))
        .thenReturn(Future.successful(noPaginationInProgressAndSubmitted))

      running(application) {
        val request = FakeRequest(GET, dueForDeletionRoute)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[DueForDeletionReturnsView]

        val inProgressViewModel =
          PaginatedDeletedInProgressReturnsViewModel(inProgressRowsForSelectedPage, None, None)
        val submittedViewModel =
          PaginatedDeletedSubmittedReturnsViewModel(submittedRowsForSelectedPage, None, None)

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(inProgressViewModel, submittedViewModel)(request, messages(application)).toString

        verify(mockService, times(1)).getInProgressReturnsDueForDeletion(any[HeaderCarrier], any[DataRequest[_]])
        verify(mockService, times(1)).getSubmittedReturnsDueForDeletion(any[HeaderCarrier], any[DataRequest[_]])
      }
    }

    "must return OK and the correct view when both tabs have pagination" in new Fixture {
      val pageIndex = 1

      val inProgressPaginator: Option[Pagination] =
        createPagination(pageIndex, deletedInProgressPaginationFullPagination.length, inProgressUrlSelector)(messages(application))
      val submittedPaginator: Option[Pagination] =
        createPagination(pageIndex, deletedSubmittedPaginationFullPagination.length, submittedUrlSelector)(messages(application))

      val inProgressPaginationText: Option[String] =
        getPaginationInfoText(pageIndex, deletedInProgressPaginationFullPagination)(messages(application))
      val submittedPaginationText: Option[String] =
        getPaginationInfoText(pageIndex, deletedSubmittedPaginationFullPagination)(messages(application))

      val inProgressRowsForSelectedPage: List[SdltDeletedInProgressReturnViewModel] =
        getSelectedPageRows(deletedInProgressPaginationFullPagination, pageIndex)
      val submittedRowsForSelectedPage: List[SdltDeletedSubmittedReturnsViewModel] =
        getSelectedPageRows(deletedSubmittedPaginationFullPagination, pageIndex)

      when(mockService.getInProgressReturnsDueForDeletion(any[HeaderCarrier], any[DataRequest[_]]))
        .thenReturn(Future.successful(paginatedInProgressAndSubmitted))

      when(mockService.getSubmittedReturnsDueForDeletion(any[HeaderCarrier], any[DataRequest[_]]))
        .thenReturn(Future.successful(paginatedInProgressAndSubmitted))

      running(application) {
        val request = FakeRequest(GET, dueForDeletionRoute)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[DueForDeletionReturnsView]

        val inProgressViewModel =
          PaginatedDeletedInProgressReturnsViewModel(inProgressRowsForSelectedPage, inProgressPaginator, inProgressPaginationText)
        val submittedViewModel =
          PaginatedDeletedSubmittedReturnsViewModel(submittedRowsForSelectedPage, submittedPaginator, submittedPaginationText)

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(inProgressViewModel, submittedViewModel)(request, messages(application)).toString

        verify(mockService, times(1)).getInProgressReturnsDueForDeletion(any[HeaderCarrier], any[DataRequest[_]])
        verify(mockService, times(1)).getSubmittedReturnsDueForDeletion(any[HeaderCarrier], any[DataRequest[_]])
      }
    }

    "must return OK and the correct view when in-progress is on page 2 and submitted is on page 1" in new Fixture {
      val inProgressPageIndex = 2
      val submittedPageIndex  = 1

      val inProgressPaginator: Option[Pagination] =
        createPagination(inProgressPageIndex, deletedPaginationInProgressHalfPagination.length, inProgressUrlSelector)(messages(application))
      val inProgressPaginationText: Option[String] =
        getPaginationInfoText(inProgressPageIndex, deletedPaginationInProgressHalfPagination)(messages(application))

      val inProgressRowsForSelectedPage: List[SdltDeletedInProgressReturnViewModel] =
        getSelectedPageRows(deletedPaginationInProgressHalfPagination, inProgressPageIndex)
      val submittedRowsForSelectedPage: List[SdltDeletedSubmittedReturnsViewModel] =
        getSelectedPageRows(deletedNoPaginationSubmittedHalfPagination, submittedPageIndex)

      when(mockService.getInProgressReturnsDueForDeletion(any[HeaderCarrier], any[DataRequest[_]]))
        .thenReturn(Future.successful(paginatedInProgressNoPaginationSubmitted))

      when(mockService.getSubmittedReturnsDueForDeletion(any[HeaderCarrier], any[DataRequest[_]]))
        .thenReturn(Future.successful(paginatedInProgressNoPaginationSubmitted))

      running(application) {
        val request =
          FakeRequest(GET, controllers.manage.routes.DueForDeletionReturnsController.onPageLoad(Some(2), Some(1)).url)

        val result = route(application, request).value
        val view   = application.injector.instanceOf[DueForDeletionReturnsView]

        val inProgressViewModel =
          PaginatedDeletedInProgressReturnsViewModel(inProgressRowsForSelectedPage, inProgressPaginator, inProgressPaginationText)
        val submittedViewModel =
          PaginatedDeletedSubmittedReturnsViewModel(submittedRowsForSelectedPage, None, None)

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(inProgressViewModel, submittedViewModel)(request, messages(application)).toString

        verify(mockService, times(1)).getInProgressReturnsDueForDeletion(any[HeaderCarrier], any[DataRequest[_]])
        verify(mockService, times(1)).getSubmittedReturnsDueForDeletion(any[HeaderCarrier], any[DataRequest[_]])
      }
    }

    "must redirect to pageIndex=1 when the page index is out of scope" in new Fixture {
      val invalidPageIndex = 100

      when(mockService.getInProgressReturnsDueForDeletion(any[HeaderCarrier], any[DataRequest[_]]))
        .thenReturn(Future.successful(paginatedInProgressNoPaginationSubmitted))

      when(mockService.getSubmittedReturnsDueForDeletion(any[HeaderCarrier], any[DataRequest[_]]))
        .thenReturn(Future.successful(paginatedInProgressNoPaginationSubmitted))

      running(application) {
        val request =
          FakeRequest(GET, controllers.manage.routes.DueForDeletionReturnsController.onPageLoad(Some(invalidPageIndex), Some(1)).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustBe onwardRoute.url

        verify(mockService, times(1)).getInProgressReturnsDueForDeletion(any[HeaderCarrier], any[DataRequest[_]])
        verify(mockService, times(1)).getSubmittedReturnsDueForDeletion(any[HeaderCarrier], any[DataRequest[_]])
      }
    }

    "must redirect to JourneyRecovery if an error occurs during returns retrieval" in new Fixture {

      when(mockService.getSubmittedReturnsDueForDeletion(any[HeaderCarrier], any[DataRequest[_]]))
        .thenReturn(Future.successful(emptyReturnSummary))

      when(mockService.getInProgressReturnsDueForDeletion(any[HeaderCarrier], any[DataRequest[_]]))
        .thenReturn(Future.failed(new RuntimeException("Error")))

      running(application) {
        val request = FakeRequest(GET, dueForDeletionRoute)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
