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
import navigation.Navigator
import play.api.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.InProgressReturnsService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.InProgressReturnView

import javax.inject.{Inject, Singleton}
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
                                      navigator: Navigator,
                                      view: InProgressReturnView
                                    )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData andThen stornRequiredAction).async { implicit request =>
    Logger("application").info(s"[InProgressReturnsController][onPageLoad]")
    inProgressReturnsService.getAll.map {
      case Right(inProgressReturnData) =>
        Ok(view())
      case Left(ex) =>
        Ok(view())
    }
  }

}