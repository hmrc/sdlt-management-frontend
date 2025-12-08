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
import models.manage.SdltReturnRecordRequest
import models.requests.DataRequest
import models.responses.SdltReturnsViewModel.convertToViewModel
import models.responses.{SdltReturnBaseViewModel, SdltReturnViewModel}
import play.api.Logging
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class StampDutyLandTaxService @Inject()(stampDutyLandTaxConnector: StampDutyLandTaxConnector)
                                       (implicit executionContext: ExecutionContext) extends Logging {

  /*
  Unified way to extract returns from DB and convert returns to viewModel
   */
  def getReturnsByTypeViewModel[ViewModel <: SdltReturnBaseViewModel](storn: String,
                                extractType: SdltReturnTypes,
                                pageIndex: Option[Int])
                               (implicit hc: HeaderCarrier): Future[ViewModel] = {
    val dataRequest: SdltReturnRecordRequest = SdltReturnRecordRequest
      .convertToDataRequest(
        storn = storn,
        extractType = extractType,
        pageIndex = pageIndex)
    logger.info(s"[StampDutyLandTaxService][getReturnsByTypeViewModel] - GENERIC::RETURNS_DATA_REQUEST:: $dataRequest")
    for {
      dataResponse <- stampDutyLandTaxConnector.getReturns(dataRequest)
    } yield {
      logger.info(s"[StampDutyLandTaxService][getReturnsByTypeViewModel] - ${storn}::" +
        s"response r/count: ${dataResponse.returnSummaryCount} :: ${dataResponse.returnSummaryList.length}")
      val viewModel = convertToViewModel(dataResponse, extractType)
      viewModel.asInstanceOf[ViewModel]
    }
  }

  def getAgentCount(implicit hc: HeaderCarrier, request: DataRequest[_]): Future[Int] = {
    stampDutyLandTaxConnector
      .getSdltOrganisation
      .map(_.agents.length)
  }

}
