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
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.manage.AtAGlanceView
import controllers.routes.JourneyRecoveryController
import controllers.manage.routes.*
import scala.concurrent.ExecutionContext

@Singleton
class AtAGlanceController@Inject()(
                                    override val messagesApi: MessagesApi,
                                    val controllerComponents: MessagesControllerComponents,
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

    val agentsF = stampDutyLandTaxService.getAllAgents(storn)
    val returnsInProgressF = stampDutyLandTaxService.getReturn(storn, "IN_PROGRESS")
    val submittedReturnsF = stampDutyLandTaxService.getReturn(storn, "SUBMITTED")
    val dueForDeletionF = stampDutyLandTaxService.getReturn(storn, "DUE_FOR_DELETION")

    (for {
      agents <- agentsF
      returnsInProgress <- returnsInProgressF
      submittedReturns <- submittedReturnsF
      dueForDeletion <- dueForDeletionF
    } yield {
      val numAgents = agents.size
      val numInProgress = returnsInProgress.size
      val numSubmitted = submittedReturns.size
      val numDueForDeletion = dueForDeletion.size

      Ok(view(
              storn,
              numAgents,
              numInProgress,
              numSubmitted,
              numDueForDeletion,
              inProgressUrl = InProgressReturnsController.onPageLoad(Some(1)).url,
              submittedUrl = SubmittedReturnsController.onPageLoad().url,
              dueForDeletionUrl = DueForDeletionController.onPageLoad().url,
              feedbackUrl = appConfig.feedbackUrl
            )
        )
    }).recover {
            case ex =>
              logger.error("[AgentOverviewController][onPageLoad] Unexpected failure", ex)
              Redirect(JourneyRecoveryController.onPageLoad())
    }

  }
}
