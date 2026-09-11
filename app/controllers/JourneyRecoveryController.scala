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

package controllers

import config.FrontendAppConfig
import controllers.actions.IdentifierAction
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.shaded.ahc.io.netty.util.Version.identify
import uk.gov.hmrc.play.bootstrap.binders.RedirectUrl.*
import uk.gov.hmrc.play.bootstrap.binders.*
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.SystemErrorView

import javax.inject.Inject

class JourneyRecoveryController @Inject()(
                                           override val messagesApi: MessagesApi,
                                           val controllerComponents: MessagesControllerComponents,
                                           identify: IdentifierAction,
                                           view: SystemErrorView
                                         )(implicit config: FrontendAppConfig) extends FrontendBaseController with Logging with I18nSupport {

  def onPageLoad(): Action[AnyContent] = identify {
    implicit request =>
      Ok(view())
  }
}
