/*
 * Copyright 2026 HM Revenue & Customs
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

package controllers.home

import base.SpecBase
import config.FrontendAppConfig
import connectors.RateLimitedAllowListConnector
import controllers.actions.{IdentifierAction, SplitterAction}
import models.requests.IdentifierRequest
import play.api.Configuration
import play.api.mvc.*
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.auth.core.AffinityGroup
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class HomeControllerSpec extends SpecBase {

  private val cc: MessagesControllerComponents = stubMessagesControllerComponents()

  private val application = applicationBuilder().build()

  private val request: FakeRequest[AnyContent] =
    FakeRequest(GET, "/")

  private def appConfig(useRateLimitedAllowList: Boolean): FrontendAppConfig =
    new FrontendAppConfig(
      Configuration
        .from(
          Map(
            "splitter.trafficSplitEnabled" -> useRateLimitedAllowList,
            "splitter.allowListName"       -> "beta-test",
            "urls.legacySdltServiceUrl"    -> "http://localhost:9020/stamp-taxes"
          )
        )
        .withFallback(application.configuration)
    )

  "HomeController landingPage" - {

    "redirect Organisation users to the organisation dashboard" in {
      val result = controller(AffinityGroup.Organisation, useRateLimitedAllowList = false, isUserAllowed = None).landingPage("")(request)

      status(result) mustBe SEE_OTHER
      redirectLocation(result).value mustBe
        controllers.routes.IndexController.onPageLoad().url
    }

    "redirect Agent users to the agent dashboard" in {
      val result = controller(AffinityGroup.Agent, useRateLimitedAllowList = false, isUserAllowed = None).landingPage("")(request)

      status(result) mustBe SEE_OTHER
      redirectLocation(result).value mustBe
        controllers.routes.IndexController.onPageLoad().url
    }

    "redirect Organisation users to the organisation dashboard if trafic split is enabled and user is allowed" in {
      val result = controller(AffinityGroup.Organisation, useRateLimitedAllowList = true, isUserAllowed = Some(_ => true)).landingPage("")(request)

      status(result) mustBe SEE_OTHER
      redirectLocation(result).value mustBe
        controllers.routes.IndexController.onPageLoad().url
    }

    "redirect Agent users to the agent dashboard if trafic split is enabled and user is allowed" in {
      val result = controller(AffinityGroup.Agent, useRateLimitedAllowList = true, isUserAllowed = Some(_ => true)).landingPage("")(request)

      status(result) mustBe SEE_OTHER
      redirectLocation(result).value mustBe
        controllers.routes.IndexController.onPageLoad().url
    }

    "redirect Organisation users to the organisation dashboard if trafic split is enabled and user is not allowed" in {
      val result = controller(AffinityGroup.Organisation, useRateLimitedAllowList = true, isUserAllowed = Some(_ => false)).landingPage("")(request)

      status(result) mustBe SEE_OTHER
      redirectLocation(result).value mustBe "http://localhost:9020/stamp-taxes/org/test-user-123"
    }

    "redirect Agent users to the agent dashboard if trafic split is enabled and user is not allowed" in {
      val result = controller(AffinityGroup.Agent, useRateLimitedAllowList = true, isUserAllowed = Some(_ => false)).landingPage("")(request)

      status(result) mustBe SEE_OTHER
      redirectLocation(result).value mustBe "http://localhost:9020/stamp-taxes/agent/test-agent-id"
    }

    "redirect to the index page when the stamp-taxes root url is requested" in {
      val app = applicationBuilder().build()

      running(app) {
        Seq("/stamp-taxes", "/stamp-taxes/", "/stamp-taxes?lang=eng").foreach { url =>
          val result = route(app, FakeRequest(GET, url)).value

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe controllers.routes.IndexController.onPageLoad().url
        }
      }
    }
  }

  private def controller(affinityGroup: AffinityGroup, useRateLimitedAllowList: Boolean, isUserAllowed: Option[String => Boolean]): HomeController =
    new HomeController(
      cc,
      identifierAction(affinityGroup),
      new SplitterAction(
        appConfig(useRateLimitedAllowList),
        new RateLimitedAllowListConnector {
          override def checkAllowList(feature: String, sdltReference: String)(using hc: HeaderCarrier): Future[Boolean] =
            Future.successful(isUserAllowed.map(_(sdltReference)).getOrElse(false))
        }
      )(using cc.executionContext)
    )

  private def identifierAction(affinityGroup: AffinityGroup): IdentifierAction =
    new IdentifierAction {
      private val storn = affinityGroup match {
        case AffinityGroup.Agent        => "test-agent-id"
        case AffinityGroup.Organisation => "test-user-123"
        case _                          => "test-individual-id"
      }

      override def invokeBlock[A](request: Request[A], block: IdentifierRequest[A] => Future[Result]): Future[Result] =
        block(IdentifierRequest(request, "id", storn, affinityGroup))

      override def parser: BodyParser[AnyContent] = cc.parsers.defaultBodyParser

      override protected def executionContext: ExecutionContext = cc.executionContext
    }
}
