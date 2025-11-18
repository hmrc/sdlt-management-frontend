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
import models.manage.ReturnSummary
import models.manageAgents.AgentDetailsResponse
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

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}
import AtAGlanceController.*
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.retrieve.Name

class AtAGlanceControllerSpec extends SpecBase with MockitoSugar {

  trait Fixture {

    val mockService: StampDutyLandTaxService = mock[StampDutyLandTaxService]
    val mockAuthConnector: AuthConnector = mock[AuthConnector]

    val application: Application =
      applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[StampDutyLandTaxService].toInstance(mockService))
        .build()

    implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

    val atAGlanceUrl: String = controllers.manage.routes.AtAGlanceController.onPageLoad().url

    val expectedAgentData: List[AgentDetailsResponse] =
      (0 to 3).toList.map(index =>
        AgentDetailsResponse(
          agentName =             "John Doe",
          addressLine1 =          "Oak Lane",
          addressLine2 =          None,
          addressLine3 =          "London",
          addressLine4 =          None,
          postcode =              None,
          phone =                 None,
          email =                 "john.doe@example.com",
          agentReferenceNumber =  "12345"
        )
      )

    val expectedReturnsManagementData: List[ReturnSummary] =
      (0 to 7).toList.map(index =>
        ReturnSummary(
          returnReference = "RETREF003",
          utrn = "UTRN003",
          status = "ACCEPTED",
          dateSubmitted = LocalDate.parse("2025-04-05"),
          purchaserName = "Brown",
          address = s"$index Riverside Drive",
          agentReference = "B4C72F7T3"
        )
      )
  }

  "At A Glance Controller" - {

    "must return OK and the correct view for a GET with no data" in new Fixture {

      when(mockAuthConnector.authorise(any(), any())(any[HeaderCarrier], any[ExecutionContext]))
        .thenReturn(Future.successful(Some(Name(Some("TestUser"), None))))

      when(mockService.getAllAgents(any[String])(any[HeaderCarrier]))
        .thenReturn(Future.successful(Nil))

      when(mockService.getReturn(any[String], any[String])(any[HeaderCarrier]))
        .thenReturn(Future.successful(Nil))

      running(application) {

        val request = FakeRequest(GET, atAGlanceUrl)
        val result = route(application, request).value

        val view = application.injector.instanceOf[AtAGlanceView]
        val expected = view(
          storn = "STN001",
          maybeName = None,
          returnsManagementViewModel(0, 0, 0),
          agentDetailsViewModel(0, appConfig),
          helpAndContactViewModel(appConfig),
          feedbackViewModel(appConfig.exitSurveyUrl)
        )(request, messages(application)).toString

        status(result) mustEqual OK
        contentAsString(result) mustEqual expected
      }
    }

    "must return OK and the correct view for a GET with data" in new Fixture {

      when(mockAuthConnector.authorise(any(), any())(any[HeaderCarrier], any[ExecutionContext]))
        .thenReturn(Future.successful(Some("David Frank")))

      when(mockService.getAllAgents(any[String])(any[HeaderCarrier]))
        .thenReturn(Future.successful(expectedAgentData))

      when(mockService.getReturn(any[String], any[String])(any[HeaderCarrier]))
        .thenReturn(Future.successful(expectedReturnsManagementData))

      running(application) {

        val request = FakeRequest(GET, atAGlanceUrl)
        val result = route(application, request).value

        val view = application.injector.instanceOf[AtAGlanceView]
        val expected = view(
          storn = "STN001",
          maybeName = Some("David Frank"),
          returnsManagementViewModel(
            expectedReturnsManagementData.size,
            expectedReturnsManagementData.size,
            expectedReturnsManagementData.size
          ),
          agentDetailsViewModel(expectedAgentData.size, appConfig),
          helpAndContactViewModel(appConfig),
          feedbackViewModel(appConfig.exitSurveyUrl)
        )(request, messages(application)).toString

        status(result) mustEqual OK
        contentAsString(result) mustEqual expected
      }
    }

    "must redirect to Journey Recovery for a GET if there is a service level error" in new Fixture {

      when(mockService.getAllAgents(any[String])(any[HeaderCarrier]))
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
