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

import config.FrontendAppConfig
import forms.mappings.Mappings
import models.SdltReturnTypes.IN_PROGRESS_RETURNS
import models.manage.{ReturnSummary, SdltReturnRecordResponse}
import models.responses.{SdltInProgressReturnViewModel, SdltReturnViewRow}
import models.responses.SdltReturnViewRow.convertToViewRows
import models.responses.SdltReturnsViewModel.*
import models.responses.UniversalStatus.{ACCEPTED, DEPARTMENTAL_ERROR, FATAL_ERROR, IN_PROGRESS, STARTED}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar.mock

import java.time.LocalDate

class SdltInProgressReturnViewRowSpec extends AnyFreeSpec with Matchers with Mappings with OptionValues {

  private val appConfig = mock[FrontendAppConfig]

  val responseWithEmptySummary: SdltReturnRecordResponse = SdltReturnRecordResponse(
    returnSummaryCount = 0,
    returnSummaryList  = List.empty
  )

  val responseWithData: SdltReturnRecordResponse = SdltReturnRecordResponse(
    returnSummaryCount = 0,
    returnSummaryList = List(
      // No status at all -> the TRUE in-progress return (kept, shown as IN_PROGRESS)
      ReturnSummary(
        returnReference = "000",
        utrn            = None,
        status          = None,
        dateSubmitted   = None,
        purchaserName   = "Name000",
        address         = "Address000",
        agentReference  = Some("AgentRef000")
      ),
      // PENDING / VALIDATED are not in the in-progress set -> excluded
      ReturnSummary(
        returnReference = "001",
        utrn            = Some("UTRN001"),
        status          = Some("PENDING"),
        dateSubmitted   = Some(LocalDate.parse("2025-01-02")),
        purchaserName   = "Name001",
        address         = "Address001",
        agentReference  = None
      ),
      ReturnSummary(
        returnReference = "002",
        utrn            = Some("UTRN002"),
        status          = Some("VALIDATED"),
        dateSubmitted   = Some(LocalDate.parse("2025-01-02")),
        purchaserName   = "Name002",
        address         = "Address002",
        agentReference  = Some("AgentRef002")
      ),
      // STARTED -> kept
      ReturnSummary(
        returnReference = "003",
        utrn            = Some("UTRN003"),
        status          = Some("STARTED"),
        dateSubmitted   = Some(LocalDate.parse("2025-01-02")),
        purchaserName   = "Name003",
        address         = "Address003",
        agentReference  = None
      ),
      // SUBMITTED -> excluded from in-progress
      ReturnSummary(
        returnReference = "004",
        utrn            = Some("UTRN004"),
        status          = Some("SUBMITTED"),
        dateSubmitted   = Some(LocalDate.parse("2025-01-02")),
        purchaserName   = "Name004",
        address         = "Address004",
        agentReference  = Some("AgentRef004")
      ),
      // ACCEPTED -> kept
      ReturnSummary(
        returnReference = "005",
        utrn            = Some("UTRN005"),
        status          = Some("ACCEPTED"),
        dateSubmitted   = Some(LocalDate.parse("2025-01-02")),
        purchaserName   = "Name005",
        address         = "Address005",
        agentReference  = Some("AgentRef005")
      ),
      // DEPARTMENTAL_ERROR -> kept (errored, shown so the filer can see it failed)
      ReturnSummary(
        returnReference = "006",
        utrn            = Some("UTRN006"),
        status          = Some("DEPARTMENTAL_ERROR"),
        dateSubmitted   = Some(LocalDate.parse("2025-01-02")),
        purchaserName   = "Name006",
        address         = "Address006",
        agentReference  = Some("AgentRef006")
      ),
      // FATAL_ERROR -> kept
      ReturnSummary(
        returnReference = "007",
        utrn            = Some("UTRN007"),
        status          = Some("FATAL_ERROR"),
        dateSubmitted   = Some(LocalDate.parse("2025-01-02")),
        purchaserName   = "Name007",
        address         = "Address007",
        agentReference  = Some("AgentRef007")
      )
    )
  )

  // Only the in-progress statuses survive the filter: IN_PROGRESS (no status), STARTED,
  // ACCEPTED, DEPARTMENTAL_ERROR, FATAL_ERROR. PENDING / VALIDATED / SUBMITTED are dropped.
  val expectedDataRows: List[SdltReturnViewRow] = List(
    SdltReturnViewRow("Address000", "AgentRef000", "Name000", IN_PROGRESS,        utrn = "",        redirectUrl = "redirectUrl"),
    SdltReturnViewRow("Address003", "",            "Name003", STARTED,            utrn = "UTRN003", redirectUrl = "redirectUrl"),
    SdltReturnViewRow("Address005", "AgentRef005", "Name005", ACCEPTED,           utrn = "UTRN005", redirectUrl = "redirectUrl"),
    SdltReturnViewRow("Address006", "AgentRef006", "Name006", DEPARTMENTAL_ERROR, utrn = "UTRN006", redirectUrl = "redirectUrl"),
    SdltReturnViewRow("Address007", "AgentRef007", "Name007", FATAL_ERROR,        utrn = "UTRN007", redirectUrl = "redirectUrl")
  )

  "Response model conversion" - {

    "empty response returns empty list" in {
      when(appConfig.returnTaskListUrl(any[String])).thenReturn("redirectUrl")

      val result: List[SdltReturnViewRow] = convertToViewRows(responseWithEmptySummary.returnSummaryList, appConfig)

      result mustBe empty
    }

    "response with data returns only the in-progress rows (incl. no-status and errored)" in {
      when(appConfig.returnTaskListUrl(any[String])).thenReturn("redirectUrl")

      val resultViewModel = convertToViewModel(responseWithData, IN_PROGRESS_RETURNS, 1, appConfig)
        .asInstanceOf[SdltInProgressReturnViewModel]

      resultViewModel.rows must contain theSameElementsAs expectedDataRows
      resultViewModel.totalRowCount mustBe 0
    }
  }
}