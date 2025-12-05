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

import controllers.actions.*
import controllers.manage.routes.*
import controllers.routes.JourneyRecoveryController
import models.SdltReturnTypes.{IN_PROGRESS_RETURNS_DUE_FOR_DELETION, SUBMITTED_RETURNS_DUE_FOR_DELETION}
import navigation.Navigator
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.StampDutyLandTaxService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
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
                                                 navigator: Navigator,
                                                 view: DueForDeletionReturnsView
                                               )(implicit executionContext: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging with PaginationHelper {

  def onPageLoad(inProgressIndex: Option[Int], submittedIndex: Option[Int]): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen stornRequiredAction).async { implicit request =>
      logger.info(s"[DueForDeletionReturnsController][onPageLoad] :: ${inProgressIndex} - ${submittedIndex}")

      val outOfScopeUrlSelector: String = DueForDeletionReturnsController.onPageLoad(Some(1), Some(1)).url

      lazy val inProgressUrlSelector: Int => String =
        (inProgressIndex: Int) =>
          s"${DueForDeletionReturnsController.onPageLoad(Some(inProgressIndex), submittedIndex).url}#in-progress"

      lazy val submittedUrlSelector: Int => String =
        (submittedIndex: Int) =>
          s"${DueForDeletionReturnsController.onPageLoad(inProgressIndex, Some(submittedIndex)).url}#submitted"

      (for {
        inProgressDurForDeletion <- stampDutyLandTaxService.getReturnsByTypeViewModel(
          storn = request.storn,
          IN_PROGRESS_RETURNS_DUE_FOR_DELETION,
          None)
        submittedDueDorDeletionViewModel <- stampDutyLandTaxService.getReturnsByTypeViewModel(
          storn = request.storn,
          SUBMITTED_RETURNS_DUE_FOR_DELETION,
          None)
      } yield {
        Ok(
            view(
              inProgressDurForDeletion,
              submittedDueDorDeletionViewModel,
              inProgressIndex.getOrElse(1),
              submittedIndex.getOrElse(1),
              inProgressUrlSelector,
              submittedUrlSelector))
      }) recover {
        case ex =>
          logger.error("[DueForDeletionReturnsController][onPageLoad] Unexpected failure", ex)
          Redirect(JourneyRecoveryController.onPageLoad())
      }
    }

}