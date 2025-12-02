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

import base.SpecBase
import models.UserAnswers
import models.requests.DataRequest
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.Application
import play.api.i18n.Messages
import play.api.test.FakeRequest
import play.api.test.Helpers.stubMessages

class PaginationHelperSpec extends AnyFreeSpec with Matchers with SpecBase{

  trait Fixture extends PaginationHelper {
    val urlSelector: Int => String = (pageIndex: Int) => controllers.manage.routes.InProgressReturnsController.onPageLoad(Some(pageIndex)).url
    val application: Application = applicationBuilder(userAnswers = None).build()
    val paginationIndexSelectedMidPage: Int = 5
    val paginationIndexSelectedFirstPage: Int = 1

    implicit val messages: Messages = stubMessages()
    implicit val request: DataRequest[_] = DataRequest(
      request = FakeRequest(),
      userId = "some-id",
      userAnswers = UserAnswers(id = "id"),
      storn = "STN001"
    )
  }

  "PageItems" - {

    "Get unique pagination items :: InProgressReturnsController" in new Fixture {
      val pageCount : Int = 10
      val items = generatePaginationItems(paginationIndex = 1, numberOfPages = pageCount, urlSelector = urlSelector)
      items.length mustBe pageCount
      items.map(_.href).toList mustBe (1 to 10).map(index =>
        s"/stamp-duty-land-tax-management/manage-returns/in-progress-returns?index=$index").toList
      items.map(_.href).toSet.toList.length mustBe pageCount
    }

    "Get prev pagination link" in new Fixture {
      val pageCount: Int = 10

      // render prev link
      generatePreviousLink(paginationIndex = paginationIndexSelectedMidPage,
        numberOfPages = pageCount,
        urlPrev = "prevLink")
        .toList.nonEmpty mustBe true

      // should not render prev link
      generatePreviousLink(paginationIndex = paginationIndexSelectedFirstPage,
        numberOfPages = pageCount,
        urlPrev = "prevLink")
        .toList.isEmpty mustBe true
    }

    "Get next pagination link" in new Fixture {
      val pageCount: Int = 10

      generateNextLink(paginationIndex = pageCount,
        numberOfPages = pageCount,
        urlNext = "prevLink")
        .toList.isEmpty mustBe true

      generateNextLink(paginationIndex = pageCount,
        numberOfPages = paginationIndexSelectedFirstPage,
        urlNext = "prevLink")
        .toList.nonEmpty mustBe true
    }
  }

  "pageIndexSelector" - {

    "returns default page index when user input is None" in new Fixture {
      pageIndexSelector(None, rowsCount = 25) mustBe Right(1)
    }

    "returns the same index when within range for single page of data" in new Fixture {
      pageIndexSelector(Some(1), rowsCount = 7) mustBe Right(1)
    }

    "returns the same index when within range for multiple pages of data" in new Fixture {
      pageIndexSelector(Some(2), rowsCount = 11) mustBe Right(2)
    }

    "returns an error when index is below minimum page" in new Fixture {
      val result = pageIndexSelector(Some(0), rowsCount = 20)
      result.isLeft mustBe true
    }

    "returns an error when index is greater than max page for exact page size" in new Fixture {
      val result = pageIndexSelector(Some(2), rowsCount = 10)
      result.isLeft mustBe true
    }

    "returns an error when index is greater than max page across multiple pages" in new Fixture {
      val result = pageIndexSelector(Some(4), rowsCount = 21)
      result.isLeft mustBe true
    }
  }

  "getPaginationInfoText" - {

    "returns None when total items fit on a single page" in new Fixture {
      val rows = (1 to 10).toList
      getPaginationInfoText(paginationIndex = 1, itemList = rows) mustBe None
    }

    "returns None when page index is zero or negative" in new Fixture {
      val rows = (1 to 25).toList
      getPaginationInfoText(paginationIndex = 0, itemList = rows) mustBe None
      getPaginationInfoText(paginationIndex = -1, itemList = rows) mustBe None
    }

    "returns correct info text for second page of multiple pages" in new Fixture {
      val rows = (1 to 25).toList
      val result = getPaginationInfoText(paginationIndex = 2, itemList = rows)

      val expected = Some(messages("manageReturns.inProgressReturns.paginationInfo", 11, 20, 25))
      result mustBe expected
    }

    "returns correct info text for last page with partial data" in new Fixture {
      val rows = (1 to 23).toList
      val result = getPaginationInfoText(paginationIndex = 3, itemList = rows)

      val expected = Some(messages("manageReturns.inProgressReturns.paginationInfo", 21, 23, 23))
      result mustBe expected
    }

    "returns None when requested page has no slice" in new Fixture {
      val rows = (1 to 15).toList
      getPaginationInfoText(paginationIndex = 3, itemList = rows) mustBe None
    }
  }

