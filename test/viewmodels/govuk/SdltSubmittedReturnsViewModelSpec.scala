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
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import models.responses.UniversalStatus.{SUBMITTED, SUBMITTED_NO_RECEIPT}
import viewmodels.manage.SdltSubmittedReturnsViewModel
import viewmodels.manage.SdltSubmittedReturnsViewModel.convertResponseToSubmittedView

import java.time.LocalDate

class SdltSubmittedReturnsViewModelSpec extends AnyFreeSpec with Matchers with Mappings with OptionValues {

  val emptyBEResponse: SdltReturnRecordResponse = SdltReturnRecordResponse(
    returnSummaryCount = Some(0),
    returnSummaryList = List.empty
  )

  val populatedBEResponse: SdltReturnRecordResponse = SdltReturnRecordResponse(
    returnSummaryCount = Some(0),
    returnSummaryList = List(
      ReturnSummary(
        returnReference = "returnReference1",
        utrn = Some("UTRN1"),
        status = "SUBMITTED",
        dateSubmitted = Some(LocalDate.parse("2025-01-02")),
        purchaserName = "purchaserName1",
        address = "propertyAddress1",
        agentReference = Some("agentReference1")
      ),
      ReturnSummary(
        returnReference = "returnReference2",
        utrn = Some("UTRN2"),
        status = "SUBMITTED_NO_RECEIPT",
        dateSubmitted = Some(LocalDate.parse("2025-01-02")),
        purchaserName = "purchaserName2",
        address = "propertyAddress2",
        agentReference = Some("agentReference2")
      )
    )
  )

  val expectedDataRows: List[SdltSubmittedReturnsViewModel] = List(
    SdltSubmittedReturnsViewModel(
      address = "propertyAddress1",
      utrn = "UTRN1",
      purchaserName = "purchaserName1",
      status = SUBMITTED
    ),
    SdltSubmittedReturnsViewModel(
      address = "propertyAddress2",
      utrn = "UTRN2",
      purchaserName = "purchaserName2",
      status = SUBMITTED_NO_RECEIPT
    )
  )

  "Convert the be data model to the view model" - {
    "when there is data received from the be, it will be converted to the view model" in {

      val result = convertResponseToSubmittedView(populatedBEResponse.returnSummaryList)
      result mustBe expectedDataRows
    }

    "when there is no data received from the be, the view model should be empty" in {

      val result = convertResponseToSubmittedView(emptyBEResponse.returnSummaryList)
      result mustBe empty
    }
  }
}
