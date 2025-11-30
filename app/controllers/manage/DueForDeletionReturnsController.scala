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


      (for {
        submitted                 <- stampDutyLandTaxService.getSubmittedReturnsDueForDeletion
        submittedReturns           = convertResponseToSubmittedView(submitted)
        submittedPaginatedView     = paginateIfValidPageIndex(Some(submittedReturns), submittedIndex, submittedUrlSelector)
        inProgress                <- stampDutyLandTaxService.getInProgressReturnsDueForDeletion
        inProgressReturns          = convertResponseToViewRows(inProgress)
        inProgressPaginatedView    = paginateIfValidPageIndex(Some(inProgressReturns), inProgressIndex, inProgressUrlSelector)
        paginatedInProgressReturns = inProgressPaginatedView.collectFirst { case Right((rows, paginator, paginationText)) => PaginatedInProgressReturnsViewModel(rows, paginator, paginationText) }
        paginatedSubmittedReturns  = submittedPaginatedView.collectFirst { case Right((rows, paginator, paginationText)) => PaginatedSubmittedReturnsViewModel(rows, paginator, paginationText) }
      } yield {
        (paginatedInProgressReturns, paginatedSubmittedReturns) match {
          case ( Some(inProgressViewModel), Some(submittedViewModel) ) =>
            Ok(view(inProgressViewModel, submittedViewModel))
          case _                                                       =>
            Logger("application").error(s"[DueForDeletionReturnsController][onPageLoad] - Pagination Index Error")
            Redirect(outOfScopeUrlSelector(1))
        }
      }) recover {
        case ex =>
          logger.error("[DueForDeletionReturnsController][onPageLoad] Unexpected failure", ex)
          Redirect(JourneyRecoveryController.onPageLoad())
      }
    }
}