  "createPagination" - {

    "returns None when there are no rows" in new Fixture {
      val result = createPagination(pageIndex = 1, totalRowsCount = 0, urlSelector = urlSelector)
      result mustBe None
    }

    "returns None when rows fit on a single page" in new Fixture {
      val result = createPagination(pageIndex = 1, totalRowsCount = 10, urlSelector = urlSelector)
      result mustBe None
    }

    "returns a Pagination when there are more rows than a single page" in new Fixture {
      val result = createPagination(pageIndex = 1, totalRowsCount = 11, urlSelector = urlSelector)

      result.isDefined mustBe true
      val pagination = result.value

      pagination.items.isDefined mustBe true
      pagination.items.get.length mustBe 2
      pagination.previous mustBe None
      pagination.next.isDefined mustBe true
    }

    "marks the current page correctly in the items" in new Fixture {
      val result = createPagination(pageIndex = 2, totalRowsCount = 25, urlSelector = urlSelector)

      val items = result.value.items.get
      items.map(_.current) mustBe Seq(
        Some(false),
        Some(true),
        Some(false)
      )
    }
  }

  "getSelectedPageRows" - {

    "returns rows for the first page" in new Fixture {
      val rows = (1 to 25).toList
      val selected = getSelectedPageRows(rows, pageIndex = 1)
      selected mustBe (1 to 10).toList
    }

    "returns rows for a middle page" in new Fixture {
      val rows = (1 to 25).toList
      val selected = getSelectedPageRows(rows, pageIndex = 2)
      selected mustBe (11 to 20).toList
    }

    "returns rows for the last partial page" in new Fixture {
      val rows = (1 to 25).toList
      val selected = getSelectedPageRows(rows, pageIndex = 3)
      selected mustBe (21 to 25).toList
    }

    "returns empty list when page index is out of range" in new Fixture {
      val rows = (1 to 5).toList
      val selected = getSelectedPageRows(rows, pageIndex = 2)
      selected mustBe Nil
    }
  }

  "paginateList" - {

    "defaults to first page when pagination index is None" in new Fixture {
      val rows = (1 to 25).toList
      val result = paginateList(rows, paginationIndex = None, urlSelector = urlSelector)

      result.isRight mustBe true
      val (pageRows, paginator, infoText) = result.toOption.get

      pageRows mustBe (1 to 10).toList
      paginator.isDefined mustBe true
      infoText.isDefined mustBe true
    }

    "returns the correct slice and metadata for a middle page" in new Fixture {
      val rows = (1 to 25).toList
      val result = paginateList(rows, paginationIndex = Some(2), urlSelector = urlSelector)

      result.isRight mustBe true
      val (pageRows, paginator, infoText) = result.toOption.get

      pageRows mustBe (11 to 20).toList
      paginator.isDefined mustBe true
      infoText mustBe Some(messages("manageReturns.inProgressReturns.paginationInfo", 11, 20, 25))
    }

    "returns only rows and no pagination when everything fits on a single page" in new Fixture {
      val rows = (1 to 5).toList
      val result = paginateList(rows, paginationIndex = Some(1), urlSelector = urlSelector)

      result.isRight mustBe true
      val (pageRows, paginator, infoText) = result.toOption.get

      pageRows mustBe rows
      paginator mustBe None
      infoText mustBe None
    }
  }

  "paginateIfValidPageIndex" - {

    "returns None when there are no rows at all" in new Fixture {
      val result = paginateIfValidPageIndex[Int](rowsOpt = None, paginationIndex = Some(1), urlSelector = urlSelector)
      result mustBe None
    }

    "returns empty page data when rows list is empty" in new Fixture {
      val result = paginateIfValidPageIndex[Int](rowsOpt = Some(Nil), paginationIndex = Some(1), urlSelector = urlSelector)

      result.isDefined mustBe true
      val inner = result.get
      inner.isRight mustBe true

      val (pageRows, paginator, infoText) = inner.toOption.get
      pageRows mustBe Nil
      paginator mustBe None
      infoText mustBe None
    }

    "returns paginated data when page index is valid" in new Fixture {
      val rows = (1 to 25).toList
      val result = paginateIfValidPageIndex(Some(rows), paginationIndex = Some(2), urlSelector = urlSelector)

      result.isDefined mustBe true
      val inner = result.get
      inner.isRight mustBe true

      val (pageRows, paginator, infoText) = inner.toOption.get
      pageRows mustBe (11 to 20).toList
      paginator.isDefined mustBe true
      infoText.isDefined mustBe true
    }

    "returns Left when page index is out of range" in new Fixture {
      val rows = (1 to 10).toList
      val result = paginateIfValidPageIndex(Some(rows), paginationIndex = Some(2), urlSelector = urlSelector)

      result.isDefined mustBe true
      val inner = result.get
      inner.isLeft mustBe true
    }
  }
}
