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
import connectors.RateLimitedAllowListConnector
import controllers.actions.IdentifierAction
import models.UserAnswers
import models.requests.IdentifierRequest
import org.mockito.ArgumentMatchers.{any, argThat}
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.{AnyContent, BodyParser, Request, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import uk.gov.hmrc.auth.core.AffinityGroup

import scala.concurrent.{ExecutionContext, Future}

class IndexControllerSpec extends SpecBase with MockitoSugar {

  "Index Controller" - {

    "onPageLoad" - {
      s"must redirect to the Landing page with the userId added to the session" in {

        val mockSessionRepository = mock[SessionRepository]
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

        val application = applicationBuilder(userAnswers = None)
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.IndexController.onPageLoad().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.manage.routes.AtAGlanceController.onPageLoad().url
          verify(mockSessionRepository).set(any[UserAnswers])
        }
      }

      "must update UserAnswers model with the userId pulled from the request" in {

        val mockSessionRepository = mock[SessionRepository]
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

        val application = applicationBuilder(userAnswers = None)
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.IndexController.onPageLoad().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          val captor = org.mockito.ArgumentCaptor.forClass(classOf[UserAnswers])
          verify(mockSessionRepository).set(captor.capture())

          val savedUserAnswers = captor.getValue
          savedUserAnswers.id must not be empty

          verify(mockSessionRepository).set(argThat((ua: UserAnswers) =>
            ua.id.contains(savedUserAnswers.id)
          ))
        }
      }

      "redirect Organisation users to the organisation dashboard if trafic split is enabled and user is allowed" in {
        redirectWithTrafficSplit(AffinityGroup.Organisation, isUserAllowed = true) mustEqual
          controllers.manage.routes.AtAGlanceController.onPageLoad().url
      }

      "redirect Agent users to the agent dashboard if trafic split is enabled and user is allowed" in {
        redirectWithTrafficSplit(AffinityGroup.Agent, isUserAllowed = true) mustEqual
          controllers.manage.routes.AtAGlanceController.onPageLoad().url
      }

      "redirect Organisation users to the organisation dashboard if trafic split is enabled and user is not allowed" in {
        redirectWithTrafficSplit(AffinityGroup.Organisation, isUserAllowed = false) mustEqual
          "http://localhost:9020/stamp-taxes-legacy/org/STN001"
      }

      "redirect Agent users to the agent dashboard if trafic split is enabled and user is not allowed" in {
        redirectWithTrafficSplit(AffinityGroup.Agent, isUserAllowed = false) mustEqual
          "http://localhost:9020/stamp-taxes-legacy/agent/STN001"
      }
    }
  }

  private def redirectWithTrafficSplit(affinityGroup: AffinityGroup, isUserAllowed: Boolean): String = {
    val mockSessionRepository             = mock[SessionRepository]
    val mockRateLimitedAllowListConnector = mock[RateLimitedAllowListConnector]

    when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
    when(mockRateLimitedAllowListConnector.checkAllowList(any(), any())(using any()))
      .thenReturn(Future.successful(isUserAllowed))

    val application = new GuiceApplicationBuilder()
      .configure(
        "splitter.trafficSplitEnabled" -> true,
        "splitter.allowListName"       -> "beta-test",
        "urls.legacySdltServiceUrl"    -> "http://localhost:9020/stamp-taxes-legacy"
      )
      .overrides(
        bind[SessionRepository].toInstance(mockSessionRepository),
        bind[RateLimitedAllowListConnector].toInstance(mockRateLimitedAllowListConnector),
        bind[IdentifierAction].toInstance(identifierAction(affinityGroup))
      )
      .build()

    running(application) {
      val request = FakeRequest(GET, routes.IndexController.onPageLoad().url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value
    }
  }

  private def identifierAction(affinityGroup: AffinityGroup): IdentifierAction =
    new IdentifierAction {
      override def invokeBlock[A](request: Request[A], block: IdentifierRequest[A] => Future[Result]): Future[Result] =
        block(IdentifierRequest(request, "id", "STN001", affinityGroup))

      override def parser: BodyParser[AnyContent] = stubBodyParser()

      override protected def executionContext: ExecutionContext = ExecutionContext.global
    }
}
