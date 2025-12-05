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
import play.api.Logging

case class SdltSubmittedReturnViewModel(rows: List[SdltSubmittedReturnsViewRow], totalRowCount: Option[Int])

case class SdltSubmittedReturnsViewRow(
                                        address: String,
                                        utrn: String,
                                        purchaserName: String,
                                        status: UniversalStatus
                                      )

object SdltSubmittedReturnsViewRow extends Logging {

  import UniversalStatus.*

  private val submittedReturnStatuses: Seq[UniversalStatus] = Seq(SUBMITTED, SUBMITTED_NO_RECEIPT)

  def convertResponseToSubmittedView(submittedReturnsList: List[ReturnSummary]): List[SdltSubmittedReturnsViewRow] = {
    val res = for {
      rec <- submittedReturnsList
    } yield fromString(rec.status) match {
      case Right(status) =>
        Some(
          SdltSubmittedReturnsViewRow(
            address = rec.address,
            utrn = rec.utrn.getOrElse(""),  // TODO: should be mandatory and error-handled correctly
            purchaserName = rec.purchaserName,
            status = status
          )
        )
      case Left(ex) =>
        logger.error(s"[SdltSubmittedReturnsViewModel][convertResponseToSubmittedView] - conversion from: ${rec} failure: $ex")
        None
    }
    res
      .flatten
      .filter(rec => submittedReturnStatuses.contains(rec.status))
  }
}
