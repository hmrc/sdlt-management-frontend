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

import models.SdltReturnTypes.*
import models.manage.{ReturnSummary, SdltReturnRecordResponse}
import models.responses.{SdltInProgressDueForDeletionReturnViewModel, SdltInProgressReturnViewModel, SdltReturnViewRow, SdltReturnsViewModel, SdltSubmittedDueForDeletionReturnViewModel, SdltSubmittedReturnViewModel}
import models.responses.UniversalStatus.{ACCEPTED, STARTED, SUBMITTED, SUBMITTED_NO_RECEIPT}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.time.LocalDate

class SdltReturnViewModelSpec extends AnyWordSpec with Matchers {

  private def summary(
                       reference: String,
                       status: String,
                       purchaserName: String,
                       address: String,
                       utrn: String,
                       agentRef: Option[String] = Some("AgentRef")
                     ): ReturnSummary =
    ReturnSummary(
      returnReference = reference,
      utrn = Some(utrn),
      status = status,
      dateSubmitted = Some(LocalDate.parse("2025-10-25")),
      purchaserName = purchaserName,
      address = address,
      agentReference = agentRef
    )

  "SdltReturnViewRow.convertToViewRows" should {

    "convert a list of ReturnSummary into SdltReturnViewRow with default values applied" in {
      val input = List(
        summary(
          reference = "RET-001",
          status = "SUBMITTED",
          purchaserName = "Alice",
          address = "1 Example Street",
          utrn = "UTRN-001",
          agentRef = Some("Agent One")
        ),
        summary(
          reference = "RET-002",
          status = "STARTED",
          purchaserName = "Bob",
          address = "2 Example Street",
          utrn = "UTRN-002",
          agentRef = None
        )
      )

      val rows = SdltReturnViewRow.convertToViewRows(input)

      rows.size mustBe 2

      val first = rows.head
      first.purchaserName mustBe "Alice"
      first.address mustBe "1 Example Street"
      first.utrn mustBe "UTRN-001"
      first.agentReference mustBe "Agent One"
      first.status mustBe SUBMITTED

      val second = rows(1)
      second.purchaserName mustBe "Bob"
      second.address mustBe "2 Example Street"
      second.utrn mustBe "UTRN-002"
      second.agentReference mustBe ""
      second.status mustBe STARTED
    }

    "drop records with an unknown status" in {
      val valid = summary("RET-001", "SUBMITTED", "Alice", "1 Example Street", "UTRN-001")
      val bad = summary("RET-002", "NOT_A_STATUS", "Bob", "2 Example Street", "UTRN-002")
      val input = List(valid, bad)

      val rows = SdltReturnViewRow.convertToViewRows(input)

      rows.size mustBe 1
      rows.head.utrn mustBe "UTRN-001"
    }
  }

  "SdltReturnsViewModel.convertToViewModel for IN_PROGRESS_RETURNS" should {

    "keep only STARTED and ACCEPTED rows and preserve total count" in {
      val inProgressAccepted = summary(
        reference = "RET-001",
        status = "ACCEPTED",
        purchaserName = "Accepted Buyer",
        address = "1 Accepted Street",
        utrn = "UTRN-ACC-001"
      )

      val inProgressStarted = summary(
        reference = "RET-002",
        status = "STARTED",
        purchaserName = "Started Buyer",
        address = "2 Started Street",
        utrn = "UTRN-STA-001"
      )

      val submitted = summary(
        reference = "RET-003",
        status = "SUBMITTED",
        purchaserName = "Submitted Buyer",
        address = "3 Submitted Street",
        utrn = "UTRN-SUB-001"
      )

      val response = SdltReturnRecordResponse(
        returnSummaryCount = 3,
        returnSummaryList = List(inProgressAccepted, inProgressStarted, submitted)
      )

      val result = SdltReturnsViewModel
        .convertToViewModel(response, IN_PROGRESS_RETURNS)
        .asInstanceOf[SdltInProgressReturnViewModel]

      result.extractType mustBe IN_PROGRESS_RETURNS
      result.totalRowCount mustBe 3
      result.rows.map(_.status).toSet mustBe Set(ACCEPTED, STARTED)
      result.rows.exists(_.purchaserName == "Submitted Buyer") mustBe false
    }
  }

