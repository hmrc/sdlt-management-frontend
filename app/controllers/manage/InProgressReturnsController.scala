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

import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction, StornRequiredAction}
import controllers.routes.JourneyRecoveryController
import models.requests.DataRequest
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, ActionBuilder, AnyContent, MessagesControllerComponents}
import services.StampDutyLandTaxService
import uk.gov.hmrc.govukfrontend.views.Aliases.Pagination
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.PaginationHelper
import views.html.InProgressReturnView

import scala.concurrent.ExecutionContext
import javax.inject.*

@Singleton
class InProgressReturnsController @Inject()(
                                             override val messagesApi: MessagesApi,
                                             val controllerComponents: MessagesControllerComponents,
                                             stampDutyLandTaxService: StampDutyLandTaxService,
                                             identify: IdentifierAction,
                                             getData: DataRetrievalAction,
                                             requireData: DataRequiredAction,
                                             stornRequiredAction: StornRequiredAction,
                                             view: InProgressReturnView
                                           )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with PaginationHelper with Logging {

  private lazy val authActions: ActionBuilder[DataRequest, AnyContent] = identify andThen getData andThen requireData andThen stornRequiredAction

  val urlSelector: Int => String = (pageIndex: Int) => controllers.manage.routes.InProgressReturnsController.onPageLoad(Some(pageIndex)).url

  def onPageLoad(index: Option[Int]): Action[AnyContent] = authActions.async { implicit request =>

    stampDutyLandTaxService.getInProgressReturnsViewModel(request.storn, index) map { viewModel =>
      logger.info(s"[InProgressReturnsController][onPageLoad] - render page: $index")
      val totalRowsCount = viewModel.totalRowCount.getOrElse(0)
      pageIndexSelector(index, totalRowsCount) match {
        case Right(selectedPageIndex) =>
          val paginator: Option[Pagination] = createPagination(selectedPageIndex, totalRowsCount, urlSelector)
          val paginationText: Option[String] = getPaginationInfoText(selectedPageIndex, viewModel.rows )
          logger.info(s"[InProgressReturnsController][onPageLoad] - view model r/count: ${viewModel.rows.length}")
          Ok(view(viewModel.rows, paginator, paginationText))
// TODO: disable page index redirect / need to be fix as part of tech debt or bug fix story: TBC
//        case Left(error) if error.getMessage.contains("PageIndex selected is out of scope") =>
//          logger.error(s"[InProgressReturnsController][onPageLoad] - indexError: $error")
//          Redirect( urlSelector(1) )
        case Left(error) =>
          logger.error(s"[InProgressReturnsController][onPageLoad] - other error: $error")
          Redirect(JourneyRecoveryController.onPageLoad())
      }
    } recover {
      case ex =>
        logger.error("[InProgressReturnsController][onPageLoad] Unexpected failure", ex)
        Redirect(JourneyRecoveryController.onPageLoad())
    }
  }
}