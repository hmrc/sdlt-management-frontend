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
import viewmodels.manage.SdltSubmittedReturnsViewModel
import models.requests.DataRequest
import models.responses.SdltInProgressReturnViewRow
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class StampDutyLandTaxService @Inject() (stampDutyLandTaxConnector: StampDutyLandTaxConnector)
                                        (implicit executionContext: ExecutionContext) {

  def getInProgressReturns(implicit hc: HeaderCarrier, request: DataRequest[_]): Future[List[SdltInProgressReturnViewRow]] = {
    for {
      accepted <- stampDutyLandTaxConnector.getReturns(Some("ACCEPTED"), Some("IN-PROGRESS"), deletionFlag = false)
      started  <- stampDutyLandTaxConnector.getReturns(Some("STARTED"),  Some("IN-PROGRESS"), deletionFlag = false)
    } yield {

      val inProgressReturnsList =
        (accepted.returnSummaryList ++ started.returnSummaryList)
          .sortBy(_.purchaserName)

      SdltInProgressReturnViewRow
        .convertResponseToViewRows(
          inProgressReturnsList
        )
    }
  }

  def getSubmittedReturns(implicit hc: HeaderCarrier, request: DataRequest[_]): Future[List[SdltSubmittedReturnsViewModel]] = {
    for {
      submitted          <- stampDutyLandTaxConnector.getReturns(Some("SUBMITTED"),            Some("SUBMITTED"), deletionFlag = false)
      submittedNoReceipt <- stampDutyLandTaxConnector.getReturns(Some("SUBMITTED_NO_RECEIPT"), Some("SUBMITTED"), deletionFlag = false)
    } yield {

      val submittedReturnsList =
        (submitted.returnSummaryList ++ submittedNoReceipt.returnSummaryList)
          .sortBy(_.purchaserName)

      SdltSubmittedReturnsViewModel
        .convertResponseToSubmittedView(
          submittedReturnsList
        )
    }
  }

  def getInProgressReturnsDueForDeletion(implicit hc: HeaderCarrier, request: DataRequest[_]): Future[List[ReturnSummary]] =
    stampDutyLandTaxConnector
      .getReturns(None, Some("IN-PROGRESS"), deletionFlag = true)
      .map(_.returnSummaryList
        .sortBy(_.purchaserName)
      )

  def getSubmittedReturnsDueForDeletion(implicit hc: HeaderCarrier, request: DataRequest[_]): Future[List[ReturnSummary]] =
    stampDutyLandTaxConnector
      .getReturns(None, Some("SUBMITTED"), deletionFlag = true)
      .map(_.returnSummaryList
        .sortBy(_.purchaserName)
      )

  def getAgentCount(implicit hc: HeaderCarrier, request: DataRequest[_]): Future[Int] =
    stampDutyLandTaxConnector
      .getSdltOrganisation
      .map(_.agents.length)
}
