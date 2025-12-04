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

package viewmodels.govuk

import forms.mappings.Mappings
import models.manage.{ReturnSummary, SdltReturnRecordResponse}
import models.responses.SdltInProgressReturnViewRow
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import models.responses.SdltInProgressReturnViewRow.*
import models.responses.UniversalStatus.{ACCEPTED, PENDING, STARTED, SUBMITTED, VALIDATED}

import java.time.LocalDate

class SdltInProgressReturnViewRowSpec extends AnyFreeSpec with Matchers with Mappings with OptionValues {

  val responseWithEmptySummary: SdltReturnRecordResponse = SdltReturnRecordResponse(
    returnSummaryCount = Some(0),
    returnSummaryList = List.empty
  )

  val responseWithData: SdltReturnRecordResponse = SdltReturnRecordResponse(
    returnSummaryCount = Some(0),
    returnSummaryList = List(
      ReturnSummary(
        returnReference = "REF001",
        utrn = Some("UTRN001"),
        status = "PENDING",
        dateSubmitted = Some(LocalDate.parse("2025-01-02")),
        purchaserName = "Name001",
        address = "Address001",
        agentReference = None
      ),
      ReturnSummary(
        returnReference = "REF002",
        utrn = Some("UTRN002"),
        status = "VALIDATED",
        dateSubmitted = Some(LocalDate.parse("2025-01-02")),
        purchaserName = "Name002",
        address = "Address002",
        agentReference = Some("AgentRef002")
      ),
      ReturnSummary(
        returnReference = "REF003",
        utrn = Some("UTRN003"),
        status = "STARTED",
        dateSubmitted = Some(LocalDate.parse("2025-01-02")),
        purchaserName = "Name003",
        address = "Address003",
        agentReference = None
      ),
      ReturnSummary(
        returnReference = "REF004",
        utrn = Some("UTRN004"),
        status = "SUBMITTED",
        dateSubmitted = Some(LocalDate.parse("2025-01-02")),
        purchaserName = "Name004",
        address = "Address004",
        agentReference = Some("AgentRef004")
      ),
      ReturnSummary(
        returnReference = "REF005",
        utrn = Some("UTRN005"),
        status = "ACCEPTED",
        dateSubmitted = Some(LocalDate.parse("2025-01-02")),
        purchaserName = "Name005",
        address = "Address005",
        agentReference = Some("AgentRef005")
      )
    )

  )

  val expectedDataRows: List[SdltInProgressReturnViewRow] = List(
    SdltInProgressReturnViewRow("Address003", "", "Name003", STARTED),
    SdltInProgressReturnViewRow("Address005", "AgentRef005", "Name005", ACCEPTED)
  )

  "Response model conversion" - {
    "empty response return empty list" in {
      val result: List[SdltInProgressReturnViewRow] = convertToReturnViewRows(responseWithEmptySummary.returnSummaryList)
      result mustBe empty
    }

    "response with some data return expected data rows" in {
      val result: List[SdltInProgressReturnViewRow] = convertToReturnViewRows(responseWithData.returnSummaryList)
      result mustBe expectedDataRows
    }

  }

}