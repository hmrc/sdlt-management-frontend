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

import models.requests.DataRequest
import play.api.Logging
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.pagination.{Pagination, PaginationItem, PaginationLink}

trait PaginationHelper extends Logging {

  private val ROWS_ON_PAGE = 10
  private val DEFAULT_PAGE_INDEX = 1

  private def slidingTopIndex(paginationIndex: Int, numberOfPages: Int): Int = {
    if (numberOfPages - paginationIndex > 10) {
      paginationIndex + 5
    } else {
      numberOfPages
    }
  }

  @deprecated("doesn't work for small number of pages -> use paginationItems")
  def generatePaginationItems(paginationIndex: Int, numberOfPages: Int,
                              urlSelector: Int => String): Seq[PaginationItem] = {
    Range
      .inclusive(paginationIndex, slidingTopIndex(paginationIndex, numberOfPages)) // This a primitive fix:: we might apply sliding logic in the future
      .map(pageIndex =>
        PaginationItem(
          href = urlSelector(pageIndex),
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

  @deprecated("this would be effectively wrong in the new way of pagination")
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

  def getPaginationInfoTextV2(paginationIndex: Int,
                              totalRecCount: Int
                             )(implicit messages: Messages): Option[String] = {

    if (totalRecCount <= ROWS_ON_PAGE || paginationIndex <= 0) {
      None
    } else {
      val total = totalRecCount
      val start = (paginationIndex - 1) * ROWS_ON_PAGE + 1
      val end = math.min(paginationIndex * ROWS_ON_PAGE, total)
      Some(messages("manageReturns.inProgressReturns.paginationInfo", start, end, total))
    }
  }

  def getPageCount(totalRecCount: Int): Int = {
    if (totalRecCount <= 0) {
      1
    } else if (totalRecCount % ROWS_ON_PAGE == 0) {
      totalRecCount / ROWS_ON_PAGE
    } else {
      (totalRecCount / ROWS_ON_PAGE) + 1
    }
  }

  // TODO: add test coverage
  def pageIndexSelector(userInputPageInput: Option[Int], rowsCount: Int): Either[Throwable, Int] = {
    userInputPageInput
      .map { attemptToSelectIndex =>
        if (attemptToSelectIndex > getPageCount(rowsCount)) {
          Left(new Error("PageIndex selected is out of scope"))
        } else if (attemptToSelectIndex < DEFAULT_PAGE_INDEX) {
          Left(new Error("PageIndex selected is out of scope"))
        } else {
          Right(attemptToSelectIndex)
        }
      }
      .getOrElse(Right(DEFAULT_PAGE_INDEX))
  }
 
  @deprecated("does not use ellipsis")
  def createPagination(pageIndex: Int, totalRowsCount: Int, urlSelector: Int => String)
                      (implicit messages: Messages): Option[Pagination] = {
    val numberOfPages: Int = getPageCount(totalRowsCount)
    if (totalRowsCount > 0 && numberOfPages > 1) {
      Some(
        Pagination(
          items = Some(generatePaginationItems(pageIndex, numberOfPages, urlSelector)),
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

  @deprecated("Will be removed as not required::pagination is done on the DB level")
  def getSelectedPageRows[A](allDataRows: List[A], pageIndex: Int): List[A] = {
    allDataRows.grouped(ROWS_ON_PAGE).toSeq.lift(pageIndex - 1) match {
      case Some(sliceData) =>
        sliceData
      case None =>
        List.empty
    }
  }

  def paginateList[A](allDataRows: List[A], paginationIndex: Option[Int], urlSelector: Int => String)(implicit request: DataRequest[_], messages: Messages): Either[String, (
    (List[A], Option[Pagination], Option[String]))] = {

    val selectedPageIndex: Int = paginationIndex.getOrElse(1)

    val paginator: Option[Pagination] = createPagination(selectedPageIndex, allDataRows.length, urlSelector)
    val paginationText: Option[String] = getPaginationInfoText(selectedPageIndex, allDataRows)
    val rowsForSelectedPage: List[A] = getSelectedPageRows(allDataRows, selectedPageIndex)

    Right(rowsForSelectedPage, paginator, paginationText)

  }

  @deprecated
  def paginateIfValidPageIndex[A](
                                   rowsOpt: Option[List[A]],
                                   paginationIndex: Option[Int],
                                   urlSelector: Int => String
                                 )(
                                   implicit req: DataRequest[_],
                                   messages: Messages
                                 ): Option[Either[String, (List[A], Option[Pagination], Option[String])]] =
    rowsOpt match {
      case None => None
      case Some(Nil) => Some(Right((Nil, None, None)))
      case Some(rows) =>
        pageIndexSelector(paginationIndex, rows.length) match {
          case Right(validIndex) =>
            Some(paginateList(rows, Some(validIndex), urlSelector))

          case Left(error) =>
            logger.warn(
              s"[paginateIfValidPageIndex] Invalid page index '$paginationIndex' " +
                s"for ${rows.length} rows: ${error.getMessage}."
            )
            Some(Left(error.getMessage))
        }
    }

  def paginationItems(
                       currentPage: Int,
                       totalPages: Int,
                       visibleBefore: Int = 1,
                       visibleAfter: Int = 1,
                       urlSelector: Int => String
                     ): Seq[PaginationItem] = {

    val middle = (currentPage - visibleBefore).max(1) to (currentPage + visibleAfter).min(totalPages)

    val start = middle.start match
      case s if s == 3 => Seq("1", "2")
      case s if s > 2 => Seq("1", "...")
      case s if s > 1 => Seq("1")
      case _ => Seq.empty

    val end = middle.end match
      case e if e == totalPages - 1 => Seq((totalPages - 1).toString, totalPages.toString)
      case e if e < totalPages - 1 => Seq("...", totalPages.toString)
      case e if e < totalPages => Seq(totalPages.toString)
      case _ => Seq.empty

    val labels = start ++ middle.map(_.toString) ++ end

    labels.map {
      case "..." =>
        PaginationItem(
          href = "#",
          ellipsis = Some(true)
        )
      case s =>
        val page = s.toInt
        PaginationItem(
          href = urlSelector(page),
          number = Some(s),
          current = Some(page == currentPage)
        )
    }
  }
  
  def createPaginationV2(
                          pageIndex: Int,
                          totalRowsCount: Int,
                          urlSelector: Int => String
                        )(implicit messages: Messages): Option[Pagination] = {

    val numberOfPages: Int = getPageCount(totalRowsCount)

    if (totalRowsCount > 0 && numberOfPages > 1) {
      Some(
        Pagination(
          items = Some(
            paginationItems(
              currentPage = pageIndex,
              totalPages = numberOfPages,
              urlSelector = urlSelector
            )
          ),
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
  
  def paginateIfValidPageIndexV2[A](
                                   rowsOpt: Option[List[A]],
                                   paginationIndex: Option[Int],
                                   urlSelector: Int => String
                                 )(
                                   implicit req: DataRequest[_],
                                   messages: Messages
                                 ): Option[Either[String, (List[A], Option[Pagination], Option[String])]] =
    rowsOpt match {
      case None => None
      case Some(Nil) => Some(Right((Nil, None, None)))
      case Some(rows) =>
        pageIndexSelector(paginationIndex, rows.length) match {
          case Right(validIndex) =>
            Some(paginateList(rows, Some(validIndex), urlSelector))

          case Left(error) =>
            logger.warn(
              s"[paginateIfValidPageIndex] Invalid page index '$paginationIndex' " +
                s"for ${rows.length} rows: ${error.getMessage}."
            )
            Some(Left(error.getMessage))
        }
    }
}
