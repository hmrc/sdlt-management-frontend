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
import models.responses.SdltInProgressReturnViewRow.convertResponseToViewRows
import models.responses.{PaginatedInProgressReturnsViewModel, SdltInProgressReturnViewRow, UniversalStatus}
import play.api.{Logger, Logging}
import uk.gov.hmrc.govukfrontend.views.viewmodels.pagination.Pagination

import javax.inject.{Inject, Singleton}
import navigation.Navigator
import utils.PaginationHelper
import services.StampDutyLandTaxService
import viewmodels.manage.{PaginatedSubmittedReturnsViewModel, SdltDueDeletionReturnsViewModel, SdltSubmittedReturnsViewModel}
import viewmodels.manage.SdltSubmittedReturnsViewModel.convertResponseToSubmittedView
import views.html.manage.DueDeletion

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DueForDeletionController @Inject()(
                                            val controllerComponents: MessagesControllerComponents,
                                            stampDutyLandTaxService: StampDutyLandTaxService,
                                            identify: IdentifierAction,
                                            getData: DataRetrievalAction,
                                            requireData: DataRequiredAction,
                                            stornRequiredAction: StornRequiredAction,
                                            navigator: Navigator,
                                            view: DueDeletion
                                          )(implicit executionContext: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging with PaginationHelper {

  val urlSelector: Int => String = (paginationIndex: Int) => controllers.manage.routes.DueForDeletionController.onPageLoad(Some(paginationIndex)).url

  def onPageLoad(paginationIndex: Option[Int]): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen stornRequiredAction).async { implicit request =>

      val allDataRows: Future[List[SdltSubmittedReturnsViewModel]] =
        Future.successful((0 to 17).toList.map(index =>
          SdltSubmittedReturnsViewModel(
            address = s"$index Riverside Drive",
            utrn = "UTRN003",
            purchaserName = "Brown",
            status = UniversalStatus.SUBMITTED_NO_RECEIPT
          )
        ))

      val allInProgressDataRows: Future[List[SdltInProgressReturnViewRow]] =
        Future.successful((0 to 17).toList.map(index =>
          SdltInProgressReturnViewRow(
            address = s"$index Riverside Drive",
            agentReference = "B4C72F7T3",
            purchaserName = "Brown",
            status = UniversalStatus.SUBMITTED_NO_RECEIPT
          )
        ))


      stampDutyLandTaxService.getReturnsDueForDeletion().map { response =>
        val rawDeletionInProgressReturns = convertResponseToViewRows(response)
        val rawDeletionSubmittedReturns = convertResponseToSubmittedView(response)
        val inProgressPaginatedView = paginateIfValidPageIndex(Some(rawDeletionInProgressReturns), paginationIndex, urlSelector)
        val submittedPaginatedView = paginateIfValidPageIndex(Some(rawDeletionSubmittedReturns), paginationIndex, urlSelector)
        if (inProgressPaginatedView.isEmpty || submittedPaginatedView.isEmpty) {
          val returnsError =
            List("inProgressPaginatedView" -> inProgressPaginatedView, "submittedPaginatedView" -> submittedPaginatedView).collect { case (name, list) if list.isEmpty => name }
          val which = returnsError.mkString(" and ")
          Logger("application").error(s"[SubmittedReturnsController][onPageLoad] - $returnsError")
          Redirect(urlSelector(1))
        } else {
          (inProgressPaginatedView, submittedPaginatedView) match {
            case (
              Some(Right((rowsP, Some(validIndexP), urlSelectorP))),
              Some(Right((rowsS, Some(validIndexS), urlSelectorS)))
              ) =>
              val paginatedInProgressReturns = PaginatedInProgressReturnsViewModel(rowsP, Some(validIndexP), urlSelectorP)
              val paginatedSubmittedReturns = PaginatedSubmittedReturnsViewModel(rowsS, Some(validIndexS), urlSelectorS)

              //val dueForDeletionReturnsPagination = SdltDueDeletionReturnsViewModel(paginatedInProgressReturns, paginatedSubmittedReturns)

              Ok(view(paginatedInProgressReturns, paginatedSubmittedReturns))
          }
        }
      }
    }
}
          
        
        
//      } recover {
//        case ex =>
//          logger.error("[SubmittedReturnsController][onPageLoad] Unexpected failure", ex)
//          Redirect(JourneyRecoveryController.onPageLoad())
//      }
//    }
          
      


//      stampDutyLandTaxService
//        .getSubmittedReturnsView(request.storn).map {
//          case allDataRows =>
//            pageIndexSelector(paginationIndex, allDataRows.length) match {
//              case Right(selectedPageIndex) =>
//
//                val selectedPageIndex: Int = paginationIndex.getOrElse(1)
//                val paginator: Option[Pagination] = createPagination(selectedPageIndex, allDataRows.length, urlSelector)
//                val paginationText: Option[String] = getPaginationInfoText(selectedPageIndex, allDataRows)
//                val rowsForSelectedPage: List[SdltSubmittedReturnsViewModel] = getSelectedPageRows(allDataRows, selectedPageIndex)
//
//                Ok(view(rowsForSelectedPage, paginator, paginationText))
//
//              case Left(error) =>
//                Logger("application").error(s"[SubmittedReturnsController][onPageLoad] - paginationIndexError: $error")
//                Redirect(urlSelector(1))
//            }
//        } recover {
//        case ex =>
//          logger.error("[SubmittedReturnsController][onPageLoad] Unexpected failure", ex)
//          Redirect(JourneyRecoveryController.onPageLoad())
//      }
//    }
//
//}
