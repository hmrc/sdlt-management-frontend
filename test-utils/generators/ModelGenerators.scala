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

package generators

import models.*
import models.manage.ReturnSummary
import models.responses.{SdltInProgressReturnViewRow, UniversalStatus}
import viewmodels.manage.SdltSubmittedReturnsViewModel

import java.time.LocalDate

trait ModelGenerators {
  def generateReturnSummaries(
                               start: Int,
                               end: Int,
                               statusPattern: Int => String = index => if (index % 2 == 0) "STARTED" else "SUBMITTED"
                             ): List[ReturnSummary] = {
    (start to end).toList.map { index =>
      ReturnSummary(
        returnReference = s"ABC123-$index",
        utrn = Some(s"UTRN${1000 + index}"),
        status = statusPattern(index),
        dateSubmitted = Some(LocalDate.now().minusDays(index.toLong)),
        purchaserName = "Brown",
        address = s"$index Riverside Drive",
        agentReference = Some("AGTREF003")
      )
    }
  }

  def toInProgressViewRows(rs: ReturnSummary): SdltInProgressReturnViewRow =
    SdltInProgressReturnViewRow(
      address = rs.address,
      agentReference = rs.agentReference.getOrElse(""),
      purchaserName = rs.purchaserName,
      status = UniversalStatus.STARTED
    )

  def toSubmittedViewRows(rs: ReturnSummary): SdltSubmittedReturnsViewModel =
    SdltSubmittedReturnsViewModel(
      address = rs.address,
      utrn = rs.utrn.getOrElse(""),
      purchaserName = rs.purchaserName,
      status = UniversalStatus.SUBMITTED
    )
}
