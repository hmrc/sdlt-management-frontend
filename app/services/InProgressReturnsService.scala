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
import models.responses.SdltInProgressReturnViewRow
import models.responses.SdltInProgressReturnViewRow.*
import play.api.Logger
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.*
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class InProgressReturnsService @Inject()(
                                          val stampDutyLandTaxConnector: StampDutyLandTaxConnector
                                        )(implicit ec: ExecutionContext) {

  def getAllReturns(storn: String)
                   (implicit hc: HeaderCarrier): Future[Either[Throwable, List[SdltInProgressReturnViewRow]]] = {
    Logger("application").info(s"[InProgressReturnsService][getAll] - get all returns")
    stampDutyLandTaxConnector.getAllReturns(storn).map { response =>
      Right(convertResponseToViewRows(response))
    }.recover{ ex =>
      Left(ex)
    }
  }

  def getPageRows(allDataRows: List[SdltInProgressReturnViewRow], pageIndex: Int, pageSize: Int): List[SdltInProgressReturnViewRow] = {
    allDataRows.grouped(pageSize).toSeq.lift(pageIndex - 1) match {
      case Some(sliceData) =>
        sliceData
      case None =>
        List.empty
    }
  }


}
