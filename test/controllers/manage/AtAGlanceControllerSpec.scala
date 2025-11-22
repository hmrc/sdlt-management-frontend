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
import controllers.manage.routes.{DueForDeletionController, InProgressReturnsController, SubmittedReturnsController}
import models.manage.AtAGlanceViewModel
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.StampDutyLandTaxService
import views.html.manage.AtAGlanceView
import play.api.inject.bind
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.Application
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future
import models.responses.{SdltInProgressReturnViewRow, UniversalStatus}
import viewmodels.manage.{AgentDetailsViewModel, FeedbackViewModel, HelpAndContactViewModel, ReturnsManagementViewModel, SdltSubmittedReturnsViewModel}
import models.requests.DataRequest
import models.responses.UniversalStatus.{STARTED, SUBMITTED}

class AtAGlanceControllerSpec extends SpecBase with MockitoSugar {

  trait Fixture {

    val mockService: StampDutyLandTaxService = mock[StampDutyLandTaxService]

    val application: Application =
      applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[StampDutyLandTaxService].toInstance(mockService))
        .build()

    implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

    val atAGlanceUrl: String = controllers.manage.routes.AtAGlanceController.onPageLoad().url

  }

  "At A Glance Controller" - {

    "must return OK and the correct view for a GET with no data" in new Fixture {

      when(mockService.getAgentCount(any[HeaderCarrier], any[DataRequest[_]]))
        .thenReturn(Future.successful(0))

      when(mockService.getInProgressReturns(any[HeaderCarrier], any[DataRequest[_]]))
        .thenReturn(Future.successful(Nil))

      when(mockService.getSubmittedReturns(any[HeaderCarrier], any[DataRequest[_]]))
        .thenReturn(Future.successful(Nil))

      when(mockService.getReturnsDueForDeletion(any[HeaderCarrier], any[DataRequest[_]]))
        .thenReturn(Future.successful(Nil))

      running(application) {

        val request = FakeRequest(GET, atAGlanceUrl)
        val result = route(application, request).value

        val view = application.injector.instanceOf[AtAGlanceView]

        val expected: String = view(AtAGlanceViewModel(
          storn = "STN001",
          name = "David Frank",
          returns = ReturnsManagementViewModel(
            inProgressReturnsCount = 0,
            inProgressReturnsUrl = InProgressReturnsController.onPageLoad(Some(1)).url,
            submittedReturnsCount = 0,
            submittedReturnsUrl = SubmittedReturnsController.onPageLoad(Some(1)).url,
            dueForDeletionReturnsCount = 0,
            dueForDeletionUrl = DueForDeletionController.onPageLoad().url,
            startReturnUrl = "#"
          ),
          agentDetails = AgentDetailsViewModel(
            agentsCount = 0,
            agentsUrl = appConfig.agentOverviewUrl,
            addAgentUrl = appConfig.startAddAgentUrl
          ),
          helpAndContact = HelpAndContactViewModel(
            helpUrl = "#",
            contactUrl = "#",
            howToPayUrl = appConfig.howToPayUrl,
            usefulLinksUrl = "#"
          ),
          feedback = FeedbackViewModel(feedbackUrl = appConfig.exitSurveyUrl)
        ))(request, messages(application)).toString

        status(result) mustEqual OK
        contentAsString(result) mustEqual expected
      }
    }

    "must return OK and the correct view for a GET with data" in new Fixture {

      when(mockService.getAgentCount(any[HeaderCarrier], any[DataRequest[_]]))
        .thenReturn(Future.successful(4))

      when(mockService.getSubmittedReturns(any[HeaderCarrier], any[DataRequest[_]]))
        .thenReturn(Future.successful(List(SdltSubmittedReturnsViewModel(address = "10 Downing Street", utrn = "XA1243523", purchaserName = "John Doe", status = SUBMITTED))))

      when(mockService.getReturnsDueForDeletion(any[HeaderCarrier], any[DataRequest[_]]))
        .thenReturn(Future.successful(Nil))

      when(mockService.getInProgressReturns(any[HeaderCarrier], any[DataRequest[_]]))
        .thenReturn(Future.successful(List(SdltInProgressReturnViewRow(address = "10 Downing Street", agentReference = "ARN0001", purchaserName = "Joe Bloggs", status = STARTED))))

      running(application) {

        val request = FakeRequest(GET, atAGlanceUrl)
        val result = route(application, request).value

        val view = application.injector.instanceOf[AtAGlanceView]

        val expected: String = view(AtAGlanceViewModel(
          storn = "STN001",
          name = "David Frank",
          returns = ReturnsManagementViewModel(
            inProgressReturnsCount = 1,
            inProgressReturnsUrl = InProgressReturnsController.onPageLoad(Some(1)).url,
            submittedReturnsCount = 1,
            submittedReturnsUrl = SubmittedReturnsController.onPageLoad(Some(1)).url,
            dueForDeletionReturnsCount = 0,
            dueForDeletionUrl = DueForDeletionController.onPageLoad().url,
            startReturnUrl = "#"
          ),
          agentDetails = AgentDetailsViewModel(
            agentsCount = 4,
            agentsUrl = appConfig.agentOverviewUrl,
            addAgentUrl = appConfig.startAddAgentUrl
          ),
          helpAndContact = HelpAndContactViewModel(
            helpUrl = "#",
            contactUrl = "#",
            howToPayUrl = appConfig.howToPayUrl,
            usefulLinksUrl = "#"
          ),
          feedback = FeedbackViewModel(feedbackUrl = appConfig.exitSurveyUrl)
        ))(request, messages(application)).toString

        status(result) mustEqual OK
        contentAsString(result) mustEqual expected
      }
    }

    "must redirect to Journey Recovery for a GET if there is a service level error" in new Fixture {

      when(mockService.getAgentCount(any[HeaderCarrier], any[DataRequest[_]]))
        .thenReturn(Future.successful(new Error("Test error")))

      when(mockService.getSubmittedReturns(any[HeaderCarrier], any[DataRequest[_]]))
        .thenReturn(Future.successful(new Error("Test error")))

      when(mockService.getReturnsDueForDeletion(any[HeaderCarrier], any[DataRequest[_]]))
        .thenReturn(Future.successful(new Error("Test error")))

      when(mockService.getInProgressReturns(any[HeaderCarrier], any[DataRequest[_]]))
        .thenReturn(Future.successful(new Error("Test error")))

      running(application) {

        val request = FakeRequest(GET, atAGlanceUrl)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
