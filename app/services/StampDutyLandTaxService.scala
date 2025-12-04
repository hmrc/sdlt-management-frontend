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
import models.SdltReturnTypes
import models.manage.{ReturnSummary, SdltReturnRecordRequest}
import models.requests.DataRequest
import models.responses.SdltReturnsViewModel.*
import models.responses.{SdltReturnViewModel, SdltReturnsViewModel}
import play.api.Logging
import uk.gov.hmrc.http.HeaderCarrier
import viewmodels.manage.SdltSubmittedReturnsViewModel
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class StampDutyLandTaxService @Inject()(stampDutyLandTaxConnector: StampDutyLandTaxConnector)
                                       (implicit executionContext: ExecutionContext) extends Logging {

  /*
  Unified way to extract returns from DB and convert returns to viewModel
   */
  def getReturnsByTypeViewModel(storn: String,
                                extractType: SdltReturnTypes,
                                pageIndex: Option[Int])
                               (implicit hc: HeaderCarrier): Future[SdltReturnViewModel] = {
    val dataRequest: SdltReturnRecordRequest = SdltReturnRecordRequest
      .convertToDataRequest(
        storn = storn,
        extractType = extractType,
        pageIndex = pageIndex)
    logger.info(s"[StampDutyLandTaxService][getReturns] - GENERIC::DATA_REQUEST:: $dataRequest")
    for {
      dataResponse <- stampDutyLandTaxConnector.getReturns(dataRequest)
    } yield {
      logger.info(s"[StampDutyLandTaxService][getReturns] - ${storn}::" +
        s"response r/count: ${dataResponse.returnSummaryCount} :: ${dataResponse.returnSummaryList.length}")
      convertToViewModel(dataResponse, extractType)
    }
  }

  @deprecated("Please use getReturns instead")
  def getSubmittedReturns(storn: String)
                         (implicit hc: HeaderCarrier): Future[List[SdltSubmittedReturnsViewModel]] = {
    for {
      submitted <- stampDutyLandTaxConnector.getReturns(
        SdltReturnRecordRequest(
          storn = storn,
          deletionFlag = false,
          status = Some("SUBMITTED"),
          pageType = Some("SUBMITTED"), pageNumber = Some("1"))
      )
      submittedNoReceipt <- stampDutyLandTaxConnector.getReturns(
        SdltReturnRecordRequest(
          storn = storn,
          deletionFlag = false,
          status = Some("SUBMITTED_NO_RECEIPT"),
          pageType = Some("SUBMITTED"), pageNumber = Some("1"))
      )
    } yield {
      logger.info(s"[StampDutyLandTaxService][getSubmittedReturns] - ${storn}::" +
        s"response r/count: ${submitted.returnSummaryList.length} :: ${submittedNoReceipt.returnSummaryList.length}")

      val submittedReturnsList =
        (submitted.returnSummaryList ++ submittedNoReceipt.returnSummaryList)
          .sortBy(_.purchaserName)

      SdltSubmittedReturnsViewModel
        .convertResponseToSubmittedView(
          submittedReturnsList
        )
    }
  }

  @deprecated("Please use getReturns instead")
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

  @deprecated("Please use getReturns instead")
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
