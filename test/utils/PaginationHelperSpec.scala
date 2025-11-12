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

class PaginationHelperSpec extends AnyFreeSpec with Matchers with SpecBase{

  trait Fixture extends PaginationHelper {
     val urlSelector = (pageIndex: Int) => controllers.manage.routes.InProgressReturnsController.onPageLoad(Some(pageIndex)).url

    val application = applicationBuilder(userAnswers = None).build()
  }

  "PageItems" - {

    "Get unique pagination items" in new Fixture {
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
      generatePreviousLink(paginationIndex = 5,
        numberOfPages = pageCount,
        urlPrev = "prevLink")( messages(application) )
        .toList.nonEmpty mustBe true

      // should not render prev link
      generatePreviousLink(paginationIndex = 1,
        numberOfPages = pageCount,
        urlPrev = "prevLink")(messages(application))
        .toList.isEmpty mustBe true
    }


  }

}