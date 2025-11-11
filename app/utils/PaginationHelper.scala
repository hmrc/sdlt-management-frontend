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

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.pagination.{Pagination, PaginationItem, PaginationLink}

trait PaginationHelper {

  private val ROWS_ON_PAGE = 10

  def generatePaginationItems(paginationIndex: Int, numberOfPages: Int, url: String): Seq[PaginationItem] = {
    Range
      .inclusive(1, numberOfPages)
      .map(pageIndex =>
        PaginationItem(
          href = url,
          number = Some(pageIndex.toString),
          visuallyHiddenText = None,
          current = Some(pageIndex == paginationIndex),
          ellipsis = None,
          attributes = Map.empty
        )
      )
  }

  def generatePreviousLink(paginationIndex: Int, numberOfPages: Int, urlPrev: String)
                          (implicit messages: Messages): Option[PaginationLink] = {
    if (paginationIndex == 1) {
      None
    }
    else {
      Some(
        PaginationLink(
          href = urlPrev,
          text = Some(messages("pagination.previous")),
          attributes = Map.empty
        )
      )
    }
  }

  def generateNextLink(paginationIndex: Int, numberOfPages: Int, urlNext: String)
                      (implicit messages: Messages): Option[PaginationLink] = {
    if (paginationIndex == numberOfPages) {
      None
    } else {
      Some(
        PaginationLink(
          href = urlNext,
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

  private def getPageCount(records: Int): Int = {
    if (records % ROWS_ON_PAGE == 0) {
      records / ROWS_ON_PAGE
    } else {
      (records / ROWS_ON_PAGE) + 1
    }
  }

  def createPagination(pageIndex: Int, totalRowsCount : Int, urlSelector: Int => String )
                              (implicit messages: Messages): Option[Pagination] = {
    val numberOfPages: Int = getPageCount(totalRowsCount)
    if (totalRowsCount > 0 && numberOfPages > 1) {
      Some(
        Pagination(
          items = Some(generatePaginationItems(pageIndex, numberOfPages, urlSelector(pageIndex) )),
          previous = generatePreviousLink(pageIndex, numberOfPages, urlSelector(pageIndex - 1)),
          next = generateNextLink(pageIndex, numberOfPages, urlSelector(pageIndex + 1)),
          landmarkLabel = None,
          classes = "",
          attributes = Map.empty
        )
      )
    } else {
      None
    }
  }

  def getSelectedPageRows[A](allDataRows: List[A], pageIndex: Int): List[A] = {
    allDataRows.grouped(ROWS_ON_PAGE).toSeq.lift(pageIndex - 1) match {
      case Some(sliceData) =>
        sliceData
      case None =>
        List.empty
    }
  }
}