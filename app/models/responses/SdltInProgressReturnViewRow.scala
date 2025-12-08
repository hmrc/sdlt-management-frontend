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
import utils.PaginationHelper

case class SdltReturnViewRow(
                              address: String,
                              agentReference: String,
                              purchaserName: String,
                              status: UniversalStatus,
                              utrn: String
                            )

case class SdltInProgressReturnViewModel(
                                extractType: SdltReturnTypes,
                                rows: List[SdltReturnViewRow],
                                totalRowCount: Option[Int]) extends SdltReturnBaseViewModel

case class SdltSubmittedReturnViewModel(
                                                       extractType: SdltReturnTypes,
                                                       rows: List[SdltReturnViewRow],
                                                       totalRowCount: Option[Int]) extends SdltReturnBaseViewModel

case class SdltSubmittedDueForDeletionReturnViewModel(
                                extractType: SdltReturnTypes,
                                rows: List[SdltReturnViewRow],
                                totalRowCount: Option[Int]) extends SdltReturnBaseViewModel

case class SdltInProgressDueForDeletionViewModel(
                                                       extractType: SdltReturnTypes,
                                                       rows: List[SdltReturnViewRow],
                                                       totalRowCount: Option[Int]) extends SdltReturnBaseViewModel

@deprecated("To be removed :: view models type should be 1:1 for each view")
case class SdltReturnViewModel(
                                extractType: SdltReturnTypes,
                                rows: List[SdltReturnViewRow],
                                totalRowCount: Option[Int]) extends SdltReturnBaseViewModel

abstract class SdltReturnBaseViewModel extends PaginationHelper

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
                utrn = rec.utrn.getOrElse("")
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


object SdltReturnsViewModel {
  private val inProgressReturnStatuses: Seq[UniversalStatus] = Seq(STARTED, ACCEPTED)

  def convertToViewModel(response: SdltReturnRecordResponse, extractType: SdltReturnTypes): SdltReturnBaseViewModel = {
    val rows: List[SdltReturnViewRow] = SdltReturnViewRow.convertToViewRows(response.returnSummaryList)

    extractType match {
      case SdltReturnTypes.IN_PROGRESS_RETURNS =>
        SdltInProgressReturnViewModel(
          extractType = extractType,
          rows = rows
            .filter(rec => inProgressReturnStatuses.contains(rec.status)),
          totalRowCount = response.returnSummaryCount
        )
      case SdltReturnTypes.SUBMITTED_SUBMITTED_RETURNS =>
        SdltSubmittedReturnViewModel(
          extractType = extractType,
          rows = rows,
          totalRowCount = response.returnSummaryCount
        )
      case SdltReturnTypes.SUBMITTED_NO_RECEIPT_RETURNS =>
        SdltReturnViewModel(
          extractType = extractType,
          rows = rows,
          totalRowCount = response.returnSummaryCount
        )
      case SdltReturnTypes.IN_PROGRESS_RETURNS_DUE_FOR_DELETION =>
        SdltInProgressDueForDeletionViewModel(
          extractType = extractType,
          rows = rows,
          totalRowCount = response.returnSummaryCount
        )
      case SdltReturnTypes.SUBMITTED_RETURNS_DUE_FOR_DELETION =>
        SdltSubmittedDueForDeletionReturnViewModel(
          extractType = extractType,
          rows = rows.sortBy(_.purchaserName), // TODO: move sorting to the view level
          // TODO: any filtering || .filter(rec => inProgressReturnStatuses.contains(rec.status)),
          totalRowCount = response.returnSummaryCount
        )
    }

  }
}