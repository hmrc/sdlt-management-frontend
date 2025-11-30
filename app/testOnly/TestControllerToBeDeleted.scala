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
                                            stampDutyLandTaxService: StampDutyLandTaxService,
                                            val controllerComponents: MessagesControllerComponents
                                          )(implicit executionContext: ExecutionContext) extends FrontendBaseController with I18nSupport {

  //TODO: TO BE DELETED - FOR VISUAL TESTING

  def onPageLoad(storn: String): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      for {
        getAllInProgressReturns         <- stampDutyLandTaxService.getInProgressReturns
        getAllSubmittedReturns          <- stampDutyLandTaxService.getSubmittedReturns
        submittedReturnsDueForDeletion  <- stampDutyLandTaxService.getSubmittedReturnsDueForDeletion
        inProgressReturnsDueForDeletion <- stampDutyLandTaxService.getInProgressReturnsDueForDeletion
        getAgentsCount                  <- stampDutyLandTaxService.getAgentCount
      } yield {
        Ok(Html(
          s"""
             |getAllInProgressReturns: $getAllInProgressReturns
             |<br><br>
             |getAllSubmittedReturns: $getAllSubmittedReturns
             |<br><br>
             |getReturnsDueForDeletion: ${(submittedReturnsDueForDeletion ++ inProgressReturnsDueForDeletion).sortBy(_.purchaserName)}
             |""".stripMargin
        ))
      }
  }
}
