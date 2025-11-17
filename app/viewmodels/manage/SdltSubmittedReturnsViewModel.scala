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
import models.responses.UniversalStatus

import java.time.LocalDate


case class SdltSubmittedReturnsViewModel(
                                        returnReference: String,
                                        utrn: String,
                                        status: UniversalStatus,
                                        dateSubmitted: LocalDate,
                                        purchaserName: String,
                                        address: String,
                                        agentReference: String,
                                      )

object SdltSubmittedReturnsViewModel {
  import UniversalStatus.*

  private val acceptableStatus : Seq[UniversalStatus] = Seq(SUBMITTED, SUBMITTED_NO_RECEIPT)

  def convertResponseToSubmittedView(response: SdltReturnRecordResponse): List[SdltSubmittedReturnsViewModel] = {
    response.returnSummaryList.flatMap {
      rec =>
        fromString(rec.status)
          .filter(acceptableStatus.contains(_))
          .map { status =>
            SdltSubmittedReturnsViewModel(
              returnReference = rec.returnReference,
              utrn = rec.utrn,
              status = status,
              dateSubmitted = rec.dateSubmitted,
              purchaserName = rec.purchaserName,
              address = rec.address,
              agentReference = rec.agentReference
            )
          }
    }
  }
}