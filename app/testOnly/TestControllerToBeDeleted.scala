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

package testOnly

import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.twirl.api.Html
import services.StampDutyLandTaxService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class TestControllerToBeDeleted @Inject()(
                                            override val messagesApi: MessagesApi,
                                            identify: IdentifierAction,
                                            getData: DataRetrievalAction,
                                            requireData: DataRequiredAction,
                                            requireStorn: IdentifierAction,
                                            stampDutyLandTaxService: StampDutyLandTaxService,
                                            val controllerComponents: MessagesControllerComponents
                                          )(implicit executionContext: ExecutionContext) extends FrontendBaseController with I18nSupport {

  //TODO: TO BE DELETED - FOR VISUAL TESTING

  def onPageLoad(storn: String): Action[AnyContent] = (identify andThen getData andThen requireData andThen requireStorn).async {
    implicit request =>

      for {
        getAllAcceptedReturns <- stampDutyLandTaxService.getReturn(storn, "ACCEPTED")
        getAllInProgressReturns <- stampDutyLandTaxService.getReturn(storn, "IN-PROGRESS")
        getAllPendingReturns <- stampDutyLandTaxService.getReturn(storn, "PENDING")
        getAllStartedReturns <- stampDutyLandTaxService.getReturn(storn, "STARTED")
        getAllSubmittedReturns <- stampDutyLandTaxService.getReturn(storn, "SUBMITTED")
        getReturnsDueForDeletion <- stampDutyLandTaxService.getReturn(storn, "DUE_FOR_DELETION")
        getAllAgents <- stampDutyLandTaxService.getAllAgents(storn)
      } yield {
        Ok(Html(
          s"""
             |getAllAgents: $getAllAgents
             |<br><br>
             |getAllAcceptedReturns: $getAllAcceptedReturns
             |<br><br>
             |getAllInProgressReturns: $getAllInProgressReturns
             |<br><br>
             |getAllPendingReturns: $getAllPendingReturns
             |<br><br>
             |getAllPendingReturns: $getAllPendingReturns
             |<br><br>
             |getAllStartedReturns: $getAllStartedReturns
             |<br><br>
             |getAllSubmittedReturns: $getAllSubmittedReturns
             |<br><br>
             |getReturnsDueForDeletion: $getReturnsDueForDeletion
             |<br><br>
             |getAllAgents: $getAllAgents
             |""".stripMargin
        ))
      }
  }
}