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
import models.manage.{ReturnSummaryLegacy, SdltReturnRecordRequest, SdltReturnRecordResponse, SdltReturnRecordResponseLegacy}
import models.manageAgents.AgentDetailsResponse
import models.requests.DataRequest
import models.responses.UniversalStatus
import models.responses.UniversalStatus.{ACCEPTED, PENDING}
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class StampDutyLandTaxService @Inject() (stampDutyLandTaxConnector: StampDutyLandTaxConnector)
                                        (implicit executionContext: ExecutionContext) {

  @deprecated
  def getReturnLegacy(storn: String, status: String)
                     (implicit headerCarrier: HeaderCarrier): Future[List[ReturnSummaryLegacy]] = {
  stampDutyLandTaxConnector
    .getAllReturnsLegacy(storn)
    .map {
      _.returnSummaryList.filter(_.status == status)
    }
  }

  // TODO: THIS IS USING A DEPRECATED CALL
  @deprecated
  def getAllAgents(storn: String)
                  (implicit headerCarrier: HeaderCarrier): Future[List[AgentDetailsResponse]] =
    stampDutyLandTaxConnector
      .getAllAgentDetailsLegacy(storn)

  def getInProgressReturns(implicit hc: HeaderCarrier, request: DataRequest[_]): Future[SdltReturnRecordResponse] = {
    for {
      accepted <- stampDutyLandTaxConnector.getReturns(Some("ACCEPTED"), Some("IN-PROGRESS"))
      pending  <- stampDutyLandTaxConnector.getReturns(Some("PENDING"),  Some("IN-PROGRESS"))
    } yield {
      SdltReturnRecordResponse(
        returnSummaryList =
          accepted.returnSummaryList ++ pending.returnSummaryList
      )
    }
  }

  def getSubmittedReturns(implicit hc: HeaderCarrier, request: DataRequest[_]): Future[SdltReturnRecordResponse] = {
    for {
      submitted          <- stampDutyLandTaxConnector.getReturns(Some("SUBMITTED"),            Some("SUBMITTED"))
      submittedNoReceipt <- stampDutyLandTaxConnector.getReturns(Some("SUBMITTED_NO_RECEIPT"), Some("SUBMITTED"))
    } yield {
      SdltReturnRecordResponse(
        returnSummaryList =
          submitted.returnSummaryList ++ submittedNoReceipt.returnSummaryList
      )
    }
  }

  def getReturnsDueForDeletion(implicit hc: HeaderCarrier, request: DataRequest[_]): Future[SdltReturnRecordResponse] =
    stampDutyLandTaxConnector
      .getReturns(None, None, deletionFlag = true)

  def getAllAgents(implicit hc: HeaderCarrier, request: DataRequest[_]): Future[Int] =
    stampDutyLandTaxConnector
      .getSdltOrganisation
      .map(_.agents.length)
}
