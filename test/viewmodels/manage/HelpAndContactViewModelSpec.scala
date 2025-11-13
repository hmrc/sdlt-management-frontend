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

package viewmodels.manage

import base.SpecBase
import org.scalatest.matchers.should.Matchers.*

class HelpAndContactViewModelSpec extends SpecBase {

  "Help And Contact View Model" - {

    "should create a valid instance with all required fields" in {
      val viewModel = HelpAndContactViewModel(
        helpUrl = "#",
        contactUrl = "#",
        howToPayUrl = "#",
        usefulLinksUrl = "#"
      )

      viewModel.helpUrl shouldBe "#"
      viewModel.contactUrl shouldBe "#"
      viewModel.howToPayUrl shouldBe "#"
      viewModel.usefulLinksUrl shouldBe "#"
    }

    "should support case class copy" in {
      val original = HelpAndContactViewModel(
        helpUrl = "#",
        contactUrl = "#",
        howToPayUrl = "#",
        usefulLinksUrl = "#"
      )

      val modified = original.copy(helpUrl = "/help")

      modified.helpUrl shouldBe "/help"
      modified.contactUrl shouldBe "#"
      modified.howToPayUrl shouldBe "#"
      modified.usefulLinksUrl shouldBe "#"
    }

    "should support equality comparison" in {
      val viewModelOne = HelpAndContactViewModel(
        helpUrl = "#",
        contactUrl = "#",
        howToPayUrl = "#",
        usefulLinksUrl = "#"
      )

      val viewModelTwo = HelpAndContactViewModel(
        helpUrl = "#",
        contactUrl = "#",
        howToPayUrl = "#",
        usefulLinksUrl = "#"
      )

      viewModelOne shouldBe viewModelTwo
    }

    "should handle different values correctly" in {
      val viewModelOne = HelpAndContactViewModel(
        helpUrl = "#",
        contactUrl = "#",
        howToPayUrl = "#",
        usefulLinksUrl = "#"
      )

      val viewModelTwo = HelpAndContactViewModel(
        helpUrl = "/help",
        contactUrl = "#",
        howToPayUrl = "#",
        usefulLinksUrl = "#"
      )

      viewModelOne should not be viewModelTwo
    }
  }
}
