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
import models.SdltReturnTypes.IN_PROGRESS_RETURNS
import models.responses.{SdltReturnViewModel, SdltReturnViewRow, UniversalStatus}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalacheck.Gen
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.Application
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.StampDutyLandTaxService
import uk.gov.hmrc.govukfrontend.views.Aliases.Pagination
import uk.gov.hmrc.http.HeaderCarrier
import utils.PaginationHelper
import views.html.InProgressReturnView

import scala.concurrent.Future

class InProgressReturnsControllerSpec extends SpecBase with MockitoSugar {

  val outOfScopePageIndex: Int = Gen.oneOf[Int](-1, -100, 9, 100, 200).sample.toList.take(1).head

  trait Fixture extends PaginationHelper {
    val rowsPerPage: Int = 10

    val mockService: StampDutyLandTaxService = mock[StampDutyLandTaxService]

    val application: Application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
      .overrides(bind[StampDutyLandTaxService].toInstance(mockService))
      .build()

    val expectedEmptyData: List[SdltReturnViewRow] = List[SdltReturnViewRow]()
    val viewModelNoRows: SdltReturnViewModel = SdltReturnViewModel(
      extractType = IN_PROGRESS_RETURNS,
      rows = expectedEmptyData,
      totalRowCount = Some(expectedEmptyData.length))

    val expectedDataPaginationOff: List[SdltReturnViewRow] =
      (0 to 7).toList.map(index =>
        SdltReturnViewRow(
          address = s"$index Riverside Drive",
          agentReference = "B4C72F7T3",
          purchaserName = "Brown",
          status = UniversalStatus.ACCEPTED
        )
      )

    val viewModelPaginationOff: SdltReturnViewModel = SdltReturnViewModel(
      extractType = IN_PROGRESS_RETURNS,
      rows = expectedDataPaginationOff,
      totalRowCount = Some(expectedDataPaginationOff.length))

    val expectedDataPaginationOn: List[SdltReturnViewRow] =
      (0 to 17).toList.map(index =>
        SdltReturnViewRow(
          address = s"$index Riverside Drive",
          agentReference = "B4C72F7T3",
          purchaserName = "Brown",
          status = UniversalStatus.ACCEPTED
        )
      )

    val viewModelPaginationOn: SdltReturnViewModel = SdltReturnViewModel(
      extractType = IN_PROGRESS_RETURNS,
      rows = expectedDataPaginationOn,
      totalRowCount = Some(expectedDataPaginationOn.length))

    val viewModelPaginationOnPage1: SdltReturnViewModel = SdltReturnViewModel(
      extractType = IN_PROGRESS_RETURNS,
      rows = expectedDataPaginationOn.take(rowsPerPage),
      totalRowCount = Some(expectedDataPaginationOn.length))

    val viewModelPaginationOnPage2: SdltReturnViewModel = SdltReturnViewModel(
      extractType = IN_PROGRESS_RETURNS,
      rows = expectedDataPaginationOn.takeRight(7),
      totalRowCount = Some(expectedDataPaginationOn.length))

    val urlSelector: Int => String = (selectedPageIndex: Int) => controllers.manage.routes.InProgressReturnsController.onPageLoad(Some(selectedPageIndex)).url
  }

