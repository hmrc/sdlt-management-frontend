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
import controllers.routes.JourneyRecoveryController
import models.manage.ReturnSummary
import models.responses.SdltInProgressReturnViewRow.convertResponseToViewRows
import models.responses.{PaginatedInProgressReturnsViewModel, SdltInProgressReturnViewRow}
import play.api.{Logger, Logging}
import uk.gov.hmrc.govukfrontend.views.viewmodels.pagination.Pagination

import javax.inject.{Inject, Singleton}
import navigation.Navigator
import utils.PaginationHelper
import services.StampDutyLandTaxService
import viewmodels.manage.{PaginatedSubmittedReturnsViewModel, SdltSubmittedReturnsViewModel}
import viewmodels.manage.SdltSubmittedReturnsViewModel.convertResponseToSubmittedView
import views.html.manage.DueForDeletionReturnsView

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

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

      val outOfScopeUrlSelector: Int => String = (paginationIndex: Int) => controllers.manage.routes.DueForDeletionReturnsController.onPageLoad(Some(1), Some(1)).url

      val inProgressUrlSelector: Int => String = (inProgressIndex: Int) => s"${controllers.manage.routes.DueForDeletionReturnsController.onPageLoad(
        Some(inProgressIndex), submittedIndex).url}#in-progress-returns"

      val submittedUrlSelector: Int => String = (submittedIndex: Int) => s"${controllers.manage.routes.DueForDeletionReturnsController.onPageLoad(
        inProgressIndex, Some(submittedIndex)).url}#submitted-returns"


      stampDutyLandTaxService.getReturnsDueForDeletion().map { response =>

        val rawDeletionInProgressReturns = convertResponseToViewRows(response)
        val rawDeletionSubmittedReturns = convertResponseToSubmittedView(response)
        val inProgressPaginatedView = paginateIfValidPageIndex(Some(rawDeletionInProgressReturns), inProgressIndex, inProgressUrlSelector)
        val submittedPaginatedView = paginateIfValidPageIndex(Some(rawDeletionSubmittedReturns), submittedIndex, submittedUrlSelector)

        if (inProgressPaginatedView.exists(_.isLeft) || submittedPaginatedView.exists(_.isLeft)) {
          Logger("application").error(s"[DueForDeletionReturnsController][onPageLoad] - Pagination Index Error")
          Redirect(outOfScopeUrlSelector(1))
        } else {
              val paginatedInProgressReturns = inProgressPaginatedView.flatMap {
                case Right((rowsP, paginatorP, paginationTextP)) =>
                  Some(PaginatedInProgressReturnsViewModel(rowsP, paginatorP, paginationTextP))
                case _ => None
              }.getOrElse(PaginatedInProgressReturnsViewModel(List.empty, None, None))

              val paginatedSubmittedReturns = submittedPaginatedView.flatMap {
                case Right((rowsS, paginatorS, paginationTextS)) =>
                  Some(PaginatedSubmittedReturnsViewModel(rowsS, paginatorS, paginationTextS))
                case _ => None
              }.getOrElse(PaginatedSubmittedReturnsViewModel(List.empty, None, None))

              Ok(view(paginatedInProgressReturns, paginatedSubmittedReturns))
        }
      } recover {
        case ex =>
          logger.error("[DueForDeletionReturnsController][onPageLoad] Unexpected failure", ex)
          Redirect(JourneyRecoveryController.onPageLoad())
      }
    }
}
