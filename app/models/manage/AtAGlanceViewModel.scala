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

package models.manage

import config.FrontendAppConfig
import controllers.manage.routes.{DueForDeletionReturnsController, InProgressReturnsController, SubmittedReturnsController}
import models.responses.*
import viewmodels.manage.{AgentDetailsViewModel, FeedbackViewModel, HelpAndContactViewModel, ReturnsManagementViewModel}

case class AtAGlanceViewModel(
                               storn: String,
                               returns: ReturnsManagementViewModel,
                               agentDetails: AgentDetailsViewModel,
                               helpAndContact: HelpAndContactViewModel,
                               feedback: FeedbackViewModel
                             )

object AtAGlanceViewModel {

  def apply(inProgressReturns: SdltInProgressReturnViewModel,
            submittedReturns: SdltSubmittedReturnViewModel,
            dueForDeletionReturnsTotal: Int,
            agentsCount: Int,
            storn: String)
           (implicit appConfig: FrontendAppConfig): AtAGlanceViewModel =
    AtAGlanceViewModel(
      storn = storn,
      returns =
        ReturnsManagementViewModel(
          inProgressReturnsCount = inProgressReturns.totalRowCount,
          inProgressReturnsUrl = InProgressReturnsController.onPageLoad(Some(1)).url,
          submittedReturnsCount = submittedReturns.totalRowCount,
          submittedReturnsUrl = SubmittedReturnsController.onPageLoad(Some(1)).url,
          dueForDeletionReturnsCount = dueForDeletionReturnsTotal,
          dueForDeletionUrl = DueForDeletionReturnsController.onPageLoad(Some(1), Some(1)).url,
          startReturnUrl = appConfig.startNewReturnUrl
        ),
      agentDetails =
        AgentDetailsViewModel(
          agentsCount = agentsCount,
          agentsUrl = appConfig.agentOverviewUrl,
          addAgentUrl = appConfig.startAddAgentUrl
        ),
      helpAndContact =
        HelpAndContactViewModel(
          helpUrl = "#",
          contactUrl = "#",
          howToPayUrl = appConfig.howToPayUrl,
          usefulLinksUrl = "#"
        ),
      feedback =
        FeedbackViewModel(
          feedbackUrl = appConfig.exitSurveyUrl
        )
    )
}