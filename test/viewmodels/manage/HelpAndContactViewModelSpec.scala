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
