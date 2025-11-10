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
import models.manage.SdltReturnRecordResponse
import models.responses.{SdltReturnViewRow, UniversalStatus}
import models.responses.UniversalStatus.*
import play.api.Logger
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.*
import scala.concurrent.{ExecutionContext, Future}


@Singleton
class InProgressReturnsService @Inject()(
                                          val stampDutyLandTaxConnector: StampDutyLandTaxConnector
                                        )(implicit ec: ExecutionContext) {

  private val acceptableStatus : Seq[UniversalStatus] = Seq(ACCEPTED, PENDING)

  private def mapResponseToViewRows(response: SdltReturnRecordResponse): List[SdltReturnViewRow] = {
    response.returnSummaryList.flatMap {
      rec =>
        fromString(rec.status)
          .filter(acceptableStatus.contains(_))
          .map { status =>
          SdltReturnViewRow(
            address = rec.address,
            agentReference = rec.agentReference,
            dateSubmitted = rec.dateSubmitted,
            utrn = rec.utrn,
            purchaserName = rec.purchaserName,
            status = status,
            returnReference = rec.returnReference
          )
        }
    }
  }

  def getAllReturns(storn: String)
                   (implicit hc: HeaderCarrier): Future[Either[Throwable, List[SdltReturnViewRow]]] = {
    Logger("application").info(s"[InProgressReturnsService][getAll] - get all returns")
    stampDutyLandTaxConnector.getAllReturns(storn).map { response =>
      Right(mapResponseToViewRows(response))
    }
  }

  def getPageRows(allDataRows: List[SdltReturnViewRow], pageIndex: Int, pageSize: Int): List[SdltReturnViewRow] = {
    val paged: Seq[Seq[SdltReturnViewRow]] = allDataRows.grouped(pageSize).toSeq
    paged.lift(pageIndex - 1) match {
      case Some(sliceData) =>
        sliceData.toList
      case None =>
        List.empty
    }
  }


}
