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
import models.manage.{ReturnSummary, SdltReturnRecordResponse}
import models.manageAgents.AgentDetailsResponse
import uk.gov.hmrc.http.HeaderCarrier

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

  def getReturn(storn: String)
               (implicit headerCarrier: HeaderCarrier): Future[SdltReturnRecordResponse] =
  stampDutyLandTaxConnector
    .getAllReturns(storn)
    .map {
      _.returnSummaryList.filter {
        case ReturnSummary(_, _, status, _, _, _, _) => status == "PENDING"
        case ReturnSummary(_, _, status, _, _, _, _) => status == "ACCEPTED"
        case ReturnSummary(_, _, status, _, _, _, _) => status == "PENDING"
        case ReturnSummary(_, _, status, _, _, _, _) => status == "PENDING"
        case ReturnSummary(_, _, status, _, _, _, _) => status == "PENDING"
        case ReturnSummary(_, _, status, _, _, _, _) => status == "PENDING"
        case ReturnSummary(_, _, status, _, _, _, _) => status == "PENDING"
        case ReturnSummary(_, _, status, _, _, _, _) => status == "PENDING"
        case summary@"SUBMITTED" => summary.status == "SUBMITTED"
        case summary@"ACCEPTED" => summary.status == "ACCEPTED"
        case summary@"STARTED" => summary.status == "STARTED"
        case summary@"IN-PROGRESS" => summary.status == "IN-PROGRESS"
        case summary@"DUE_FOR_DELETION" => summary.status == "DUE_FOR_DELETION"
        case _ => false
      }
    }
}
