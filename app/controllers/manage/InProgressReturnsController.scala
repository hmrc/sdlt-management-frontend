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
import models.responses.SdltReturnInfoResponse
import play.api.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, ActionBuilder, AnyContent, MessagesControllerComponents}
import services.InProgressReturnsService
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
                                             val inProgressReturnsService: InProgressReturnsService,
                                             identify: IdentifierAction,
                                             getData: DataRetrievalAction,
                                             requireData: DataRequiredAction,
                                             stornRequiredAction: StornRequiredAction,
                                             view: InProgressReturnView
                                           )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with PaginationHelper {

  private lazy val authActions: ActionBuilder[DataRequest, AnyContent] = identify andThen getData andThen requireData andThen stornRequiredAction

  def onPageLoad(index: Option[Int]): Action[AnyContent] = authActions.async { implicit request =>
    Logger("application").info(s"[InProgressReturnsController][onPageLoad] - pageIndex: $index")
    inProgressReturnsService.getAllReturns(request.storn).map {
      case Right(allDataRows) =>
        Logger("application").info(s"[InProgressReturnsController][onPageLoad] - render page")
        val selectedPageIndex: Int = index.getOrElse(1)
        val paginator: Option[Pagination] = createPagination(selectedPageIndex, allDataRows.length)
        val paginationText: Option[String] = getPaginationInfoText(selectedPageIndex, allDataRows)
        val rowsForSelectedPage: List[SdltReturnInfoResponse] = inProgressReturnsService.getRowForPageSelected(allDataRows, selectedPageIndex, ROWS_ON_PAGE)
        Ok(view(rowsForSelectedPage, paginator, paginationText))
      case Left(ex) =>
        Logger("application").error(s"[InProgressReturnsController][onPageLoad] - pageIndex: $index / error: ${ex}")
        Redirect(JourneyRecoveryController.onPageLoad())
    }
  }

}