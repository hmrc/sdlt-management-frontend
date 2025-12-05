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
import models.manage.{ReturnSummary, SdltReturnRecordRequest}
import models.requests.DataRequest
import models.responses.{SdltInProgressReturnViewModel, SdltInProgressReturnViewRow, SdltSubmittedReturnsViewRow, SdltSubmittedReturnViewModel}
import play.api.Logging
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class StampDutyLandTaxService @Inject()(stampDutyLandTaxConnector: StampDutyLandTaxConnector)
                                       (implicit executionContext: ExecutionContext) extends Logging {

  def getInProgressReturnsViewModel(storn: String, pageIndex: Option[Int])
                                   (implicit hc: HeaderCarrier): Future[SdltInProgressReturnViewModel] = {
    val dataRequest: SdltReturnRecordRequest = SdltReturnRecordRequest(
      storn = storn,
      status = None,
      deletionFlag = false,
      pageType = Some("IN-PROGRESS"),
      pageNumber = pageIndex.map(_.toString))
    logger.info(s"[StampDutyLandTaxService][getInProgressReturnsViewModel] - data request:: ${dataRequest}")
    for {
      inProgressResponse <- stampDutyLandTaxConnector.getReturns(dataRequest)
    } yield {
      logger.info(s"[StampDutyLandTaxService][getInProgressReturnsViewModel] - ${storn}::" +
        s"response r/count: ${inProgressResponse.returnSummaryCount} :: ${inProgressResponse.returnSummaryList.length}")
      SdltInProgressReturnViewModel(
        rows = SdltInProgressReturnViewRow
          .convertResponseToReturnViewRows(
            inProgressResponse.returnSummaryList
          ),
        totalRowCount = inProgressResponse.returnSummaryCount
      )
    }
  }

  def getSubmittedReturnsViewModel(storn: String, pageIndex: Option[Int])
                                  (implicit hc: HeaderCarrier): Future[SdltSubmittedReturnViewModel] = {
    val dataRequest: SdltReturnRecordRequest = SdltReturnRecordRequest(
      storn = storn,
      status = None,
      deletionFlag = false,
      pageType = Some("SUBMITTED"),
      pageNumber = pageIndex.map(_.toString))
    logger.info(s"[StampDutyLandTaxService][getSubmittedReturnsViewModel] - data request:: ${dataRequest}")
    for {
      submittedResponse <- stampDutyLandTaxConnector.getReturns(dataRequest)
    } yield {
      logger.info(s"[StampDutyLandTaxService][getSubmittedReturnsViewModel] - ${storn}::" +
        s"response r/count: ${submittedResponse.returnSummaryCount} :: ${submittedResponse.returnSummaryList.length}")
      SdltSubmittedReturnViewModel(
        rows = SdltSubmittedReturnsViewRow
          .convertResponseToSubmittedView(
            submittedResponse.returnSummaryList
          ),
        totalRowCount = submittedResponse.returnSummaryCount
      )
    }
  }

  def getInProgressReturnsDueForDeletion(storn: String)
                                        (implicit hc: HeaderCarrier): Future[List[ReturnSummary]] = {
    stampDutyLandTaxConnector
      .getReturns(
        SdltReturnRecordRequest(
          storn = storn,
          deletionFlag = true,
          status = None,
          pageType = Some("IN-PROGRESS"),
          pageNumber = Some("1"))
      )
      .map(
        res => {
          logger.info(s"[StampDutyLandTaxService][getInProgressReturnsDueForDeletion] - ${storn}::response r/count: ${res.returnSummaryList.length}")
          res.returnSummaryList.sortBy(_.purchaserName)
        })
  }

  def getSubmittedReturnsDueForDeletion(storn: String)
                                       (implicit hc: HeaderCarrier): Future[List[ReturnSummary]] =
    stampDutyLandTaxConnector
      .getReturns(
        SdltReturnRecordRequest(
          storn = storn,
          deletionFlag = true,
          status = None,
          pageType = Some("SUBMITTED"),
          pageNumber = Some("1"))
      )
      .map(res => {
        logger.info(s"[StampDutyLandTaxService][getSubmittedReturnsDueForDeletion] - ${storn}::response r/count: ${res.returnSummaryList.length}")
        res.returnSummaryList
          .sortBy(_.purchaserName)
      })

  def getAgentCount(implicit hc: HeaderCarrier, request: DataRequest[_]): Future[Int] =
    stampDutyLandTaxConnector
      .getSdltOrganisation
      .map(_.agents.length)
}
