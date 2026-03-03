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
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.Application
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages

class PaginationHelperSpec extends AnyFreeSpec with Matchers with SpecBase{

  trait Fixture extends PaginationHelper {
    val urlSelector: Int => String = (pageIndex: Int) => controllers.manage.routes.InProgressReturnsController.onPageLoad(Some(pageIndex)).url
    val application: Application = applicationBuilder(userAnswers = None).build()
    val paginationIndexSelectedMidPage: Int = 5
    val paginationIndexSelectedFirstPage: Int = 1
  }

  "PageItems" - {

    "Get prev pagination link" in new Fixture {
      val pageCount: Int = 10

      // render prev link
      generatePreviousLink(paginationIndex = paginationIndexSelectedMidPage,
        numberOfPages = pageCount,
        urlPrev = "prevLink")( messages(application) )
        .toList.nonEmpty mustBe true

      // should not render prev link
      generatePreviousLink(paginationIndex = paginationIndexSelectedFirstPage,
        numberOfPages = pageCount,
        urlPrev = "prevLink")(messages(application))
        .toList.isEmpty mustBe true
    }

    "Get next pagination link" in new Fixture {
      val pageCount: Int = 10

      generateNextLink(paginationIndex = pageCount,
        numberOfPages = pageCount,
        urlNext = "prevLink")(messages(application))
        .toList.isEmpty mustBe true

      generateNextLink(paginationIndex = pageCount,
        numberOfPages = paginationIndexSelectedFirstPage,
        urlNext = "prevLink")(messages(application))
        .toList.nonEmpty mustBe true
    }

    "Calc number of pages::pagination:: all cases" in new PaginationHelper {
      getPageCount(0) mustBe 1
      getPageCount(-1) mustBe 1
      getPageCount(117) mustBe 12
      getPageCount(121) mustBe 13
      getPageCount(10) mustBe 1
      getPageCount(11) mustBe 2
    }
  }

  "validatePageIndex" - {
    "return Left when page index is below default" in new PaginationHelper {
      val result = validatePageIndex(
        userInputPageInput = Some(0),
        rowsCount = 10
      )

      result match {
        case Left(e) =>
          e.getMessage mustBe "PageIndex selected is out of scope"
        case Right(_) =>
          fail("Expected Left(Error)")
      }
    }
  }

  "paginationItems" - {
    "return pages 1 and 2 when current number starts at 3" in new Fixture {
      val result = paginationItems(
        currentPage = 4,
        totalPages = 10,
        urlSelector,
        visibleBefore = 1,
        visibleAfter = 10
      )

      result.take(2).map(_.number) mustBe Seq(Some("1"), Some("2"))
    }

    "return page 1 and ellipsis when number starts after 3" in new Fixture {
      val result = paginationItems(
        currentPage = 6,
        totalPages = 2,
        urlSelector,
        visibleBefore = 2,
        visibleAfter = 20
      )

      result.head.number mustBe Some("1")
      result(1).ellipsis mustBe Some(true)
    }

    "return page 1 when number is less than 1" in new Fixture {
      val result = paginationItems(
        currentPage = 3,
        totalPages = 1,
        urlSelector,
        visibleBefore = 1,
        visibleAfter = 10
      )

      result.head.number mustBe Some("1")
      result.exists(_.ellipsis.contains(true)) mustBe false
      result.take(2).map(_.number) must not contain Some("2")
    }

    "return last two pages when number ends two pages before totalPages" in new Fixture {
      val result = paginationItems(
        currentPage = 7,
        totalPages = 10,
        urlSelector,
        visibleBefore = 1,
        visibleAfter = 1
      )

      val lastTwo = result.takeRight(2).map(_.number)
      lastTwo mustBe Seq(Some("9"), Some("10"))
    }
    "return ellipsis and last pagr when number ends before the final page range" in new Fixture {
      val result = paginationItems(
        currentPage = 5,
        totalPages = 10,
        urlSelector,
        visibleBefore = 1,
        visibleAfter = 1
      )

      val lastTwo = result.takeRight(2)
      lastTwo.head.ellipsis mustBe Some(true)
      lastTwo(1).number mustBe Some("10")
    }
    "should create a pagination item with ellipsis when label is '...'" in new Fixture {
      val result = paginationItems(
        currentPage = 5,
        totalPages = 10,
        urlSelector,
        visibleBefore = 1,
        visibleAfter = 1
      )

      val ellipsisItem = result.find(_.ellipsis.contains(true))

      ellipsisItem mustBe defined
      ellipsisItem.get.href mustBe "#"
      ellipsisItem.get.number mustBe None
    }
  }
  "getPaginationWithInfoText" - {
    "return None when page index is out of scope" in new Fixture {
      implicit val messages: Messages = stubMessages()

      val result =
        getPaginationWithInfoText(
          rows = List(1, 2, 3),
          totalRowCount = 3,
          paginationIndex = Some(100),
          urlSelector
        )(messages)

      result mustBe None
    }
  }



}