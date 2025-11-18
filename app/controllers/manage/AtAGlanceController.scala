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

import config.FrontendAppConfig
import controllers.actions.*
import play.api.Logging

import javax.inject.{Inject, Singleton}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.StampDutyLandTaxService
import services.InProgressReturnsService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.manage.AtAGlanceView
import controllers.routes.JourneyRecoveryController
import controllers.manage.routes.*
import viewmodels.manage.{AgentDetailsViewModel, FeedbackViewModel, HelpAndContactViewModel, ReturnsManagementViewModel}
import AtAGlanceController.*
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.authorise.EmptyPredicate
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import scala.concurrent.ExecutionContext

@Singleton
class AtAGlanceController@Inject()(
                                    override val messagesApi: MessagesApi,
                                    val controllerComponents: MessagesControllerComponents,
                                    val authConnector: AuthConnector,
                                    inProgressService: InProgressReturnsService,
                                    stampDutyLandTaxService: StampDutyLandTaxService,
                                    appConfig: FrontendAppConfig,
                                    identify: IdentifierAction,
                                    getData: DataRetrievalAction,
                                    view: AtAGlanceView,
                                    requireData: DataRequiredAction,
                                    stornRequiredAction: StornRequiredAction,
                                  )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData andThen stornRequiredAction).async { implicit request =>

    val storn = request.storn

    (for {
      agents <- stampDutyLandTaxService.getAllAgentDetails(storn)
      returnsInProgress <- inProgressService.getAllReturns(storn).map { result =>
        result.toOption.get
      }
      submittedReturns <- stampDutyLandTaxService.getSubmittedReturnsView(storn)
      dueForDeletion <- stampDutyLandTaxService.getReturn(storn, "DUE_FOR_DELETION")
      optName <- authConnector.authorise(EmptyPredicate,Retrievals.name)
    } yield {


      val maybeName = optName.flatMap(_.name).filterNot(_ == "TestUser")

      Ok(view(
          storn,
          maybeName,
          returnsManagementViewModel(returnsInProgress.size, submittedReturns.size, dueForDeletion.size),
          agentDetailsViewModel(agents.size, appConfig),
          helpAndContactViewModel(appConfig),
          feedbackViewModel(appConfig.exitSurveyUrl)
          )
        )
    }).recover {
        case ex =>
          logger.error("[AgentOverviewController][onPageLoad] Unexpected failure", ex)
          Redirect(JourneyRecoveryController.onPageLoad())
    }
  }
}

object AtAGlanceController {

  def returnsManagementViewModel(inProgress: Int, submitted: Int, dueForDeletion: Int): ReturnsManagementViewModel = ReturnsManagementViewModel(
    inProgress,
    InProgressReturnsController.onPageLoad(Some(1)).url,
    submitted,
    SubmittedReturnsController.onPageLoad(Some(1)).url,
    dueForDeletion,
    DueForDeletionController.onPageLoad().url,
    "#"
  )

  def agentDetailsViewModel(agents: Int, appConfig: FrontendAppConfig): AgentDetailsViewModel = AgentDetailsViewModel(
    agents,
    appConfig.agentOverviewUrl,
    appConfig.startAddAgentUrl
  )

  def helpAndContactViewModel(appConfig: FrontendAppConfig): HelpAndContactViewModel = HelpAndContactViewModel(
    "#",
    "#",
    appConfig.howToPayUrl,
    "#"
  )

  def feedbackViewModel(feedbackUrl: String): FeedbackViewModel = FeedbackViewModel(
    feedbackUrl
  )
}
