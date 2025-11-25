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
import models.requests.DataRequest
import models.responses.{PaginatedInProgressReturnsViewModel, SdltInProgressReturnViewRow, UniversalStatus}
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
import viewmodels.manage.{PaginatedSubmittedReturnsViewModel, SdltSubmittedReturnsViewModel}
import views.html.manage.DueDeletion

import java.time.LocalDate
import scala.concurrent.Future

class DueForDeletionReturnsControllerSpec extends SpecBase with MockitoSugar {

  trait Fixture extends PaginationHelper {
    val rowsPerPage: Int = 10

    val mockService: StampDutyLandTaxService = mock[StampDutyLandTaxService]

    val application: Application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
      .overrides(bind[StampDutyLandTaxService].toInstance(mockService))
      .build()
  }

//    val expectedEmptyData: List[SdltSubmittedReturnsViewModel] = List[SdltSubmittedReturnsViewModel]()
//
//    val expectedDataNoPagination: List[SdltSubmittedReturnsViewModel] =
//      (0 to 7).toList.map(index =>
//        SdltSubmittedReturnsViewModel(
//          address = s"$index Riverside Drive",
//          utrn = "UTRN003",
//          purchaserName = "Brown",
//          status = UniversalStatus.SUBMITTED
//        )
//      )
//
//    val expectedDataPagination: List[SdltSubmittedReturnsViewModel] =
//      (0 to 17).toList.map(index =>
//        SdltSubmittedReturnsViewModel(
//          address = s"$index Riverside Drive",
//          utrn = "UTRN003",
//          purchaserName = "Brown",
//          status = UniversalStatus.SUBMITTED_NO_RECEIPT
//        )
//      )
//
//    val expectedDataNoPaginationInProgress: List[SdltInProgressReturnViewRow] =
//      (0 to 7).toList.map(index =>
//        SdltInProgressReturnViewRow(
//          address = s"$index Riverside Drive",
//          agentReference = "AGTREF003",
//          purchaserName = "Brown",
//          status = UniversalStatus.ACCEPTED
//        )
//      )
//
//    val expectedDataPaginationInProgress: List[SdltInProgressReturnViewRow] =
//      (0 to 17).toList.map(index =>
//        SdltInProgressReturnViewRow(
//          address = s"$index Riverside Drive",
//          agentReference = "AGTREF003",
//          purchaserName = "Brown",
//          status = UniversalStatus.PENDING
//        )
//      )
//
//    val urlSelector: Int => String = (selectedPageIndex: Int) => controllers.manage.routes.DueForDeletionController.onPageLoad(Some(selectedPageIndex)).url
//        lazy val DueForDeletionReturnsControllerRoute = controllers.manage.routes.DueForDeletionController.onPageLoad(None).url
//    def onwardRoute = Call("GET", "/stamp-duty-land-tax-management/manage-returns/due-for-deletion?paginationIndex=1")
//  }
//
//  "DueForDeletionReturnsController " - {
//
//    "must return OK and the correct view for a GET request" in new Fixture {
//
//      when(mockService.getReturnsDueForDeletion(any[HeaderCarrier], any[DataRequest[_]]))
//        .thenReturn(Future.successful(expectedEmptyData))
//
//      running(application) {
//
//        val request = FakeRequest(GET, DueForDeletionReturnsControllerRoute)
//
//        val result = route(application, request).value
//        val view = application.injector.instanceOf[DueDeletion]
//
//        status(result) mustEqual OK
//        contentAsString(result) mustEqual view(PaginatedInProgressReturnsViewModel(), PaginatedSubmittedReturnsViewModel())(request, messages(application)).toString
//
//        verify(mockService, times(1)).getSubmittedReturns(any[HeaderCarrier], any[DataRequest[_]])
//      }
//    }
//
//    "must return OK and the correct view for a GET request with no pagination" in new Fixture {
//      val actualDataNoPagination: List[SdltSubmittedReturnsViewModel] =
//        (0 to 7).toList.map(index =>
//          SdltSubmittedReturnsViewModel(
//            address = s"$index Riverside Drive",
//            utrn = "UTRN003",
//            purchaserName = "Brown",
//            status = UniversalStatus.SUBMITTED
//          )
//        )
//
//      when(mockService.getSubmittedReturns(any[HeaderCarrier], any[DataRequest[_]]))
//        .thenReturn(Future.successful(actualDataNoPagination))
//
//      running(application) {
//
//        val request = FakeRequest(GET, SubmittedControllerRoute)
//
//        val result = route(application, request).value
//        val view = application.injector.instanceOf[SubmittedReturnsView]
//
//        status(result) mustEqual OK
//        contentAsString(result) mustEqual view(expectedDataNoPagination, None, None)(request, messages(application)).toString
//
//        verify(mockService, times(1)).getSubmittedReturns(any[HeaderCarrier], any[DataRequest[_]])
//      }
//    }
//
//    "must return OK and the correct view for a GET request with pagination" in new Fixture {
//      val selectedPageIndex: Int = 1
//      val paginator: Option[Pagination] = createPagination(selectedPageIndex, expectedDataPagination.length, urlSelector)(messages(application))
//      val paginationText: Option[String] = getPaginationInfoText(selectedPageIndex, expectedDataPagination)(messages(application))
//
//      val actualDataPagination: List[SdltSubmittedReturnsViewModel] =
//      (0 to 17).toList.map(index =>
//        SdltSubmittedReturnsViewModel(
//          address = s"$index Riverside Drive",
//          utrn = "UTRN003",
//          purchaserName = "Brown",
//          status = UniversalStatus.SUBMITTED_NO_RECEIPT
//        )
//      )
//
//      when(mockService.getSubmittedReturns(any[HeaderCarrier], any[DataRequest[_]]))
//        .thenReturn(Future.successful(actualDataPagination))
//
//
//      running(application) {
//
//        val request = FakeRequest(GET, SubmittedControllerRoute)
//
//        val result = route(application, request).value
//        val view = application.injector.instanceOf[SubmittedReturnsView]
//
//        status(result) mustEqual OK
//        contentAsString(result) mustEqual view(expectedDataPagination.take(rowsPerPage), paginator, paginationText)(request, messages(application)).toString
//
//        verify(mockService, times(1)).getSubmittedReturns(any[HeaderCarrier], any[DataRequest[_]])
//      }
//    }
//
//    "must return OK and the correct view for a GET request with pagination Page 2" in new Fixture {
//      val selectedPageIndex: Int = 2
//      val paginator: Option[Pagination] = createPagination(selectedPageIndex, expectedDataPagination.length, urlSelector)(messages(application))
//      val paginationText: Option[String] = getPaginationInfoText(selectedPageIndex, expectedDataPagination)(messages(application))
//
//      val actualDataPagination: List[SdltSubmittedReturnsViewModel] =
//      (0 to 17).toList.map(index =>
//        SdltSubmittedReturnsViewModel(
//          address = s"$index Riverside Drive",
//          utrn = "UTRN003",
//          purchaserName = "Brown",
//          status = UniversalStatus.SUBMITTED_NO_RECEIPT
//        )
//      )
//
//      when(mockService.getSubmittedReturns(any[HeaderCarrier], any[DataRequest[_]]))
//        .thenReturn(Future.successful(actualDataPagination))
//
//      running(application) {
//
//        val request = FakeRequest(GET, SubmittedControllerRoute + s"?paginationIndex=$selectedPageIndex")
//
//        val result = route(application, request).value
//        val view = application.injector.instanceOf[SubmittedReturnsView]
//
//        status(result) mustEqual OK
//        contentAsString(result) mustEqual view(expectedDataPagination.takeRight(expectedDataPagination.length - rowsPerPage), paginator, paginationText)(request, messages(application)).toString
//
//        verify(mockService, times(1)).getSubmittedReturns(any[HeaderCarrier], any[DataRequest[_]])
//      }
//
//    }
//
//    "must redirect to paginationIndex=1 for a GET request when pagination index is out of scope" in new Fixture {
//      val invalidPageIndex: Int = 100
//
//      val actualDataPagination: List[SdltSubmittedReturnsViewModel] = {
//        (0 to 17).toList.map(index =>
//          SdltSubmittedReturnsViewModel(
//            address = s"$index Riverside Drive",
//            utrn = "UTRN003",
//            purchaserName = "Brown",
//            status = UniversalStatus.SUBMITTED_NO_RECEIPT
//          )
//        )
//      }
//
//      when(mockService.getSubmittedReturns(any[HeaderCarrier], any[DataRequest[_]]))
//        .thenReturn(Future.successful(actualDataPagination))
//
//
//      running(application) {
//
//        val request = FakeRequest(GET, SubmittedControllerRoute + s"?paginationIndex=$invalidPageIndex")
//
//        val result = route(application, request).value
//        val view = application.injector.instanceOf[SubmittedReturnsView]
//
//        status(result) mustEqual SEE_OTHER
//        redirectLocation(result).value mustBe onwardRoute.url
//
//        verify(mockService, times(1)).getSubmittedReturns(any[HeaderCarrier], any[DataRequest[_]])
//      }
//
//    }
//
//    "redirect to JourneyRecovery if error occurs during returns retrieval" in new Fixture {
//
//      when(mockService.getSubmittedReturns(any[HeaderCarrier], any[DataRequest[_]]))
//        .thenReturn(Future.successful(new Error("Error")))
//
//      running(application) {
//
//        val request = FakeRequest(GET, SubmittedControllerRoute)
//
//        val result = route(application, request).value
//
//        status(result) mustEqual SEE_OTHER
//        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
//      }
//    }
//  }

}
