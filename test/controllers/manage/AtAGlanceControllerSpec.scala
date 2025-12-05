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
import models.SdltReturnTypes.{IN_PROGRESS_RETURNS, IN_PROGRESS_RETURNS_DUE_FOR_DELETION, SUBMITTED_RETURNS_DUE_FOR_DELETION}
import models.manage.AtAGlanceViewModel
import models.responses.{SdltReturnViewModel, SdltReturnViewRow}
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import play.api.Application
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.StampDutyLandTaxService
import viewmodels.manage.{AgentDetailsViewModel, FeedbackViewModel, HelpAndContactViewModel, ReturnsManagementViewModel}
import views.html.manage.AtAGlanceView

import scala.concurrent.Future

class AtAGlanceControllerSpec
  extends SpecBase
    with MockitoSugar {

  trait Fixture {
    val mockService = mock[StampDutyLandTaxService]

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
      val returnsInProgressViewModel = SdltReturnViewModel(
        extractType = IN_PROGRESS_RETURNS,
        rows = List.empty,
        totalRowCount = Some(0)
      )

      val submittedReturns = Nil
      val submittedReturnsDueForDeletion = List.empty[SdltReturnViewRow]
      val inProgressReturnsDueForDeletion = List.empty[SdltReturnViewRow]
      val combinedDueForDeletion = (submittedReturnsDueForDeletion ++ inProgressReturnsDueForDeletion)
        .sortBy(_.purchaserName)

      when(mockService.getAgentCount(any(), any()))
        .thenReturn(Future.successful(agentsCount))

      when(mockService.getReturnsByTypeViewModel(any(), eqTo(IN_PROGRESS_RETURNS), any())(any()))
        .thenReturn(Future.successful(returnsInProgressViewModel))

      when(mockService.getSubmittedReturns(any())(any()))
        .thenReturn(Future.successful(List.empty))

      when(mockService.getReturnsByTypeViewModel(any(), eqTo(SUBMITTED_RETURNS_DUE_FOR_DELETION), any())(any()))
        .thenReturn(Future.successful(SdltReturnViewModel(
          extractType = SUBMITTED_RETURNS_DUE_FOR_DELETION, rows = submittedReturnsDueForDeletion,
          totalRowCount = Some(0)
        )))

      when(mockService.getReturnsByTypeViewModel(any(), eqTo(IN_PROGRESS_RETURNS_DUE_FOR_DELETION), any())(any()))
        .thenReturn(Future.successful(SdltReturnViewModel(
          extractType = IN_PROGRESS_RETURNS_DUE_FOR_DELETION, rows = inProgressReturnsDueForDeletion,
          totalRowCount = Some(0)
        )))

      running(application) {
        implicit val appConfig: FrontendAppConfig =
          application.injector.instanceOf[FrontendAppConfig]

        val request = FakeRequest(GET, AtAGlanceController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual OK

        val view = application.injector.instanceOf[AtAGlanceView]

        val expectedModel = AtAGlanceViewModel(
          storn = "STN001",
          name = "David Frank",
          returns = ReturnsManagementViewModel(
            inProgressReturnsCount = returnsInProgressViewModel.totalRowCount.getOrElse(0),
            inProgressReturnsUrl = InProgressReturnsController.onPageLoad(Some(1)).url,
            submittedReturnsCount = submittedReturns.length,
            submittedReturnsUrl = SubmittedReturnsController.onPageLoad(Some(1)).url,
            dueForDeletionReturnsCount = combinedDueForDeletion.length,
            dueForDeletionUrl = DueForDeletionReturnsController.onPageLoad(Some(1), Some(1)).url,
            startReturnUrl = "#"
          ),
          agentDetails = AgentDetailsViewModel(
            agentsCount = agentsCount,
            agentsUrl = appConfig.agentOverviewUrl,
            addAgentUrl = appConfig.startAddAgentUrl
          ),
          helpAndContact = HelpAndContactViewModel(
            helpUrl = "#",
            contactUrl = "#",
            howToPayUrl = appConfig.howToPayUrl,
            usefulLinksUrl = "#"
          ),
          feedback = FeedbackViewModel(
            feedbackUrl = appConfig.exitSurveyUrl
          )
        )

        contentAsString(result) mustEqual
          view(expectedModel)(request, messages(application)).toString

        verify(mockService, times(1)).getAgentCount(any(), any())
        verify(mockService, times(1)).getReturnsByTypeViewModel(any(), eqTo(IN_PROGRESS_RETURNS), any())(any())
        verify(mockService, times(1)).getSubmittedReturns(any())(any())
        verify(mockService, times(1)).getReturnsByTypeViewModel(any(), eqTo(SUBMITTED_RETURNS_DUE_FOR_DELETION), any())(any())
        verify(mockService, times(1)).getReturnsByTypeViewModel(any(), eqTo(IN_PROGRESS_RETURNS_DUE_FOR_DELETION), any())(any())
      }
    }

//    "must redirect to JourneyRecoveryController when any service call fails (e.g. getAgentCount)" in new Fixture {
//      reset(mockService)
//
//      when(mockService.getAgentCount(any(), any()))
//        .thenReturn(Future.failed(new RuntimeException("boom-agents")))
//
//      when(mockService.getReturnsByTypeViewModel(any(), any(), any())(any()))
//        .thenReturn(Future.successful(Nil))
//
//      when(mockService.getSubmittedReturns(any())(any()))
//        .thenReturn(Future.successful(Nil))
//
//      when(mockService.getReturnsByTypeViewModel(any(), any(), any())(any()))
//        .thenReturn(Future.successful(Nil))
//
//      when(mockService.getInProgressReturnsDueForDeletion(any())(any()))
//        .thenReturn(Future.successful(Nil))
//
//      val app = application
//
//      running(app) {
//        val request = FakeRequest(GET, AtAGlanceController.onPageLoad().url)
//
//        val result = route(app, request).value
//
//        status(result) mustEqual SEE_OTHER
//        redirectLocation(result).value mustEqual
//          controllers.routes.JourneyRecoveryController.onPageLoad().url
//
//        verify(mockService, times(1)).getAgentCount(any(), any())
//        verify(mockService, times(0)).getReturnsByTypeViewModel(any(), any(), any())(any())
//        verify(mockService, times(0)).getSubmittedReturns(any())(any())
//        verify(mockService, times(0)).getReturnsByTypeViewModel(any(), any(), any())(any())
//        verify(mockService, times(0)).getInProgressReturnsDueForDeletion(any())(any())
//      }
//    }

  }
}