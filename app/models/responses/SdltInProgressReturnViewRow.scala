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

import models.manage.ReturnSummary
import play.api.{Logger, Logging}

case class SdltInProgressReturnViewRow(
                                        address: String,
                                        agentReference: String,
                                        purchaserName: String,
                                        status: UniversalStatus
                                      )

object SdltInProgressReturnViewRow extends Logging {

  import UniversalStatus.*

  private val inProgressReturnStatuses: Seq[UniversalStatus] = Seq(STARTED, ACCEPTED)

  def convertResponseToReturnViewRows(inProgressReturnsList: List[ReturnSummary]): List[SdltInProgressReturnViewRow] = {
    val res = for {
      rec <- inProgressReturnsList
      st = fromString(rec.status)
      arn <- rec.agentReference
    } yield {
      st match {
        case Right(status) =>
          Some(
            SdltInProgressReturnViewRow(
              address = rec.address,
              agentReference = arn,
              purchaserName = rec.purchaserName,
              status = status,
            )
          )
        case Left(ex) =>
          logger.error(s"[SdltInProgressReturnViewRow][convertResponseToViewRows] - conversion from: ${rec} failure: $ex")
          None
      }
    }
    res
      .flatten
      .filter(rec => inProgressReturnStatuses.contains(rec.status))
  }

}