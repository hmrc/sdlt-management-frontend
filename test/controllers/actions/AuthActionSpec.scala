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

package controllers.actions

import base.SpecBase
import config.FrontendAppConfig
import controllers.actions.TestAuthRetrievals.Ops
import controllers.routes
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.Application
import play.api.inject.bind
import play.api.mvc.{Action, AnyContent, BodyParsers, Results}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.auth.core.*
import uk.gov.hmrc.auth.core.AffinityGroup.{Agent, Individual, Organisation}
import uk.gov.hmrc.auth.core.retrieve.~

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class AuthActionSpec extends SpecBase {

  trait Fixture {
    val mockAuthConnector: AuthConnector = mock[AuthConnector]

    val application: Application = applicationBuilder(userAnswers = None)
      .overrides(bind[AuthConnector].toInstance(mockAuthConnector))
      .build()

    val bodyParsers: BodyParsers.Default = application.injector.instanceOf[BodyParsers.Default]
    val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

    val emptyEnrolments = Enrolments(Set.empty)

    val orgEnrollment: Enrolment = Enrolment(
      "IR-SDLT-ORG",
      Seq(
        EnrolmentIdentifier("STORN", testStorn)
      ),
      "activated",
      None
    )

    // TODO: clarify enrollments details for agent
    val agentEnrollment: Enrolment = Enrolment(
      "IR-SDLT-AGENT",
      Seq(
        EnrolmentIdentifier("STORN", testStorn)
      ),
      "activated",
      None
    )

    val id: String = UUID.randomUUID().toString
    val testStorn: String = "STN001"
  }

  type RetrievalsType = Option[String] ~ Enrolments ~ Option[AffinityGroup] ~ Option[CredentialRole]

  class Harness(authAction: IdentifierAction) {
    def onPageLoad(): Action[AnyContent] = authAction(_ => Results.Ok)
  }

  "Authentication Action" - {

    "when the user hasn't logged in" - {
      "must redirect the user to log in " in new Fixture {
        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig = application.injector.instanceOf[FrontendAppConfig]

          val authAction = new AuthenticatedIdentifierAction(new FakeFailingAuthConnector(new MissingBearerToken), appConfig, bodyParsers)
          val controller = new Harness(authAction)
          val result = controller.onPageLoad()(FakeRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value must startWith(appConfig.loginUrl)
        }
      }
    }

    "the user's session has expired" - {
      "must redirect the user to log in " in new Fixture {
        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig = application.injector.instanceOf[FrontendAppConfig]

          val authAction = new AuthenticatedIdentifierAction(new FakeFailingAuthConnector(new BearerTokenExpired), appConfig, bodyParsers)
          val controller = new Harness(authAction)
          val result = controller.onPageLoad()(FakeRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value must startWith(appConfig.loginUrl)
        }
      }
    }

    "the user doesn't have sufficient enrolments" - {

      "must redirect the user to the unauthorised page" in new Fixture {

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig = application.injector.instanceOf[FrontendAppConfig]

          val authAction = new AuthenticatedIdentifierAction(new FakeFailingAuthConnector(new InsufficientEnrolments), appConfig, bodyParsers)
          val controller = new Harness(authAction)
          val result = controller.onPageLoad()(FakeRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe routes.UnauthorisedController.onPageLoad().url
        }
      }
    }

    "the user doesn't have sufficient confidence level" - {

      "must redirect the user to the unauthorised page" in new Fixture {

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig = application.injector.instanceOf[FrontendAppConfig]

          val authAction = new AuthenticatedIdentifierAction(new FakeFailingAuthConnector(new InsufficientConfidenceLevel), appConfig, bodyParsers)
          val controller = new Harness(authAction)
          val result = controller.onPageLoad()(FakeRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe routes.UnauthorisedController.onPageLoad().url
        }
      }
    }

    "the user used an unaccepted auth provider" - {

      "must redirect the user to the unauthorised page" in new Fixture {

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig = application.injector.instanceOf[FrontendAppConfig]

          val authAction = new AuthenticatedIdentifierAction(new FakeFailingAuthConnector(new UnsupportedAuthProvider), appConfig, bodyParsers)
          val controller = new Harness(authAction)
          val result = controller.onPageLoad()(FakeRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe routes.UnauthorisedController.onPageLoad().url
        }
      }
    }


    "the user has an unsupported affinity group" - {

      "must redirect the user to the unauthorised page" in new Fixture {

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig = application.injector.instanceOf[FrontendAppConfig]

          val authAction = new AuthenticatedIdentifierAction(new FakeFailingAuthConnector(new UnsupportedAffinityGroup), appConfig, bodyParsers)
          val controller = new Harness(authAction)
          val result = controller.onPageLoad()(FakeRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad().url)
        }
      }
    }

    "the user has an unsupported credential role" - {

      "must redirect the user to the unauthorised page" in new Fixture {

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig = application.injector.instanceOf[FrontendAppConfig]

          val authAction = new AuthenticatedIdentifierAction(new FakeFailingAuthConnector(new UnsupportedCredentialRole), appConfig, bodyParsers)
          val controller = new Harness(authAction)
          val result = controller.onPageLoad()(FakeRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad().url)
        }
      }
    }

    "user logged in as an agent" - {
      "and is allowed into the service" - {
        "must succeed" - {
          "when the user has a IR-SDLT-AGENT enrolment with the correct activated identifiers" in new Fixture {
            val enrolments = Enrolments(Set(agentEnrollment))
            when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
              .thenReturn(
                Future.successful(Some(id) ~ enrolments ~ Some(Organisation) ~ Some(User))
              )
            running(application) {
              val authAction = new AuthenticatedIdentifierAction(mockAuthConnector, appConfig, bodyParsers)
              val controller = new Harness(authAction)
              val result = controller.onPageLoad()(FakeRequest())

              status(result) mustBe OK
            }
          }
        }
      }
      /*
      "must redirect the user to unauthorised agent affinity screen" in new Fixture {

        when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
          .thenReturn(Future.successful(Some(id) ~ emptyEnrolments ~ Some(Agent) ~ None))

        running(application) {
          val authAction = new AuthenticatedIdentifierAction(mockAuthConnector, appConfig, bodyParsers)
          val controller = new Harness(authAction)
          val result = controller.onPageLoad()(FakeRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe controllers.manage.routes.UnauthorisedAgentAffinityController
            .onPageLoad()
            .url
        }
      }
       */
    }

    "the user is logged in as an individual" - {
      "fail and redirect to unauthorised individual affinity screen" in new Fixture {
        when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
          .thenReturn(
            Future.successful(Some(id) ~ emptyEnrolments ~ Some(Individual) ~ Some(Assistant))
          )
        running(application) {
          val authAction = new AuthenticatedIdentifierAction(mockAuthConnector, appConfig, bodyParsers)
          val controller = new Harness(authAction)
          val result = controller.onPageLoad()(FakeRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(
            result
          ).value mustBe controllers.manage.routes.UnauthorisedIndividualAffinityController.onPageLoad().url
        }
      }
    }

    "the user is logged in as an organisation assistant" - {
      "fail and redirect to unauthorised wrong role screen" in new Fixture {
        when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
          .thenReturn(
            Future.successful(Some(id) ~ emptyEnrolments ~ Some(Organisation) ~ Some(Assistant))
          )
        running(application) {
          val authAction = new AuthenticatedIdentifierAction(mockAuthConnector, appConfig, bodyParsers)
          val controller = new Harness(authAction)
          val result = controller.onPageLoad()(FakeRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe controllers.manage.routes.UnauthorisedWrongRoleController
            .onPageLoad()
            .url
        }
      }
    }

    "user is logged in as an organisation" - {
      "and is allowed into the service" - {
        "must succeed" - {
          "when the user has a IR-SDLT-ORG enrolment with the correct activated identifiers" in new Fixture {
            val enrolments = Enrolments(Set(orgEnrollment))
            when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
              .thenReturn(
                Future.successful(Some(id) ~ enrolments ~ Some(Organisation) ~ Some(User))
              )
            running(application) {
              val authAction = new AuthenticatedIdentifierAction(mockAuthConnector, appConfig, bodyParsers)
              val controller = new Harness(authAction)
              val result = controller.onPageLoad()(FakeRequest())

              status(result) mustBe OK
            }
          }
        }
      }

      "and is not allowed into the service" - {

        "when there is no IR-SDLT-ORG enrolment" - {

          "must redirect to unauthorised organisation affinity screen" in new Fixture {

            when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
              .thenReturn(
                Future.successful(Some(id) ~ emptyEnrolments ~ Some(Organisation) ~ Some(User))
              )
            running(application) {
              val authAction = new AuthenticatedIdentifierAction(mockAuthConnector, appConfig, bodyParsers)
              val controller = new Harness(authAction)
              val result = controller.onPageLoad()(FakeRequest())

              status(result) mustBe SEE_OTHER
              redirectLocation(
                result
              ).value mustBe controllers.manage.routes.UnauthorisedOrganisationAffinityController
                .onPageLoad()
                .url
            }
          }

        }

        "when there is an inactive IR-SDLT-ORG enrolment" - new Fixture {
          "must redirect to unauthorised organisation affinity screen" in {
            val enrolments = Enrolments(
              Set(
                Enrolment(
                  "IR-SDLT-ORG",
                  Seq(
                    EnrolmentIdentifier("STORN", testStorn)
                  ),
                  "inactivated",
                  None
                )
              )
            )
            when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
              .thenReturn(
                Future.successful(Some(id) ~ enrolments ~ Some(Organisation) ~ Some(User))
              )
            running(application) {
              val authAction = new AuthenticatedIdentifierAction(mockAuthConnector, appConfig, bodyParsers)
              val controller = new Harness(authAction)
              val result = controller.onPageLoad()(FakeRequest())
              status(result) mustBe SEE_OTHER
              redirectLocation(
                result
              ).value mustBe controllers.manage.routes.UnauthorisedOrganisationAffinityController
                .onPageLoad()
                .url
            }
          }
        }
      }
    }

    "Unable to retrieve internal id or affinity group" - {

      "fail and redirect to Unauthorised screen" in new Fixture {
        when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
          .thenReturn(Future.successful(None ~ emptyEnrolments ~ None ~ None))
        running(application) {
          val authAction = new AuthenticatedIdentifierAction(mockAuthConnector, appConfig, bodyParsers)
          val controller = new Harness(authAction)
          val result = controller.onPageLoad()(FakeRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe controllers.routes.UnauthorisedController.onPageLoad().url
        }
      }
    }

  }

}