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
import models.manage.{ReturnSummary, SdltReturnRecordResponse}
import models.responses.UniversalStatus.{ACCEPTED, STARTED}
import play.api.Logging

case class SdltReturnViewRow(
                              address: String,
                              agentReference: String,
                              purchaserName: String,
                              status: UniversalStatus
                            )

object SdltReturnViewRow extends Logging {

  import UniversalStatus.*

  def convertToViewRows(returnsList: List[ReturnSummary]): List[SdltReturnViewRow] = {
    {
      for {
        rec <- returnsList
      } yield {
        fromString(rec.status) match {
          case Right(status) =>
            Some(
              SdltReturnViewRow(
                address = rec.address,
                agentReference = rec.agentReference.getOrElse(""), // default agent ref to empty
                purchaserName = rec.purchaserName,
                status = status,
              )
            )
          case Left(ex) =>
            logger.error(s"[SdltReturnViewRow][convertToViewRows] - conversion from: ${rec} failure: $ex")
            None
        }
      }
    }.flatten
  }
}

case class SdltReturnViewModel(
                                extractType: SdltReturnTypes,
                                rows: List[SdltReturnViewRow],
                                totalRowCount: Option[Int])


object SdltReturnsViewModel {
  private val inProgressReturnStatuses: Seq[UniversalStatus] = Seq(STARTED, ACCEPTED)

  def convertToViewModel(response: SdltReturnRecordResponse, extractType: SdltReturnTypes): SdltReturnViewModel = {
    val rows: List[SdltReturnViewRow] = SdltReturnViewRow.convertToViewRows(response.returnSummaryList)

    extractType match {
      case SdltReturnTypes.IN_PROGRESS_RETURNS =>
        SdltReturnViewModel(
          extractType = extractType,
          rows = rows
            .filter(rec => inProgressReturnStatuses.contains(rec.status)),
          totalRowCount = response.returnSummaryCount
        )
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