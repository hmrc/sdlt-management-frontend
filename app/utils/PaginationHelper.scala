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

import play.api.Logging
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.pagination.{Pagination,
  PaginationItem, PaginationLink}

trait PaginationHelper extends Logging {

  private val ROWS_ON_PAGE = 10
  private val DEFAULT_PAGE_INDEX = 1
  private val numberOfPages: Int => Int = totalRowCount => getPageCount(totalRowCount)

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

  def getPageCount(totalRecCount: Int): Int = {
    if (totalRecCount <= 0) {
      1
    } else if (totalRecCount % ROWS_ON_PAGE == 0) {
      totalRecCount / ROWS_ON_PAGE
    } else {
      (totalRecCount / ROWS_ON_PAGE) + 1
    }
  }

  def pageIndexSelector(userInputPageInput: Option[Int],
                        rowsCount: Int): Either[Throwable, Int] = {
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

  def paginationItems(
                       currentPage: Int,
                       totalPages: Int,
                       urlSelector: Int => String,
                       visibleBefore: Int = 1,
                       visibleAfter: Int = 1
                     ): Seq[PaginationItem] = {

    val middle = (currentPage - visibleBefore).max(1) to (currentPage + visibleAfter).min(totalPages)

    val start = middle.start match
      case s if s == 3 => Seq("1", "2")
      case s if s > 2 => Seq("1", "...")
      case s if s > 1 => Seq("1")
      case _ => Seq.empty

    val end = middle.end match
      case e if e == totalPages - 2 => Seq((totalPages - 1).toString, totalPages.toString)
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

  def getPaginationWithInfoText[A](
                                   rows: List[A],
                                   totalRowCount: Int,
                                   paginationIndex: Option[Int],
                                   urlSelector: Int => String
                                 )(implicit messages: Messages): Option[(List[A], Option[Pagination], Option[String])] = {

    pageIndexSelector(paginationIndex, totalRowCount) match {
      case Right(validIndex) =>

        val pagination = Option.when(
          totalRowCount > 0 && numberOfPages(totalRowCount) > 1
        )(
          Pagination(
            items    = Some(paginationItems( validIndex, numberOfPages(totalRowCount), urlSelector                )),
            previous = generatePreviousLink( validIndex, numberOfPages(totalRowCount), urlSelector(validIndex - 1 )),
            next     = generateNextLink(     validIndex, numberOfPages(totalRowCount), urlSelector(validIndex + 1 )),
          )
        )

        val paginationText = Option.unless(
          totalRowCount <= ROWS_ON_PAGE || validIndex <= 0
        ){
          val total = totalRowCount
          val start = (validIndex - 1) * ROWS_ON_PAGE + 1
          val end   = math.min(validIndex * ROWS_ON_PAGE, total)
          messages("manageReturns.inProgressReturns.paginationInfo", start, end, total)
        }

        Some((rows, pagination, paginationText))

      case Left(error) =>
        logger.warn(s"[getPaginationWithInfoText] Invalid page index '$paginationIndex' for $totalRowCount rows: ${error.getMessage}.")
        None
    }
  }
}