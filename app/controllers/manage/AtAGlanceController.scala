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
import controllers.routes.SystemErrorController
import controllers.manage.routes.*
import viewmodels.manage.{AgentDetailsViewModel, FeedbackViewModel, HelpAndContactViewModel, ReturnsManagementViewModel}
import AtAGlanceController.*
import models.SdltReturnTypes.{IN_PROGRESS_RETURNS, IN_PROGRESS_RETURNS_DUE_FOR_DELETION, SUBMITTED_RETURNS_DUE_FOR_DELETION, SUBMITTED_SUBMITTED_RETURNS}
import models.manage.AtAGlanceViewModel

import scala.concurrent.ExecutionContext

@Singleton
class AtAGlanceController@Inject()(
                                    override val messagesApi: MessagesApi,
                                    val controllerComponents: MessagesControllerComponents,
                                    stampDutyLandTaxService: StampDutyLandTaxService,
                                    identify: IdentifierAction,
                                    getData: DataRetrievalAction,
                                    view: AtAGlanceView,
                                    requireData: DataRequiredAction,
                                    stornRequiredAction: StornRequiredAction,
                                  )(implicit ec: ExecutionContext, appConfig: FrontendAppConfig) extends FrontendBaseController with I18nSupport with Logging {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData andThen stornRequiredAction).async { implicit request =>

    // TODO : retrieve first and last name of user and pass down to view
    val name = "David Frank"

    (for {
      agentsCount                     <- stampDutyLandTaxService.getAgentCount
      returnsInProgress               <- stampDutyLandTaxService.getReturnsByTypeViewModel(request.storn, IN_PROGRESS_RETURNS, None)
      submittedReturns                <- stampDutyLandTaxService.getReturnsByTypeViewModel(request.storn, SUBMITTED_SUBMITTED_RETURNS, None)
      submittedReturnsDueForDeletion  <- stampDutyLandTaxService.getReturnsByTypeViewModel(request.storn, SUBMITTED_RETURNS_DUE_FOR_DELETION, None)
      inProgressReturnsDueForDeletion <- stampDutyLandTaxService.getReturnsByTypeViewModel(request.storn, IN_PROGRESS_RETURNS_DUE_FOR_DELETION, None)
      returnsDueForDeletionRows            = (submittedReturnsDueForDeletion.rows ++ inProgressReturnsDueForDeletion.rows).sortBy(_.purchaserName)
    } yield {

      Ok(view(
        AtAGlanceViewModel(
          storn = request.storn,
          name = name,
          inProgressReturns = returnsInProgress,
          submittedReturns = submittedReturns,
          dueForDeletionReturns = returnsDueForDeletionRows,
          agentsCount = agentsCount
        )
      ))
    }) recover {
        case ex =>
          logger.error("[AgentOverviewController][onPageLoad] Unexpected failure", ex)
          Redirect(SystemErrorController.onPageLoad())
    }
  }
}