  "SdltReturnsViewModel.convertToViewModel for SUBMITTED_* returns" should {

    "keep SUBMITTED and SUBMITTED_NO_RECEIPT for SUBMITTED_SUBMITTED_RETURNS" in {
      val submitted = summary(
        reference = "RET-001",
        status = "SUBMITTED",
        purchaserName = "Submitted Buyer",
        address = "1 Submitted Street",
        utrn = "UTRN-SUB-001"
      )

      val submittedNoReceipt = summary(
        reference = "RET-002",
        status = "SUBMITTED_NO_RECEIPT",
        purchaserName = "No Receipt Buyer",
        address = "2 NoReceipt Street",
        utrn = "UTRN-SNR-001"
      )

      val started = summary(
        reference = "RET-003",
        status = "STARTED",
        purchaserName = "Started Buyer",
        address = "3 Started Street",
        utrn = "UTRN-STA-001"
      )

      val response = SdltReturnRecordResponse(
        returnSummaryCount = 3,
        returnSummaryList = List(submitted, submittedNoReceipt, started)
      )

      val result = SdltReturnsViewModel
        .convertToViewModel(response, SUBMITTED_SUBMITTED_RETURNS)
        .asInstanceOf[SdltSubmittedReturnViewModel]

      result.extractType mustBe SUBMITTED_SUBMITTED_RETURNS
      result.totalRowCount mustBe 3
      result.rows.map(_.status).toSet mustBe Set(SUBMITTED, SUBMITTED_NO_RECEIPT)
      result.rows.exists(_.status == STARTED) mustBe false
    }

    "behave the same way for SUBMITTED_NO_RECEIPT_RETURNS extract type" in {
      val submitted = summary(
        reference = "RET-001",
        status = "SUBMITTED",
        purchaserName = "Submitted Buyer",
        address = "1 Submitted Street",
        utrn = "UTRN-SUB-001"
      )

      val submittedNoReceipt = summary(
        reference = "RET-002",
        status = "SUBMITTED_NO_RECEIPT",
        purchaserName = "No Receipt Buyer",
        address = "2 NoReceipt Street",
        utrn = "UTRN-SNR-001"
      )

      val response = SdltReturnRecordResponse(
        returnSummaryCount = 2,
        returnSummaryList = List(submitted, submittedNoReceipt)
      )

      val result = SdltReturnsViewModel
        .convertToViewModel(response, SUBMITTED_NO_RECEIPT_RETURNS)
        .asInstanceOf[SdltSubmittedReturnViewModel]

      result.extractType mustBe SUBMITTED_NO_RECEIPT_RETURNS
      result.totalRowCount mustBe 2
      result.rows.map(_.status).toSet mustBe Set(SUBMITTED, SUBMITTED_NO_RECEIPT)
    }
  }

  "SdltReturnsViewModel.convertToViewModel for IN_PROGRESS_RETURNS_DUE_FOR_DELETION" should {

    "return all rows without filtering or sorting" in {
      val first = summary(
        reference = "RET-001",
        status = "STARTED",
        purchaserName = "B Buyer",
        address = "1 Street",
        utrn = "UTRN-001"
      )

      val second = summary(
        reference = "RET-002",
        status = "SUBMITTED",
        purchaserName = "A Buyer",
        address = "2 Street",
        utrn = "UTRN-002"
      )

      val response = SdltReturnRecordResponse(
        returnSummaryCount = 2,
        returnSummaryList = List(first, second)
      )

      val result = SdltReturnsViewModel
        .convertToViewModel(response, IN_PROGRESS_RETURNS_DUE_FOR_DELETION)
        .asInstanceOf[SdltInProgressDueForDeletionReturnViewModel]

      result.extractType mustBe IN_PROGRESS_RETURNS_DUE_FOR_DELETION
      result.totalRowCount mustBe 2
      result.rows.map(_.utrn) mustBe List("UTRN-001", "UTRN-002")
    }
  }

  "SdltReturnsViewModel.convertToViewModel for SUBMITTED_RETURNS_DUE_FOR_DELETION" should {

    "return all rows sorted by purchaser name" in {
      val first = summary(
        reference = "RET-001",
        status = "SUBMITTED",
        purchaserName = "Charlie",
        address = "1 Street",
        utrn = "UTRN-001"
      )

      val second = summary(
        reference = "RET-002",
        status = "SUBMITTED",
        purchaserName = "Alice",
        address = "2 Street",
        utrn = "UTRN-002"
      )

      val third = summary(
        reference = "RET-003",
        status = "SUBMITTED_NO_RECEIPT",
        purchaserName = "Bob",
        address = "3 Street",
        utrn = "UTRN-003"
      )

      val response = SdltReturnRecordResponse(
        returnSummaryCount = 3,
        returnSummaryList = List(first, second, third)
      )

      val result = SdltReturnsViewModel
        .convertToViewModel(response, SUBMITTED_RETURNS_DUE_FOR_DELETION)
        .asInstanceOf[SdltSubmittedDueForDeletionReturnViewModel]

      result.extractType mustBe SUBMITTED_RETURNS_DUE_FOR_DELETION
      result.totalRowCount mustBe 3

      val purchaserOrder = result.rows.map(_.purchaserName)
      purchaserOrder mustBe List("Alice", "Bob", "Charlie")
    }
  }
}
