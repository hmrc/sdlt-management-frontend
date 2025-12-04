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

package models.responses

import models.SdltReturnTypes
import models.manage.{ReturnSummary, SdltReturnRecordRequest, SdltReturnRecordResponse}
import play.api.Logging

sealed trait SdltReturnsViewModel

case class SdltInProgressReturnViewModel(rows: List[SdltInProgressReturnViewRow], totalRowCount: Option[Int]) extends SdltReturnsViewModel

case class SdltInProgressReturnViewRow(
                                        address: String,
                                        agentReference: String,
                                        purchaserName: String,
                                        status: UniversalStatus
                                      )

object SdltInProgressReturnViewRow extends Logging {

  import UniversalStatus.*

  private val inProgressReturnStatuses: Seq[UniversalStatus] = Seq(STARTED, ACCEPTED)

  def convertToReturnViewRows(inProgressReturnsList: List[ReturnSummary]): List[SdltInProgressReturnViewRow] = {
    val res = for {
      rec <- inProgressReturnsList
    } yield {
      fromString(rec.status) match {
        case Right(status) =>
          Some(
            SdltInProgressReturnViewRow(
              address = rec.address,
              agentReference = rec.agentReference.getOrElse(""), // default agent ref to empty
              purchaserName = rec.purchaserName,
              status = status,
            )
          )
        case Left(ex) =>
          logger.error(s"[SdltInProgressReturnViewRow][convertResponseToViewRows] - conversion from: ${rec} failure: $ex")
          None
      }
    }
    res
      .flatten
      .filter(rec => inProgressReturnStatuses.contains(rec.status))
  }
}

object SdltReturnsViewModel {
  def convertToViewModel(response: SdltReturnRecordResponse, extractType: SdltReturnTypes): SdltReturnsViewModel = {
    extractType match {
      case SdltReturnTypes.IN_PROGRESS_RETURNS =>
        SdltInProgressReturnViewModel(
          rows = SdltInProgressReturnViewRow.convertToReturnViewRows(response.returnSummaryList),
          totalRowCount = response.returnSummaryCount
        )
        ???
      case SdltReturnTypes.SUBMITTED_SUBMITTED_RETURNS =>
        ???
      case SdltReturnTypes.SUBMITTED_NO_RECEIPT_RETURNS =>
        ???
      case SdltReturnTypes.IN_PROGRESS_RETURNS_DUR_FOR_DELETION =>
        ???
      case SdltReturnTypes.SUBMITTED_RETURNS_DUR_FOR_DELETION =>
        ???
    }
  }
}