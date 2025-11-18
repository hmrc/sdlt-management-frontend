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

package services

import connectors.StampDutyLandTaxConnector
import models.manage.ReturnSummary
import models.manageAgents.AgentDetailsResponse
import viewmodels.manage.SdltSubmittedReturnsViewModel
import uk.gov.hmrc.http.HeaderCarrier
import viewmodels.manage.SdltSubmittedReturnsViewModel.convertResponseToSubmittedView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class StampDutyLandTaxService @Inject() (stampDutyLandTaxConnector: StampDutyLandTaxConnector)
                                        (implicit executionContext: ExecutionContext) {

  // TODO: THIS LOGIC IMPLEMENTATION IS WRONG DUE TO INCORRECT DOCUMENTATION (wrong models) - THIS WILL BE FIXED IN THE NEXT SPRINT

  private val VALID_STATUSES: Set[String] = Set(
    "PENDING",
    "ACCEPTED",
    "STARTED",
    "IN-PROGRESS",
    "ACCEPTED",
    "FATAL_ERROR",
    "DEPARTMENTAL_ERROR",
    "SUBMITTED",
    "DUE_FOR_DELETION",    // TODO: THIS IS AN INCORRECT WORKAROUND DUE TO INCORRECT DOCUMENTATION
    "SUBMITTED_NO_RECEIPT"
  )

  def getReturn(storn: String, status: String)
               (implicit headerCarrier: HeaderCarrier): Future[List[ReturnSummary]] = {
  stampDutyLandTaxConnector
    .getAllReturns(storn)
    .map {
      _.returnSummaryList.filter(_.status == status)
    }
  }

  // TODO: REMOVE THIS DEPRECATED CALL
  @deprecated
  def getAllAgents(storn: String)
                  (implicit headerCarrier: HeaderCarrier): Future[List[AgentDetailsResponse]] =
    stampDutyLandTaxConnector
      .getAllAgentDetails(storn)


  def getAllAgentDetails(storn: String)
                        (implicit headerCarrier: HeaderCarrier): Future[Seq[AgentDetailsResponse]] =
    stampDutyLandTaxConnector
      .getSdltOrganisation(storn)
      .map(_.agents)

  def getSubmittedReturnsView(storn: String)
                   (implicit hc: HeaderCarrier): Future[List[SdltSubmittedReturnsViewModel]] = {
    stampDutyLandTaxConnector
      .getAllReturns(storn).map { response =>
      convertResponseToSubmittedView(response)
    }
  }
}
