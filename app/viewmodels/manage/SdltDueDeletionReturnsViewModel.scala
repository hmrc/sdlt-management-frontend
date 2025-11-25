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
import models.manage.{ReturnSummary, SdltReturnRecordResponse}
import models.responses.{PaginatedInProgressReturnsViewModel, SdltInProgressReturnViewRow, UniversalStatus}

import java.time.LocalDate


case class SdltDueDeletionReturnsViewModel(
                                          inProgressReturns: Option[PaginatedInProgressReturnsViewModel],
                                          submittedReturns: Option[PaginatedSubmittedReturnsViewModel]
                                        )

object SdltDueDeletionReturnsViewModel {
  import UniversalStatus.*

  private val acceptableInProgressStatus : Seq[UniversalStatus] = Seq(ACCEPTED, PENDING)
  private val acceptableSubmittedStatus : Seq[UniversalStatus] = Seq(SUBMITTED, SUBMITTED_NO_RECEIPT)

//  def convertResponseToDueForDeletionView(returns: List[ReturnSummary]): SdltDueDeletionReturnsViewModel =
//
//    val inProgress =
//      for {
//      rec <- returns
//      status <- fromString(rec.status)
//      utrn <- rec.utrn
//      if acceptableInProgressStatus.contains(status)
//    } yield
//      SdltInProgressReturnViewRow(
//        address = rec.address,
//        utrn = utrn,
//        purchaserName = rec.purchaserName,
//        status = status
//      )
//
//    val submitted =
//      for {
//      rec <- returns
//      status <- fromString(rec.status)
//      utrn <- rec.utrn
//      if acceptableSubmittedStatus.contains(status)
//    } yield 
//      SdltSubmittedReturnsViewModel(
//        address = rec.address,
//        utrn = utrn,
//        purchaserName = rec.purchaserName,
//        status = status
//      )
//    SdltDueDeletionReturnsViewModel(Some(inProgress), Some(submitted))
//    
}