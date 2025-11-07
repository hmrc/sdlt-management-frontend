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
import play.api.Logger
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.InProgressReturnsService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.InProgressReturnView
import com.google.inject.{Inject, Singleton}
import models.responses.SdltReturnInfoResponse
import uk.gov.hmrc.govukfrontend.views.Aliases.Pagination
import uk.gov.hmrc.govukfrontend.views.viewmodels.pagination.{Pagination, PaginationItem, PaginationLink}
import utils.PaginationHelper

import scala.concurrent.ExecutionContext

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

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData andThen stornRequiredAction).async { implicit request =>
    Logger("application").info(s"[InProgressReturnsController][onPageLoad]")
    inProgressReturnsService.getSummaryList(request.storn).map {
      case Right(dataRows) =>
        val paginator = createPagination(dataRows)
        val paginationText: Option[String]     = getPaginationInfoText(1, dataRows)
        Ok(view(dataRows, paginator, paginationText))
      case Left(ex) =>
        Ok(view(List.empty, None, None))
    }
  }

  private def createPagination(dataRows: List[SdltReturnInfoResponse])
                              (implicit messages: Messages): Option[Pagination] = {
    // TODO: evaluate number of pages => dataRows
    if (dataRows.nonEmpty && dataRows.length > ROWS_ON_PAGE) {
      Some(
        Pagination(
          items = Some(generatePaginationItems(0, 2)),
          previous = generatePreviousLink(0, 2),
          next = generateNextLink(0, 2),
          landmarkLabel = None,
          classes = "",
          attributes = Map.empty
        )
      )
    } else {
      None
  }

  }


}