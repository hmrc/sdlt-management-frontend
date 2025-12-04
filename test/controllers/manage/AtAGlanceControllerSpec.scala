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
import models.manage.{AtAGlanceViewModel, ReturnSummary}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import play.api.Application
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.StampDutyLandTaxService
import viewmodels.manage.{AgentDetailsViewModel, FeedbackViewModel, HelpAndContactViewModel, ReturnsManagementViewModel}
import views.html.manage.AtAGlanceView
import controllers.manage.routes.*
import models.SdltReturnTypes.IN_PROGRESS_RETURNS
import models.responses.{SdltReturnViewModel}

import scala.concurrent.Future

class AtAGlanceControllerSpec
  extends SpecBase
    with MockitoSugar {

  private val mockService = mock[StampDutyLandTaxService]

  private def application: Application =
    applicationBuilder(userAnswers = Some(emptyUserAnswers))
      .overrides(
        bind[StampDutyLandTaxService].toInstance(mockService)
      )
      .build()

  private val routeUrl =
    AtAGlanceController.onPageLoad().url

  "AtAGlanceController.onPageLoad" - {

    "must return OK and render the correct view when all service calls succeed" in {
      reset(mockService)

      val agentsCount = 0
      val returnsInProgressViewModel = SdltReturnViewModel(
        extractType = IN_PROGRESS_RETURNS,
        rows = List.empty,
        totalRowCount = Some(0)
      )

      val submittedReturns = Nil
      val submittedReturnsDueForDeletion = List.empty[ReturnSummary]
      val inProgressReturnsDueForDeletion = List.empty[ReturnSummary]
      val combinedDueForDeletion = (submittedReturnsDueForDeletion ++ inProgressReturnsDueForDeletion)
        .sortBy(_.purchaserName)

      when(mockService.getAgentCount(any(), any()))
        .thenReturn(Future.successful(agentsCount))

      when(mockService.getReturns(any(), any(), any())(any()))
        .thenReturn(Future.successful(returnsInProgressViewModel))

      when(mockService.getSubmittedReturns(any())(any()))
        .thenReturn(Future.successful(submittedReturns))

      when(mockService.getSubmittedReturnsDueForDeletion(any())(any()))
        .thenReturn(Future.successful(submittedReturnsDueForDeletion))

      when(mockService.getInProgressReturnsDueForDeletion(any())(any()))
        .thenReturn(Future.successful(inProgressReturnsDueForDeletion))

      val app = application

      running(app) {
        implicit val appConfig: FrontendAppConfig =
          app.injector.instanceOf[FrontendAppConfig]

        val request = FakeRequest(GET, routeUrl)

        val result = route(app, request).value

        status(result) mustEqual OK

        val view = app.injector.instanceOf[AtAGlanceView]

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
          view(expectedModel)(request, messages(app)).toString

        verify(mockService, times(1)).getAgentCount(any(), any())
        verify(mockService, times(1)).getReturns(any(), any(), any())(any())
        verify(mockService, times(1)).getSubmittedReturns(any())(any())
        verify(mockService, times(1)).getSubmittedReturnsDueForDeletion(any())(any())
        verify(mockService, times(1)).getInProgressReturnsDueForDeletion(any())(any())
      }
    }

    "must redirect to JourneyRecoveryController when any service call fails (e.g. getAgentCount)" in {
      reset(mockService)

      when(mockService.getAgentCount(any(), any()))
        .thenReturn(Future.failed(new RuntimeException("boom-agents")))

      when(mockService.getReturns(any(), any(), any())(any()))
        .thenReturn(Future.successful(Nil))

      when(mockService.getSubmittedReturns(any())(any()))
        .thenReturn(Future.successful(Nil))

      when(mockService.getSubmittedReturnsDueForDeletion(any())(any()))
        .thenReturn(Future.successful(Nil))

      when(mockService.getInProgressReturnsDueForDeletion(any())(any()))
        .thenReturn(Future.successful(Nil))

      val app = application

      running(app) {
        val request = FakeRequest(GET, routeUrl)

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.routes.JourneyRecoveryController.onPageLoad().url

        verify(mockService, times(1)).getAgentCount(any(), any())
        verify(mockService, times(0)).getReturns(any(), any(), any())(any())
        verify(mockService, times(0)).getSubmittedReturns(any())(any())
        verify(mockService, times(0)).getSubmittedReturnsDueForDeletion(any())(any())
        verify(mockService, times(0)).getInProgressReturnsDueForDeletion(any())(any())
      }
    }

  }
}