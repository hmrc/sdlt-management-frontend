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
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import controllers.routes.{JourneyRecoveryController, SystemErrorController}
import play.api.Logging

import javax.inject.{Inject, Singleton}
import navigation.Navigator
import utils.PaginationHelper
import services.StampDutyLandTaxService
import uk.gov.hmrc.govukfrontend.views.Aliases.Pagination
import views.html.manage.SubmittedReturnsView
import controllers.manage.routes.*
import models.SdltReturnTypes.SUBMITTED_SUBMITTED_RETURNS
import models.responses.SdltSubmittedReturnViewModel
import utils.PageUrlSelector.submittedUrlSelector

import scala.concurrent.ExecutionContext

@Singleton
class SubmittedReturnsController @Inject()(
                                            val controllerComponents: MessagesControllerComponents,
                                            stampDutyLandTaxService: StampDutyLandTaxService,
                                            identify: IdentifierAction,
                                            getData: DataRetrievalAction,
                                            requireData: DataRequiredAction,
                                            stornRequiredAction: StornRequiredAction,
                                            navigator: Navigator,
                                            view: SubmittedReturnsView
                                          )(implicit executionContext: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging with PaginationHelper {


  def onPageLoad(paginationIndex: Option[Int]): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen stornRequiredAction)
      .async { implicit request =>
        stampDutyLandTaxService
          .getReturnsByTypeViewModel[SdltSubmittedReturnViewModel](request.storn, SUBMITTED_SUBMITTED_RETURNS, paginationIndex)
          .map { viewModel =>
            viewModel.validatePageIndex(paginationIndex, viewModel.totalRowCount) match {
              case Right(selectedPageIndex) =>
                logger.info(s"[SubmittedReturnsController][onPageLoad] - rows on page: ${paginationIndex} - ${viewModel.rows.length}")
                Ok( view(viewModel) )
              case Left(error) =>
                logger.error(s"[InProgressReturnsController][onPageLoad] - other error: $error")
                Redirect(JourneyRecoveryController.onPageLoad())
            }
          } recover {
          case ex =>
            logger.error("[SubmittedReturnsController][onPageLoad] Unexpected failure", ex)
            Redirect(SystemErrorController.onPageLoad())
        }
      }
}
