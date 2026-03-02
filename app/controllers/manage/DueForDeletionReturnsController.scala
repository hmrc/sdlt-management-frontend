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
import controllers.routes.SystemErrorController
import models.SdltReturnTypes.{IN_PROGRESS_RETURNS_DUE_FOR_DELETION, SUBMITTED_RETURNS_DUE_FOR_DELETION}
import models.requests.Storn
import models.responses.{SdltDueForDeletionReturnViewModel, SdltInProgressDueForDeletionReturnViewModel, SdltReturnViewRow, SdltSubmittedDueForDeletionReturnViewModel}
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.StampDutyLandTaxService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.LoggerUtil.{logError, logInfo}
import utils.PaginationHelper
import views.html.manage.DueForDeletionReturnsView

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class DueForDeletionReturnsController @Inject()(
                                                 val controllerComponents: MessagesControllerComponents,
                                                 stampDutyLandTaxService: StampDutyLandTaxService,
                                                 identify: IdentifierAction,
                                                 getData: DataRetrievalAction,
                                                 requireData: DataRequiredAction,
                                                 stornRequiredAction: StornRequiredAction,
                                                 view: DueForDeletionReturnsView
                                               )(implicit executionContext: ExecutionContext, appConfig: FrontendAppConfig) extends FrontendBaseController with I18nSupport with Logging with PaginationHelper {

  def onPageLoad(inProgressIndex: Option[Int], submittedIndex: Option[Int]): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen stornRequiredAction).async { implicit request =>
      logInfo(s"[DueForDeletionReturnsController][onPageLoad] :: ${inProgressIndex} - ${submittedIndex}")

      (for {
        inProgressDueForDeletionViewModel <- stampDutyLandTaxService.getReturnsByTypeViewModel[SdltInProgressDueForDeletionReturnViewModel](
          storn = request.storn,
          IN_PROGRESS_RETURNS_DUE_FOR_DELETION,
          inProgressIndex)
        submittedDueForDeletionViewModel <- stampDutyLandTaxService.getReturnsByTypeViewModel[SdltSubmittedDueForDeletionReturnViewModel](
          storn = request.storn,
          SUBMITTED_RETURNS_DUE_FOR_DELETION,
          submittedIndex)
      } yield {
        val viewModel : SdltDueForDeletionReturnViewModel =
          SdltDueForDeletionReturnViewModel(
            inProgressSelectedPageIndex = inProgressIndex,
            submittedSelectedPageIndex = submittedIndex,
            inProgressViewModel = inProgressDueForDeletionViewModel,
            submittedViewModel = submittedDueForDeletionViewModel,
          )
        Ok(view(viewModel, appConfig.startNewReturnUrl))
      }) recover {
        case ex =>
          logError(s"[DueForDeletionReturnsController][onPageLoad] Unexpected failure: ${ex.getMessage}")
          Redirect(SystemErrorController.onPageLoad())
      }
    }

}