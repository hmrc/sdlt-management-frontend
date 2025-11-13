package viewmodels.manage

import base.SpecBase
import org.scalatest.matchers.should.Matchers.*

class AgentDetailsViewModelSpec extends SpecBase {

  "Agent Details View Model" - {

    "should create a valid instance with all required fields" in {
      val viewModel = AgentDetailsViewModel(
        agents = 22,
        agentsUrl = "#",
        addAgentUrl = "#",
      )

      viewModel.agents shouldBe 22
      viewModel.agentsUrl shouldBe "#"
      viewModel.addAgentUrl shouldBe "#"
    }

    "should support case class copy" in {
      val original = AgentDetailsViewModel(
        agents = 22,
        agentsUrl = "#",
        addAgentUrl = "#",
      )

      val modified = original.copy(agents = 5)

      modified.agents shouldBe 5
      modified.agentsUrl shouldBe "#"
      modified.addAgentUrl shouldBe "#"
    }

    "should support equality comparison" in {
      val viewModelOne = AgentDetailsViewModel(
        agents = 22,
        agentsUrl = "#",
        addAgentUrl = "#",
      )

      val viewModelTwo = AgentDetailsViewModel(
        agents = 22,
        agentsUrl = "#",
        addAgentUrl = "#",
      )

      viewModelOne shouldBe viewModelTwo
    }

    "should handle different values correctly" in {
      val viewModelOne = AgentDetailsViewModel(
        agents = 22,
        agentsUrl = "#",
        addAgentUrl = "#",
      )

      val viewModelTwo = AgentDetailsViewModel(
        agents = 5,
        agentsUrl = "#",
        addAgentUrl = "#",
      )

      viewModelOne should not be viewModelTwo
    }
  }
}
