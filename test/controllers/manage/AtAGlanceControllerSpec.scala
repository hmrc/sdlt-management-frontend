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
import controllers.manage.routes.*
import models.SdltReturnTypes.{IN_PROGRESS_RETURNS, IN_PROGRESS_RETURNS_DUE_FOR_DELETION, SUBMITTED_RETURNS_DUE_FOR_DELETION, SUBMITTED_SUBMITTED_RETURNS}
import models.manage.AtAGlanceViewModel
import models.responses.{SdltInProgressDueForDeletionReturnViewModel, SdltInProgressReturnViewModel, SdltReturnViewRow, SdltSubmittedDueForDeletionReturnViewModel, SdltSubmittedReturnViewModel}
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import play.api.Application
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.StampDutyLandTaxService
import views.html.manage.AtAGlanceView
import controllers.routes.{SystemErrorController}
import scala.concurrent.Future

class AtAGlanceControllerSpec
  extends SpecBase
    with MockitoSugar {

  trait Fixture {
    val mockService: StampDutyLandTaxService = mock[StampDutyLandTaxService]

    def application: Application =
      applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[StampDutyLandTaxService].toInstance(mockService)
        )
        .build()
  }

  "AtAGlanceController.onPageLoad" - {

    "must return OK and render the correct view when all service calls succeed" in new Fixture {
      reset(mockService)

      val agentsCount = 0

      val inProgressRows: List[SdltReturnViewRow] = Nil
      val returnsInProgressViewModel = SdltInProgressReturnViewModel(
        extractType    = IN_PROGRESS_RETURNS,
        rows           = inProgressRows,
        totalRowCount  = inProgressRows.length,
        selectedPageIndex = 1
      )

      val submittedRows: List[SdltReturnViewRow] = Nil
      val submittedViewModel = SdltSubmittedReturnViewModel(
        extractType    = SUBMITTED_SUBMITTED_RETURNS,
        rows           = submittedRows,
        totalRowCount  = submittedRows.length,
        selectedPageIndex = 1
      )

      val submittedDueRows: List[SdltReturnViewRow] = Nil
      val inProgressDueRows: List[SdltReturnViewRow] = Nil

      val submittedDueVm = SdltSubmittedDueForDeletionReturnViewModel(
        extractType    = SUBMITTED_RETURNS_DUE_FOR_DELETION,
        rows           = submittedDueRows,
        totalRowCount  = submittedDueRows.length
      )

      val inProgressDueVm = SdltInProgressDueForDeletionReturnViewModel(
        extractType    = IN_PROGRESS_RETURNS_DUE_FOR_DELETION,
        rows           = inProgressDueRows,
        totalRowCount  = inProgressDueRows.length
      )

      val combinedDueForDeletionRows: List[SdltReturnViewRow] =
        (submittedDueVm.rows ++ inProgressDueVm.rows).sortBy(_.purchaserName)

      when(mockService.getAgentCount(any(), any()))
        .thenReturn(Future.successful(agentsCount))

      when(mockService.getReturnsByTypeViewModel(any(), eqTo(IN_PROGRESS_RETURNS), any())(any()))
        .thenReturn(Future.successful(returnsInProgressViewModel))

      when(mockService.getReturnsByTypeViewModel(any(), eqTo(SUBMITTED_SUBMITTED_RETURNS), any())(any()))
        .thenReturn(Future.successful(submittedViewModel))

      when(mockService.getReturnsByTypeViewModel(any(), eqTo(SUBMITTED_RETURNS_DUE_FOR_DELETION), any())(any()))
        .thenReturn(Future.successful(submittedDueVm))

      when(mockService.getReturnsByTypeViewModel(any(), eqTo(IN_PROGRESS_RETURNS_DUE_FOR_DELETION), any())(any()))
        .thenReturn(Future.successful(inProgressDueVm))

      running(application) {
        implicit val appConfig: FrontendAppConfig =
          application.injector.instanceOf[FrontendAppConfig]

        val request = FakeRequest(GET, AtAGlanceController.onPageLoad().url)
        val result  = route(application, request).value

        status(result) mustEqual OK

        val view = application.injector.instanceOf[AtAGlanceView]

        val expectedModel = AtAGlanceViewModel(
          inProgressReturns       = returnsInProgressViewModel,
          submittedReturns        = submittedViewModel,
          dueForDeletionReturnsTotal   = combinedDueForDeletionRows.length,
          agentsCount             = agentsCount,
          storn                   = "STN001",
          name                    = "John Doe"
        )

        contentAsString(result) mustEqual
          view(expectedModel)(request, messages(application)).toString

        verify(mockService, times(1)).getAgentCount(any(), any())
        verify(mockService, times(1)).getReturnsByTypeViewModel(any(), eqTo(IN_PROGRESS_RETURNS), any())(any())
        verify(mockService, times(1)).getReturnsByTypeViewModel(any(), eqTo(SUBMITTED_SUBMITTED_RETURNS), any())(any())
        verify(mockService, times(1)).getReturnsByTypeViewModel(any(), eqTo(SUBMITTED_RETURNS_DUE_FOR_DELETION), any())(any())
        verify(mockService, times(1)).getReturnsByTypeViewModel(any(), eqTo(IN_PROGRESS_RETURNS_DUE_FOR_DELETION), any())(any())
      }
    }

    "must redirect to JourneyRecoveryController when any service call fails (e.g. getAgentCount)" in new Fixture {
      reset(mockService)

      when(mockService.getAgentCount(any(), any()))
        .thenReturn(Future.failed(new RuntimeException("boom-agents")))

      val app = application

      running(app) {
        val request = FakeRequest(GET, AtAGlanceController.onPageLoad().url)
        val result  = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          SystemErrorController.onPageLoad().url

        verify(mockService, times(1)).getAgentCount(any(), any())
        verify(mockService, times(0)).getReturnsByTypeViewModel(any(), eqTo(IN_PROGRESS_RETURNS), any())(any())
        verify(mockService, times(0)).getReturnsByTypeViewModel(any(), eqTo(SUBMITTED_SUBMITTED_RETURNS), any())(any())
        verify(mockService, times(0)).getReturnsByTypeViewModel(any(), eqTo(SUBMITTED_RETURNS_DUE_FOR_DELETION), any())(any())
        verify(mockService, times(0)).getReturnsByTypeViewModel(any(), eqTo(IN_PROGRESS_RETURNS_DUE_FOR_DELETION), any())(any())
      }
    }
  }
}
