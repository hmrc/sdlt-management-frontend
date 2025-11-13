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
