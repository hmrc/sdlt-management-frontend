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