  "InProgress Returns Controller " - {

    "return OK for GET:: show empty screen" in new Fixture {

      when(mockService.getReturnsByTypeViewModel(any(), any(), any())(any[HeaderCarrier]))
        .thenReturn(Future.successful(viewModelNoRows))

      running(application) {

        val request = FakeRequest(GET, manage.routes.InProgressReturnsController.onPageLoad(None).url)

        val result = route(application, request).value
        val view = application.injector.instanceOf[InProgressReturnView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(List[SdltReturnViewRow](), None, None)(request, messages(application)).toString

        verify(mockService, times(1)).getReturnsByTypeViewModel(any(), any(), any())(any[HeaderCarrier])
      }
    }

    "return OK for GET:: few rows :: pagination OFF" in new Fixture {
      val actualDataPaginationOff: List[SdltReturnViewRow] =
        (0 to 7).toList.map(index =>
          SdltReturnViewRow(
            address = s"$index Riverside Drive",
            agentReference = "B4C72F7T3",
            purchaserName = "Brown",
            status = UniversalStatus.ACCEPTED
          )
        )

      when(mockService.getReturnsByTypeViewModel(any(), any(), any())(any[HeaderCarrier]))
        .thenReturn(Future.successful(viewModelPaginationOff))

      running(application) {

        val request = FakeRequest(GET, manage.routes.InProgressReturnsController.onPageLoad(None).url)

        val result = route(application, request).value
        val view = application.injector.instanceOf[InProgressReturnView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(actualDataPaginationOff, None, None)(request, messages(application)).toString

        verify(mockService, times(1)).getReturnsByTypeViewModel(any(), any(), any())(any[HeaderCarrier])
      }
    }

    "return OK for GET:: more than 10 rows:: pagination ON :: page 1" in new Fixture {
      val actualDataPaginationOn: List[SdltReturnViewRow] =
        (0 to 17).toList.map(index =>
          SdltReturnViewRow(
            address = s"$index Riverside Drive",
            agentReference = "B4C72F7T3",
            purchaserName = "Brown",
            status = UniversalStatus.ACCEPTED
          )
        ).take(rowsPerPage)

      when(mockService.getReturnsByTypeViewModel(any(), any(), any())(any[HeaderCarrier]))
        .thenReturn(Future.successful(viewModelPaginationOnPage1))

      val selectedPageIndex: Int = 1
      val paginator: Option[Pagination] = createPagination(selectedPageIndex, expectedDataPaginationOn.length, urlSelector)(messages(application))
      val paginationText: Option[String] = getPaginationInfoText(selectedPageIndex, expectedDataPaginationOn.take(rowsPerPage))(messages(application))

      running(application) {

        val request = FakeRequest(GET, manage.routes.InProgressReturnsController.onPageLoad(None).url)

        val result = route(application, request).value
        val view = application.injector.instanceOf[InProgressReturnView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(actualDataPaginationOn.take(rowsPerPage), paginator, paginationText)(request, messages(application)).toString

        verify(mockService, times(1)).getReturnsByTypeViewModel(any(), any(), any())(any[HeaderCarrier])

      }
    }

    "return OK for GET:: more than 10 rows:: pagination ON :: page 2" in new Fixture {
      val actualDataPaginationOn: List[SdltReturnViewRow] = {
        (0 to 17).toList.map(index =>
          SdltReturnViewRow(
            address = s"$index Riverside Drive",
            agentReference = "B4C72F7T3",
            purchaserName = "Brown",
            status = UniversalStatus.ACCEPTED
          )
        )
      }.takeRight(7)

      val selectedPageIndex: Int = 2
      when(mockService.getReturnsByTypeViewModel(any(), any(), any())(any[HeaderCarrier]))
        .thenReturn(Future.successful(viewModelPaginationOnPage2))

      val paginator: Option[Pagination] = createPagination(selectedPageIndex,
        expectedDataPaginationOn.length, urlSelector)(messages(application))
      val paginationText: Option[String] = getPaginationInfoText(selectedPageIndex,
        expectedDataPaginationOn.takeRight(7))(messages(application))

      running(application) {

        val request = FakeRequest(GET, manage.routes.InProgressReturnsController.onPageLoad(None).url + s"?index=$selectedPageIndex")

        val result = route(application, request).value
        val view = application.injector.instanceOf[InProgressReturnView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(actualDataPaginationOn.takeRight(7), paginator, paginationText)(request, messages(application)).toString

        verify(mockService, times(1)).getReturnsByTypeViewModel(any(), any(), any())(any[HeaderCarrier])
      }

    }

    // random pageIndex would be used on each run
    s"return OK for GET:: more than 10 rows:: pagination ON :: page index out of scope ${outOfScopePageIndex}" in new Fixture {
      val actualDataPaginationOn: List[SdltReturnViewRow] = {
        (0 to 17).toList.map(index =>
          SdltReturnViewRow(
            address = s"$index Riverside Drive",
            agentReference = "B4C72F7T3",
            purchaserName = "Brown",
            status = UniversalStatus.ACCEPTED
          )
        )
      }
      val viewModelActual: SdltReturnViewModel = SdltReturnViewModel(
        extractType = IN_PROGRESS_RETURNS,
        rows = actualDataPaginationOn,
        totalRowCount = Some(actualDataPaginationOn.length)
      )

      when(mockService.getReturnsByTypeViewModel(any(), any(), any())(any[HeaderCarrier]))
        .thenReturn(Future.successful(viewModelActual))

      running(application) {

        val request = FakeRequest(GET, manage.routes.InProgressReturnsController.onPageLoad(None).url + s"?index=$outOfScopePageIndex")

        val result = route(application, request).value
        val view = application.injector.instanceOf[InProgressReturnView]

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustBe "/stamp-duty-land-tax-management/there-is-a-problem"

        verify(mockService, times(1)).getReturnsByTypeViewModel(any(), any(), any())(any[HeaderCarrier])
      }

    }

    // error case #1
    "return SEE_OTHER on GET :: service level error" in new Fixture {
      when(mockService.getReturnsByTypeViewModel(any(), any(), any())(any[HeaderCarrier]))
        .thenReturn(Future.failed(new RuntimeException("boom")))
      running(application) {
        val request = FakeRequest(GET, manage.routes.InProgressReturnsController.onPageLoad(None).url)
        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }

}
