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
import models.manage.SdltReturnRecordResponse
import models.responses.{SdltInProgressReturnViewRow, UniversalStatus}

import java.time.LocalDate


case class SdltDueDeletionReturnsViewModel(
                                          inProgressReturns: List[SdltInProgressReturnViewRow],
                                          submittedReturns: List[SdltSubmittedReturnsViewModel]
                                        )

object SdltDueDeletionReturnsViewModel {
  import UniversalStatus.*

  private val acceptableInProgressStatus : Seq[UniversalStatus] = Seq(ACCEPTED, PENDING)
  private val acceptableSubmittedStatus : Seq[UniversalStatus] = Seq(SUBMITTED, SUBMITTED_NO_RECEIPT)

  def convertResponseToDueDeletionView(response: SdltReturnRecordResponse): List[SdltDueDeletionReturnsViewModel] = {
    response.returnSummaryList.flatMap {
      case inProgress: SdltInProgressReturnViewRow =>
        fromString(inProgress.status)
          .filter(acceptableInProgressStatus.contains(_))
          .map { status =>
            SdltInProgressReturnViewRow(
              address = inProgress.address,
              agentReference = inProgress.agentReference,
              dateSubmitted = inProgress.dateSubmitted,
              utrn = inProgress.utrn,
              purchaserName = inProgress.purchaserName,
              status = status,
              returnReference = inProgress.returnReference
            )
          }
      case submitted: SdltSubmittedReturnsViewModel =>
        fromString(submitted.status)
          .filter(acceptableSubmittedStatus.contains(_))
          .map { status =>
            SdltSubmittedReturnsViewModel(
              address = submitted.address,
              agentReference = submitted.agentReference,
              dateSubmitted = submitted.dateSubmitted,
              utrn = submitted.utrn,
              purchaserName = submitted.purchaserName,
              status = status,
              returnReference = submitted.returnReference
            )
          }
      case _ => None
    }
  }
}