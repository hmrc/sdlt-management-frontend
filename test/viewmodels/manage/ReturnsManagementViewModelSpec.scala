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

class ReturnsManagementViewModelSpec extends SpecBase {

  "Returns Management View Model" - {

    "should create a valid instance with all required fields" in {
      val viewModel = ReturnsManagementViewModel(
        inProgress = 7,
        inProgressUrl = "#",
        submitted = 9,
        submittedUrl = "#",
        dueForDeletion = 0,
        dueForDeletionUrl = "#",
        startReturnUrl = "#"
      )

      viewModel.inProgress shouldBe 7
      viewModel.inProgressUrl shouldBe "#"
      viewModel.submitted shouldBe 9
      viewModel.submittedUrl shouldBe "#"
      viewModel.dueForDeletion shouldBe 0
      viewModel.dueForDeletionUrl shouldBe "#"
      viewModel.startReturnUrl shouldBe "#"
    }

    "should support case class copy" in {
      val original = ReturnsManagementViewModel(
        inProgress = 7,
        inProgressUrl = "#",
        submitted = 9,
        submittedUrl = "#",
        dueForDeletion = 0,
        dueForDeletionUrl = "#",
        startReturnUrl = "#"
      )

      val modified = original.copy(inProgress = 5, dueForDeletion = 2)

      modified.inProgress shouldBe 5
      modified.inProgressUrl shouldBe "#"
      modified.submitted shouldBe 9
      modified.submittedUrl shouldBe "#"
      modified.dueForDeletion shouldBe 2
      modified.dueForDeletionUrl shouldBe "#"
      modified.startReturnUrl shouldBe "#"
    }

    "should support equality comparison" in {
      val viewModelOne = ReturnsManagementViewModel(
        inProgress = 7,
        inProgressUrl = "#",
        submitted = 9,
        submittedUrl = "#",
        dueForDeletion = 0,
        dueForDeletionUrl = "#",
        startReturnUrl = "#"
      )

      val viewModelTwo = ReturnsManagementViewModel(
        inProgress = 7,
        inProgressUrl = "#",
        submitted = 9,
        submittedUrl = "#",
        dueForDeletion = 0,
        dueForDeletionUrl = "#",
        startReturnUrl = "#"
      )

      viewModelOne shouldBe viewModelTwo
    }

    "should handle different values correctly" in {
      val viewModelOne = ReturnsManagementViewModel(
        inProgress = 7,
        inProgressUrl = "#",
        submitted = 9,
        submittedUrl = "#",
        dueForDeletion = 0,
        dueForDeletionUrl = "#",
        startReturnUrl = "#"
      )

      val viewModelTwo = ReturnsManagementViewModel(
        inProgress = 4,
        inProgressUrl = "#",
        submitted = 0,
        submittedUrl = "#",
        dueForDeletion = 2,
        dueForDeletionUrl = "#",
        startReturnUrl = "#"
      )

      viewModelOne should not be viewModelTwo
    }
  }
}
