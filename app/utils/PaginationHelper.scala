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

package utils

import controllers.manage.InProgressReturnsController
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.pagination.{PaginationItem, PaginationLink}

trait PaginationHelper {

  val ROWS_ON_PAGE = 10

  def generatePaginationItems(paginationIndex: Int, numberOfPages: Int): Seq[PaginationItem] = {
    Range
      .inclusive(1, numberOfPages)
      .map(pageIndex =>
        PaginationItem(
          href = controllers.manage.routes.InProgressReturnsController.onPageLoad(Some(pageIndex)).url,
          number = Some(pageIndex.toString),
          visuallyHiddenText = None,
          current = Some(pageIndex == paginationIndex),
          ellipsis = None,
          attributes = Map.empty
        )
      )
  }

  def generatePreviousLink(paginationIndex: Int, numberOfPages: Int)
                          (implicit messages: Messages): Option[PaginationLink] = {
    if (paginationIndex == 1) None
    else {
      Some(
        PaginationLink(
          href = "",
          text = Some(messages("pagination.previous")),
          attributes = Map.empty
        )
      )
    }
  }

  def generateNextLink(paginationIndex: Int, numberOfPages: Int)
                      (implicit messages: Messages): Option[PaginationLink] = {
    if (paginationIndex == numberOfPages)
      None
    else {
      Some(
        PaginationLink(
          href = "",
          text = Some(messages("pagination.next")),
          attributes = Map.empty
        )
      )
    }
  }

  def getPaginationInfoText[A](paginationIndex: Int, itemList: Seq[A])
                              (implicit messages: Messages): Option[String] = {

    if (itemList.length <= ROWS_ON_PAGE || paginationIndex <= 0) {
      None
    }
    else {
      val paged = itemList.grouped(ROWS_ON_PAGE).toSeq
      paged.lift(paginationIndex - 1).map { detailsChunk =>
        val total = itemList.length
        val start = (paginationIndex - 1) * ROWS_ON_PAGE + 1
        val end = math.min(paginationIndex * ROWS_ON_PAGE, total)
        messages("manageReturns.inProgressReturns.paginationInfo", start, end, total)
      }
    }
  }

  def getPageCount(records: Int): Int = {
    if (records % ROWS_ON_PAGE == 0) {
      records / ROWS_ON_PAGE
    } else {
      (records / ROWS_ON_PAGE) + 1
    }
  }

}