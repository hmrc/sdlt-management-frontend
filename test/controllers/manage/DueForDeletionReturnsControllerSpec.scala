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
import models.SdltReturnTypes.{IN_PROGRESS_RETURNS_DUE_FOR_DELETION, SUBMITTED_RETURNS_DUE_FOR_DELETION}
import models.manage.ReturnSummary
import models.responses.{SdltReturnViewModel, SdltReturnViewRow}
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import play.api.Application
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.StampDutyLandTaxService
import uk.gov.hmrc.http.HeaderCarrier
import views.html.manage.DueForDeletionReturnsView

import scala.concurrent.Future

class DueForDeletionReturnsControllerSpec
  extends SpecBase
    with MockitoSugar {

  trait Fixture {
    val mockService: StampDutyLandTaxService = mock[StampDutyLandTaxService]

    def app: Application =
      applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[StampDutyLandTaxService].toInstance(mockService)
        )
        .build()

    lazy val inProgressUrlSelector: Int => String =
      (inProgressIndex: Int) =>
        s"${controllers.manage.routes.DueForDeletionReturnsController.onPageLoad(Some(inProgressIndex), Some(1)).url}#in-progress"

    lazy val submittedUrlSelector: Int => String =
      (submittedIndex: Int) =>
        s"${controllers.manage.routes.DueForDeletionReturnsController.onPageLoad(Some(1), Some(submittedIndex)).url}#submitted"
  }

  "DueForDeletionReturnsController.onPageLoad" - {

    "must return OK and render the correct view when both services return empty lists and no indices are provided" in new Fixture {

      reset(mockService)

      when(mockService.getReturnsByTypeViewModel(any(), eqTo(IN_PROGRESS_RETURNS_DUE_FOR_DELETION), any())(any[HeaderCarrier]))
        .thenReturn(Future.successful(SdltReturnViewModel(
          extractType = IN_PROGRESS_RETURNS_DUE_FOR_DELETION, rows = List.empty,
          totalRowCount = Some(0)
        )))

      when(mockService.getReturnsByTypeViewModel(any(), eqTo(SUBMITTED_RETURNS_DUE_FOR_DELETION), any())(any[HeaderCarrier]))
        .thenReturn(Future.successful(SdltReturnViewModel(
          extractType = SUBMITTED_RETURNS_DUE_FOR_DELETION, rows = List.empty,
          totalRowCount = Some(0)
        )))

      running(app) {
        val request = FakeRequest(GET,
          controllers.manage.routes.DueForDeletionReturnsController.onPageLoad(None, None).url)

        val result = route(app, request).value

        status(result) mustEqual OK

        val view = app.injector.instanceOf[DueForDeletionReturnsView]

        val expectedInProgress =
          SdltReturnViewModel(extractType = IN_PROGRESS_RETURNS_DUE_FOR_DELETION,
            rows = List[SdltReturnViewRow](), totalRowCount = Some(0))
        val expectedSubmitted =
          SdltReturnViewModel(extractType = SUBMITTED_RETURNS_DUE_FOR_DELETION,
            rows = List[SdltReturnViewRow](), totalRowCount = Some(0))

        contentAsString(result) mustEqual
          view(expectedInProgress, expectedSubmitted, 1, 1, inProgressUrlSelector, submittedUrlSelector)(request, messages(app)).toString

        verify(mockService, times(1)).getReturnsByTypeViewModel(any(), eqTo(IN_PROGRESS_RETURNS_DUE_FOR_DELETION), any())(any())
        verify(mockService, times(1)).getReturnsByTypeViewModel(any(), eqTo(SUBMITTED_RETURNS_DUE_FOR_DELETION), any())(any())

      }
    }

    "must return OK and render the correct view when indices are provided but lists are empty" in new Fixture {
      reset(mockService)

      when(mockService.getReturnsByTypeViewModel(any(), eqTo(IN_PROGRESS_RETURNS_DUE_FOR_DELETION), any())(any[HeaderCarrier]))
        .thenReturn(Future.successful(SdltReturnViewModel(
          extractType = IN_PROGRESS_RETURNS_DUE_FOR_DELETION, rows = List.empty,
          totalRowCount = Some(0)
        )))

      when(mockService.getReturnsByTypeViewModel(any(), eqTo(SUBMITTED_RETURNS_DUE_FOR_DELETION), any())(any[HeaderCarrier]))
        .thenReturn(Future.successful(SdltReturnViewModel(
          extractType = SUBMITTED_RETURNS_DUE_FOR_DELETION, rows = List.empty,
          totalRowCount = Some(0)
        )))

      val inProgressIndex = Some(3)
      val submittedIndex = Some(2)

      running(app) {
        val request = FakeRequest(
          GET,
          controllers.manage.routes.DueForDeletionReturnsController
            .onPageLoad(inProgressIndex, submittedIndex)
            .url
        )

        val result = route(app, request).value

        status(result) mustEqual OK

        val view = app.injector.instanceOf[DueForDeletionReturnsView]

        val expectedInProgress =
          SdltReturnViewModel(extractType = IN_PROGRESS_RETURNS_DUE_FOR_DELETION, rows = List.empty, totalRowCount = Some(0))
        val expectedSubmitted =
          SdltReturnViewModel(extractType = SUBMITTED_RETURNS_DUE_FOR_DELETION, rows = List.empty, totalRowCount = Some(0))

        contentAsString(result) mustEqual
          view(expectedInProgress, expectedSubmitted, 1, 1, submittedUrlSelector, inProgressUrlSelector)(request, messages(app)).toString

        verify(mockService, times(1)).getReturnsByTypeViewModel(any(), eqTo(IN_PROGRESS_RETURNS_DUE_FOR_DELETION), any())(any())
        verify(mockService, times(1)).getReturnsByTypeViewModel(any(), eqTo(SUBMITTED_RETURNS_DUE_FOR_DELETION), any())(any())
      }
    }

    "must redirect to JourneyRecoveryController when getSubmittedReturnsDueForDeletion fails" in new Fixture {
      reset(mockService)

      when(mockService.getReturnsByTypeViewModel(any(), eqTo(IN_PROGRESS_RETURNS_DUE_FOR_DELETION), any())(any()))
        .thenReturn(Future.failed(new RuntimeException("boom-submitted")))

      when(mockService.getReturnsByTypeViewModel(any(), eqTo(SUBMITTED_RETURNS_DUE_FOR_DELETION), any())(any()))
        .thenReturn(Future.successful(List.empty[ReturnSummary]))

      running(app) {
        val request = FakeRequest(GET, controllers.manage.routes.DueForDeletionReturnsController.onPageLoad(None, None).url)

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.routes.SystemErrorController.onPageLoad().url

        verify(mockService, times(1)).getReturnsByTypeViewModel(any(), eqTo(IN_PROGRESS_RETURNS_DUE_FOR_DELETION), any())(any())
        verify(mockService, times(0)).getReturnsByTypeViewModel(any(), eqTo(SUBMITTED_RETURNS_DUE_FOR_DELETION), any())(any())
      }
    }

    "must redirect to JourneyRecoveryController when getInProgressReturnsDueForDeletion fails" in new Fixture {
      reset(mockService)

      when(mockService.getReturnsByTypeViewModel(any(), eqTo(IN_PROGRESS_RETURNS_DUE_FOR_DELETION), any())(any[HeaderCarrier]))
        .thenReturn(Future.successful(SdltReturnViewModel(
          extractType = IN_PROGRESS_RETURNS_DUE_FOR_DELETION, rows = List.empty,
          totalRowCount = Some(0)
        )))

      when(mockService.getReturnsByTypeViewModel(any(), eqTo(SUBMITTED_RETURNS_DUE_FOR_DELETION), any())(any()))
        .thenReturn(Future.failed(new RuntimeException("boom-in-progress")))


      running(app) {
        val request = FakeRequest(GET, controllers.manage.routes.DueForDeletionReturnsController.onPageLoad(None, None).url)

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.routes.SystemErrorController.onPageLoad().url

        verify(mockService, times(1)).getReturnsByTypeViewModel(any(), eqTo(IN_PROGRESS_RETURNS_DUE_FOR_DELETION), any())(any())
        verify(mockService, times(1)).getReturnsByTypeViewModel(any(), eqTo(SUBMITTED_RETURNS_DUE_FOR_DELETION), any())(any())
      }
    }

  }
}