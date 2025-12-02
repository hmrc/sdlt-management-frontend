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

import models.manage.ReturnSummary
import models.responses.UniversalStatus
import play.api.Logging

case class SdltSubmittedReturnsViewModel(
                                          address: String,
                                          utrn: String,
                                          purchaserName: String,
                                          status: UniversalStatus
                                        )

object SdltSubmittedReturnsViewModel extends Logging {

  import UniversalStatus.*

  private val acceptableStatus: Seq[UniversalStatus] = Seq(SUBMITTED, SUBMITTED_NO_RECEIPT)

  def convertResponseToSubmittedView(submittedReturns: List[ReturnSummary]): List[SdltSubmittedReturnsViewModel] = {
    val res = for {
      rec <- submittedReturns
      st = fromString(rec.status)
      utrn <- rec.utrn
    } yield st match {
      case Right(status) =>
        Some(
          SdltSubmittedReturnsViewModel(
            address = rec.address,
            utrn = utrn,
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
      .filter(rec => acceptableStatus.contains(rec.status))
  }
}
