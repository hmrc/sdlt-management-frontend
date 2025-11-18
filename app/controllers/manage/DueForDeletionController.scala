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
import models.responses.UniversalStatus
import play.api.{Logger, Logging}
import uk.gov.hmrc.govukfrontend.views.viewmodels.pagination.Pagination

import javax.inject.{Inject, Singleton}
import navigation.Navigator
import utils.PaginationHelper
import services.StampDutyLandTaxService
import viewmodels.manage.SdltSubmittedReturnsViewModel
import views.html.manage.{DueDeletion, SubmittedReturnsView}

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



  def onPageLoad(paginationIndex: Option[Int]): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen stornRequiredAction).async { implicit request =>

      val urlSelector: Int => String = (paginationIndex: Int) => controllers.manage.routes.DueForDeletionController.onPageLoad(Some(paginationIndex)).url

      val allDataRows: Future[List[SdltSubmittedReturnsViewModel]] =
        Future.successful((0 to 17).toList.map(index =>
          SdltSubmittedReturnsViewModel(
            address = s"$index Riverside Drive",
            agentReference = "B4C72F7T3",
            dateSubmitted = LocalDate.parse("2025-04-05"),
            utrn = "UTRN003",
            purchaserName = "Brown",
            status = UniversalStatus.SUBMITTED_NO_RECEIPT,
            returnReference = "RETREF003",
          )
        ))

      allDataRows.map {
          case allDataRows =>
            pageIndexSelector(paginationIndex, allDataRows.length) match {
              case Right(selectedPageIndex) =>

                val selectedPageIndex: Int = paginationIndex.getOrElse(1)
                val paginator: Option[Pagination] = createPagination(selectedPageIndex, allDataRows.length, urlSelector)
                val paginationText: Option[String] = getPaginationInfoText(selectedPageIndex, allDataRows)
                val rowsForSelectedPage: List[SdltSubmittedReturnsViewModel] = getSelectedPageRows(allDataRows, selectedPageIndex)

                Ok(view(rowsForSelectedPage, paginator, paginationText))

              case Left(error) =>
                Logger("application").error(s"[SubmittedReturnsController][onPageLoad] - paginationIndexError: $error")
                Redirect(urlSelector(1))
            }
        } recover {
        case ex =>
          logger.error("[SubmittedReturnsController][onPageLoad] Unexpected failure", ex)
          Redirect(JourneyRecoveryController.onPageLoad())
      }
    }

}
