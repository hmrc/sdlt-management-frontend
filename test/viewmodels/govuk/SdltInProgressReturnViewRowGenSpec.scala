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
import org.scalacheck.*

import java.time.LocalDate

object SdltInProgressReturnViewRowGenSpec extends Properties("SdltInProgressReturnViewRow") {

  import models.responses.SdltInProgressReturnViewRow._
  import Prop.forAll

  val returnSummaryGen: Gen[SdltReturnRecordResponse] = for {
    returnReference <- Gen.alphaNumStr
    status <- Gen.oneOf('A', 'E', 'I', 'O', 'U', 'Y')
  } yield SdltReturnRecordResponse(
    storn = "STORN",
    returnSummaryCount = 20,
    returnSummaryList = List(
      ReturnSummary(
        returnReference = returnReference,
        utrn = "UTRN002",
        status = "VALIDATED",
        dateSubmitted = LocalDate.parse("2025-01-02"),
        purchaserName = "Name002",
        address = "Address002",
        agentReference = "AgentRef002"
      )
    )
  )


  property("convertResponseToViewRows") = forAll(returnSummaryGen) { returnSummary =>
    println(returnSummaryGen)
    convertResponseToViewRows(returnSummary).length > 0
  }

}