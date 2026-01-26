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
import models.responses.UniversalStatus.{ACCEPTED, STARTED, SUBMITTED, SUBMITTED_NO_RECEIPT}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.pagination.Pagination
import utils.LoggerUtil.logError
import utils.PageUrlSelector.{dueForDeletionInProgressUrlSelector, dueForDeletionSubmittedUrlSelector, inProgressUrlSelector, submittedUrlSelector}
import utils.{PageUrlSelector, PaginationHelper}

// TODO: still some refactoring work required for each pagination method within SdltReturnBaseViewModel models

case class SdltInProgressReturnViewModel(
                                          extractType: SdltReturnTypes,
                                          rows: List[SdltReturnViewRow],
                                          selectedPageIndex: Int,
                                          totalRowCount: Int) extends SdltReturnBaseViewModel {

  def pagination(implicit messages: Messages): Option[Pagination] =
    getPaginationWithInfoText(rows, totalRowCount, Some(selectedPageIndex), inProgressUrlSelector)
      .collect {
        case (_, maybePaginator, _) => maybePaginator
      }.flatten

  def paginationInfoText(implicit messages: Messages): Option[String] =
    getPaginationWithInfoText(rows, totalRowCount, Some(selectedPageIndex), inProgressUrlSelector)
      .collect {
        case (_, _, maybePaginationText) => maybePaginationText
      }.flatten

}

case class SdltSubmittedReturnViewModel(
                                         extractType: SdltReturnTypes,
                                         rows: List[SdltReturnViewRow],
                                         selectedPageIndex: Int,
                                         totalRowCount: Int) extends SdltReturnBaseViewModel {

  def paginator(implicit messages: Messages): Option[Pagination] = {
    getPaginationWithInfoText(rows, totalRowCount, Some(selectedPageIndex), submittedUrlSelector)
      .collect {
        case (_, maybePaginator, _) => maybePaginator
      }.flatten
  }

  def paginationText(implicit messages: Messages): Option[String] = {
    getPaginationWithInfoText(rows, totalRowCount, Some(selectedPageIndex), submittedUrlSelector)
      .collect {
        case (_, _, maybePaginationText) => maybePaginationText
      }.flatten
  }

}


case class SdltDueForDeletionReturnViewModel(
                                              inProgressSelectedPageIndex: Option[Int],
                                              submittedSelectedPageIndex: Option[Int],
                                              inProgressViewModel: SdltInProgressDueForDeletionReturnViewModel,
                                              submittedViewModel: SdltSubmittedDueForDeletionReturnViewModel) extends SdltReturnBaseViewModel {

  val isEmpty :Boolean = inProgressViewModel.rows.isEmpty && submittedViewModel.rows.isEmpty

  def paginatorInProgress(implicit messages: Messages): Option[Pagination] = {
    getPaginationWithInfoText(inProgressViewModel.rows, inProgressViewModel.totalRowCount,
      inProgressSelectedPageIndex, dueForDeletionInProgressUrlSelector(submittedSelectedPageIndex))
      .collect {
        case (_, maybePaginator, _) => maybePaginator
      }.flatten
  }

  def paginationTexInProgress(implicit messages: Messages): Option[String] = {
    getPaginationWithInfoText(inProgressViewModel.rows, inProgressViewModel.totalRowCount,
      inProgressSelectedPageIndex, dueForDeletionInProgressUrlSelector(submittedSelectedPageIndex))
      .collect {
        case (_, _, maybePaginationText) => maybePaginationText
      }.flatten
  }

  def paginatorSubmitted(implicit messages: Messages): Option[Pagination] = {
    getPaginationWithInfoText(submittedViewModel.rows, submittedViewModel.totalRowCount,
      submittedSelectedPageIndex, dueForDeletionSubmittedUrlSelector(inProgressSelectedPageIndex))
      .collect {
        case (_, maybePaginator, _) => maybePaginator
      }.flatten
  }

  def paginationTexSubmitted(implicit messages: Messages): Option[String] = {
    getPaginationWithInfoText(submittedViewModel.rows, submittedViewModel.totalRowCount,
      submittedSelectedPageIndex, dueForDeletionSubmittedUrlSelector(inProgressSelectedPageIndex))
      .collect {
        case (_, _, maybePaginationText) => maybePaginationText
      }.flatten
  }

}

// Dependant models below :: TODO: wrap these under the main model??
case class SdltSubmittedDueForDeletionReturnViewModel(
                                                       extractType: SdltReturnTypes,
                                                       rows: List[SdltReturnViewRow],
                                                       totalRowCount: Int) extends SdltReturnBaseViewModel

case class SdltInProgressDueForDeletionReturnViewModel(
                                                        extractType: SdltReturnTypes,
                                                        rows: List[SdltReturnViewRow],
                                                        totalRowCount: Int) extends SdltReturnBaseViewModel

abstract class SdltReturnBaseViewModel extends PaginationHelper

case class SdltReturnViewRow(
                              address: String,
                              agentReference: String,
                              purchaserName: String,
                              status: UniversalStatus,
                              utrn: String
                            )


object SdltReturnViewRow  {

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
            logError(s"[SdltReturnViewRow][convertToViewRows] - conversion from: ${rec} failure: $ex")
            None
        }
      }
    }.flatten
  }
}

object SdltReturnsViewModel {
  private val inProgressReturnStatuses: Seq[UniversalStatus] = Seq(STARTED, ACCEPTED)
  private val submittedReturnsStatuses: Seq[UniversalStatus] = Seq(SUBMITTED, SUBMITTED_NO_RECEIPT)

  def convertToViewModel(response: SdltReturnRecordResponse,
                         extractType: SdltReturnTypes, selectedPageIndex: Int): SdltReturnBaseViewModel = {
    val rows: List[SdltReturnViewRow] = SdltReturnViewRow.convertToViewRows(response.returnSummaryList)

    extractType match {
      case SdltReturnTypes.IN_PROGRESS_RETURNS =>
        SdltInProgressReturnViewModel(
          extractType = extractType,
          rows = rows
            .filter(rec => inProgressReturnStatuses.contains(rec.status))
            .sortBy(_.purchaserName),
          selectedPageIndex = selectedPageIndex,
          totalRowCount = response.returnSummaryCount
        )
      case SdltReturnTypes.SUBMITTED_SUBMITTED_RETURNS | SdltReturnTypes.SUBMITTED_NO_RECEIPT_RETURNS =>
        SdltSubmittedReturnViewModel(
          extractType = extractType,
          rows = rows
            .filter(rec => submittedReturnsStatuses.contains(rec.status)),
          selectedPageIndex = selectedPageIndex,
          totalRowCount = response.returnSummaryCount
        )
      case SdltReturnTypes.IN_PROGRESS_RETURNS_DUE_FOR_DELETION =>
        SdltInProgressDueForDeletionReturnViewModel(
          extractType = extractType,
          rows = rows,
          totalRowCount = response.returnSummaryCount
        )
      case SdltReturnTypes.SUBMITTED_RETURNS_DUE_FOR_DELETION =>
        SdltSubmittedDueForDeletionReturnViewModel(
          extractType = extractType,
          rows = rows.sortBy(_.purchaserName),
          totalRowCount = response.returnSummaryCount
        )
    }

  }
}