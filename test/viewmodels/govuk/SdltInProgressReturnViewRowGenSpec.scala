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

import models.manage.{ReturnSummary, SdltReturnRecordResponse}
import models.responses.UniversalStatus.{ACCEPTED, STARTED}
import org.scalacheck.*

import java.time.LocalDate

object SdltInProgressReturnViewRowGenSpec extends Properties("SdltInProgressReturnViewRow") {

  import models.responses.SdltInProgressReturnViewRow._
  import Prop.forAll

  val maxNumberOfRecsInGeneratedSet : Int = 100

  val returnSummaryGen: Gen[ReturnSummary] = for {
    ref <- Gen.alphaStr
    utr <- Gen.alphaNumStr
    status <- Gen.oneOf("STARTED", "VALIDATED", "ACCEPTED",
      "PENDING", "SUBMITTED", "SUBMITTED_NO_RECEIPT", "DEPARTMENTAL_ERROR", "FATAL_ERROR")
  } yield
    ReturnSummary(
      returnReference = s"REF${ref.take(5)}",
      utrn = Some(s"UTR${utr.take(5)}"),
      status = status,
      dateSubmitted = Some(LocalDate.parse("2025-01-02")),
      purchaserName = "Name002",
      address = "Address002",
      agentReference = Some("AgentRef002")
    )

  val syntheticResponse: Gen[List[ReturnSummary]] = Gen.listOfN(maxNumberOfRecsInGeneratedSet, returnSummaryGen)

  val convertToResponse: List[ReturnSummary] => SdltReturnRecordResponse = (list: List[ReturnSummary]) => {
    SdltReturnRecordResponse(
      returnSummaryCount = Some(list.length),
      returnSummaryList = list
    )
  }

  // Verify that we can only get recs with these 2 statuses
  property("convertReturnsResponseToViewRows") = forAll(syntheticResponse) { returnSummary =>
    val response: SdltReturnRecordResponse = convertToResponse(returnSummary)
    val result = convertResponseToReturnViewRows(response.returnSummaryList)
    result.nonEmpty && result.map(_.status).toSet == Set(ACCEPTED, STARTED)
  }

}