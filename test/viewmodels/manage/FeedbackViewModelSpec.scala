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

class FeedbackViewModelSpec extends SpecBase {

  "Feedback View Model" - {

    "should create a valid instance with all required fields" in {
      val viewModel = FeedbackViewModel(
        feedbackUrl = "#",
      )

      viewModel.feedbackUrl shouldBe "#"
    }

    "should support case class copy" in {
      val original = FeedbackViewModel(
        feedbackUrl = "#",
      )

      val modified = original.copy(feedbackUrl = "/feedback")

      modified.feedbackUrl shouldBe "/feedback"
    }

    "should support equality comparison" in {
      val viewModelOne = FeedbackViewModel(
        feedbackUrl = "#",
      )

      val viewModelTwo = FeedbackViewModel(
        feedbackUrl = "#",
      )

      viewModelOne shouldBe viewModelTwo
    }

    "should handle different values correctly" in {
      val viewModelOne = FeedbackViewModel(
        feedbackUrl = "#",
      )

      val viewModelTwo = FeedbackViewModel(
        feedbackUrl = "/feedback",
      )

      viewModelOne should not be viewModelTwo
    }
  }
}
