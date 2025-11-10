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

package controllers

import base.SpecBase
import models.responses.{SdltReturnViewRow, UniversalStatus}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.Application
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.InProgressReturnsService
import uk.gov.hmrc.govukfrontend.views.Aliases.Pagination
import utils.PaginationHelper
import views.html.InProgressReturnView

import java.time.LocalDate
import scala.concurrent.Future

class InProgressReturnsControllerSpec extends SpecBase with MockitoSugar {

  trait Fixture extends PaginationHelper {
    val rowsPerPage : Int = 10
    val mockService: InProgressReturnsService = mock[InProgressReturnsService]
    val application: Application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
      .overrides(bind[InProgressReturnsService].toInstance(mockService))
      .build()

    val expectedEmptyData: List[SdltReturnViewRow] = List[SdltReturnViewRow]()
    val expectedDataPaginationOff: List[SdltReturnViewRow] =
      (0 to 7).toList.map( index =>
        SdltReturnViewRow(
          address = s"$index Riverside Drive",
          agentReference = "B4C72F7T3",
          dateSubmitted = LocalDate.parse("2025-04-05"),
          utrn = "UTRN003",
          purchaserName = "Brown",
          status = UniversalStatus.ACCEPTED,
          returnReference = "RETREF003",
          returnId = s"RETID$index"
        )
      )

    val expectedDataPaginationOn: List[SdltReturnViewRow] =
      (0 to 17).toList.map(index =>
        SdltReturnViewRow(
          address = s"$index Riverside Drive",
          agentReference = "B4C72F7T3",
          dateSubmitted = LocalDate.parse("2025-04-05"),
          utrn = "UTRN003",
          purchaserName = "Brown",
          status = UniversalStatus.ACCEPTED,
          returnReference = "RETREF003",
          returnId = s"RETID$index"
        )
      )
  }

