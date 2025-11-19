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

import models.manage.SdltReturnRecordResponseLegacy
import models.responses.UniversalStatus.{ACCEPTED, PENDING, SUBMITTED}
import play.api.i18n.Messages
import play.api.libs.json.{Json, OFormat}

import java.time.LocalDate

case class SdltInProgressReturnViewRow(
                                        address: String,
                                        agentReference: String,
                                        purchaserName: String,
                                        status: UniversalStatus
                                      )

object SdltInProgressReturnViewRow {

  import UniversalStatus.*

  private val inProgressReturnStatuses: Seq[UniversalStatus] = Seq(STARTED, ACCEPTED)

  def convertResponseToViewRows(response: SdltReturnRecordResponseLegacy): List[SdltInProgressReturnViewRow] = {

    for {
      rec <- response.returnSummaryList
      status <- fromString(rec.status)
      if inProgressReturnStatuses.contains(status)
    } yield SdltInProgressReturnViewRow(
      address = rec.address,
      agentReference = rec.agentReference,
      purchaserName = rec.purchaserName,
      status = status,
    )
  }

}