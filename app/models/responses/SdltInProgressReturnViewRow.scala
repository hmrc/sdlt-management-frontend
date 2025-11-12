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

package models.responses

import models.manage.SdltReturnRecordResponse
import models.responses.UniversalStatus.{ACCEPTED, PENDING, SUBMITTED}
import play.api.i18n.Messages
import play.api.libs.json.{Json, OFormat}

import java.time.LocalDate


case class SdltInProgressReturnViewRow(
                                   address: String,
                                   agentReference: String,
                                   dateSubmitted: LocalDate,
                                   utrn: String,
                                   purchaserName: String,
                                   status: UniversalStatus,
                                   returnReference: String
                               )

object SdltInProgressReturnViewRow {
  import UniversalStatus.*

  // TODO: add required tests
  private val inProgressReturnStatuses : Seq[UniversalStatus] = Seq(STARTED, PENDING)

  def convertResponseToViewRows(response: SdltReturnRecordResponse): List[SdltInProgressReturnViewRow] = {
    response.returnSummaryList.flatMap {
      rec =>
        fromString(rec.status) // Ignore rows which we fail to convert :: should we fail execution???
          .filter(inProgressReturnStatuses.contains(_))
          .map { status =>
            SdltInProgressReturnViewRow(
              address = rec.address,
              agentReference = rec.agentReference,
              dateSubmitted = rec.dateSubmitted,
              utrn = rec.utrn,
              purchaserName = rec.purchaserName,
              status = status,
              returnReference = rec.returnReference
            )
          }
    }
  }
}