  "InProgress Returns Controller " - {

    // happy path
    "return OK for GET:: show empty screen" in new Fixture {

      when(mockService.getAllReturns(any()))
        .thenReturn( Future.successful(Right(expectedEmptyData)) )

      when(mockService.getPageRows(any(), any(), any()))
        .thenReturn(expectedEmptyData)

      running(application) {

        val request = FakeRequest(GET, manage.routes.InProgressReturnsController.onPageLoad(None).url)

        val result = route(application, request).value
        val view = application.injector.instanceOf[InProgressReturnView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(List[SdltReturnViewRow](), None, None)(request, messages(application)).toString

        verify(mockService, times(1)).getAllReturns(any())
        verify(mockService, times(1)).getPageRows(any(), any(), any())
      }
    }

    "return OK for GET:: few rows :: pagination OFF" in new Fixture {
      val actualDataPaginationOff: List[SdltReturnViewRow] =
        (0 to 7).toList.map(index =>
          SdltReturnViewRow(
            address = s"$index Riverside Drive",
            agentReference = "B4C72F7T3",
            dateSubmitted = LocalDate.parse("2025-04-05"),
            utrn = "UTRN003",
            purchaserName = "Brown",
            status = UniversalStatus.ACCEPTED,
            returnReference = "RETREF003",
            returnId = s"RETID$index"
          )
        )

      when(mockService.getAllReturns(any()))
        .thenReturn(Future.successful(Right(expectedDataPaginationOff)))

      when(mockService.getPageRows(any(), any(), any()))
        .thenReturn(expectedDataPaginationOff)

      running(application) {

        val request = FakeRequest(GET, manage.routes.InProgressReturnsController.onPageLoad(None).url)

        val result = route(application, request).value
        val view = application.injector.instanceOf[InProgressReturnView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(actualDataPaginationOff, None, None)(request, messages(application)).toString

        verify(mockService, times(1)).getAllReturns(any())
        verify(mockService, times(1)).getPageRows(any(), any(), any())
      }
    }

    "return OK for GET:: more than 10 rows:: pagination ON :: page 1" in new Fixture {
      val actualDataPaginationOn: List[SdltReturnViewRow] =
        (0 to 17).toList.map(index =>
          SdltReturnViewRow(
            address = s"$index Riverside Drive",
            agentReference = "B4C72F7T3",
            dateSubmitted = LocalDate.parse("2025-04-05"),
            utrn = "UTRN003",
            purchaserName = "Brown",
            status = UniversalStatus.ACCEPTED,
            returnReference = "RETREF003",
            returnId = s"RETID$index"
          )
        )

      when(mockService.getAllReturns(any()))
        .thenReturn(Future.successful(Right(expectedDataPaginationOn)))

      when(mockService.getPageRows(any(), any(), any()))
        .thenReturn(expectedDataPaginationOn.take(rowsPerPage))

      val selectedPageIndex : Int = 1
      val paginator: Option[Pagination] = createPagination(selectedPageIndex, expectedDataPaginationOn.length)(messages(application))
      val paginationText: Option[String] = getPaginationInfoText(selectedPageIndex, expectedDataPaginationOn)(messages(application))

      running(application) {

        val request = FakeRequest(GET, manage.routes.InProgressReturnsController.onPageLoad(None).url)

        val result = route(application, request).value
        val view = application.injector.instanceOf[InProgressReturnView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(actualDataPaginationOn.take(rowsPerPage), paginator, paginationText)(request, messages(application)).toString

        verify(mockService, times(1)).getAllReturns(any())
        verify(mockService, times(1)).getPageRows(any(), any(), any())

      }
    }

    "return OK for GET:: more than 10 rows:: pagination ON :: page 2" in new Fixture {
      val actualDataPaginationOn: List[SdltReturnViewRow] = {
        (0 to 17).toList.map(index =>
          SdltReturnViewRow(
            address = s"$index Riverside Drive",
            agentReference = "B4C72F7T3",
            dateSubmitted = LocalDate.parse("2025-04-05"),
            utrn = "UTRN003",
            purchaserName = "Brown",
            status = UniversalStatus.ACCEPTED,
            returnReference = "RETREF003",
            returnId = s"RETID$index"
          )
        )
      }

      val selectedPageIndex : Int = 2
      when(mockService.getAllReturns(any()))
        .thenReturn(Future.successful(Right(expectedDataPaginationOn)))

      when(mockService.getPageRows(any(), any(), any()))
        .thenReturn(expectedDataPaginationOn.takeRight(actualDataPaginationOn.length - rowsPerPage) )

      val paginator: Option[Pagination] = createPagination(selectedPageIndex, expectedDataPaginationOn.length)(messages(application))
      val paginationText: Option[String] = getPaginationInfoText(selectedPageIndex, expectedDataPaginationOn)(messages(application))

      running(application) {

        val request = FakeRequest(GET, manage.routes.InProgressReturnsController.onPageLoad(None).url + s"?index=$selectedPageIndex")

        val result = route(application, request).value
        val view = application.injector.instanceOf[InProgressReturnView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(actualDataPaginationOn.takeRight(actualDataPaginationOn.length - rowsPerPage), paginator, paginationText)(request, messages(application)).toString

        verify(mockService, times(1)).getAllReturns(any())
        verify(mockService, times(1)).getPageRows(any(), any(), any())
      }

    }

    "return OK for GET:: more than 10 rows:: pagination ON :: page index out of scope" in new Fixture {
      val actualDataPaginationOn: List[SdltReturnViewRow] = {
        (0 to 17).toList.map(index =>
          SdltReturnViewRow(
            address = s"$index Riverside Drive",
            agentReference = "B4C72F7T3",
            dateSubmitted = LocalDate.parse("2025-04-05"),
            utrn = "UTRN003",
            purchaserName = "Brown",
            status = UniversalStatus.ACCEPTED,
            returnReference = "RETREF003",
            returnId = s"RETID$index"
          )
        )
      }

      val selectedPageIndex: Int = 9
      when(mockService.getAllReturns(any()))
        .thenReturn(Future.successful(Right(expectedDataPaginationOn)))

      when(mockService.getPageRows(any(), any(), any()))
        .thenReturn(List.empty)

      running(application) {

        val request = FakeRequest(GET, manage.routes.InProgressReturnsController.onPageLoad(None).url + s"?index=$selectedPageIndex")

        val result = route(application, request).value
        val view = application.injector.instanceOf[InProgressReturnView]

        status(result) mustEqual OK
        // NoData found screen expected
        contentAsString(result) mustEqual view(List.empty, None, None)(request, messages(application)).toString

        verify(mockService, times(1)).getAllReturns(any())
        verify(mockService, times(1)).getPageRows(any(), any(), any())
      }

    }

    // error case #1
    "return SEE_OTHER on GET :: service level error" in new Fixture {

      when(mockService.getAllReturns(any()))
        .thenReturn(Future.successful(Left(new Error("SomeError"))))

      running(application) {

        val request = FakeRequest(GET, manage.routes.InProgressReturnsController.onPageLoad(None).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    // error case #?? pagination errors?
  }



}
