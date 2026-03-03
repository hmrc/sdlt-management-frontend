/*
 * Copyright 2026 HM Revenue & Customs
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

package models.manage

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.Json

import java.time.LocalDate

class SdltReturnRecordResponseSpec extends AnyFreeSpec with Matchers {



  "SdltReturnRecordResponse" - {
    "should serialize to JSON" in {
      val formatter = SdltReturnRecordResponse.format
      val returnResponse = SdltReturnRecordResponse(1, List(ReturnSummary("returnReference", Some("utrn"), "status", Some(LocalDate.parse("2019-03-20")), "purchaserName", "address", Some("agentReference"))))

      val json = formatter.writes(returnResponse)

      (json \ "returnSummaryCount").as[Int] mustBe 1
      (json \ "returnSummaryList")(0).as[ReturnSummary] mustBe ReturnSummary("returnReference", Some("utrn"), "status", Some(LocalDate.parse("2019-03-20")), "purchaserName", "address", Some("agentReference"))
    }
    "should deserialize from JSON" in {
      val json =
        """
              {
                "returnSummaryCount": 1,
                "returnSummaryList": [
                  { "returnReference": "returnReference", "utrn": "utrn", "status": "status", "dateSubmitted": "2019-03-20", "purchaserName": "purchaserName", "address": "address", "agentReference": "agentReference"}
                ]
              }
        """

      val returnResponse = Json.parse(json).as[SdltReturnRecordResponse]

      returnResponse mustBe SdltReturnRecordResponse(1, List(ReturnSummary("returnReference", Some("utrn"), "status", Some(LocalDate.parse("2019-03-20")), "purchaserName", "address", Some("agentReference"))))
    }
  }
  "ReturnSummary" - {
    "should serialize to JSON" in {
      val formatter = ReturnSummary.format
      val returnSummary = ReturnSummary("returnReference", Some("utrn"), "status", Some(LocalDate.parse("2019-03-20")), "purchaserName", "address", Some("agentReference"))

      val json = formatter.writes(returnSummary)

      (json \ "returnReference").as[String] mustBe "returnReference"
      (json \ "utrn").as[String] mustBe "utrn"
      (json \ "status").as[String] mustBe "status"
      (json \ "dateSubmitted").as[String] mustBe "2019-03-20"
      (json \ "purchaserName").as[String] mustBe "purchaserName"
      (json \ "address").as[String] mustBe "address"
      (json \ "agentReference").as[String] mustBe "agentReference"
    }

    "should deserialize from JSON" in {
      val json =
        """
                  {
                    "returnReference": "returnReference",
                    "utrn": "utrn",
                    "status": "status",
                    "dateSubmitted": "2019-03-20",
                    "purchaserName": "purchaserName",
                    "address": "address",
                    "agentReference": "agentReference"
                  }
        """

      val returnSummary = Json.parse(json).as[ReturnSummary]

      returnSummary mustBe ReturnSummary("returnReference", Some("utrn"), "status", Some(LocalDate.parse("2019-03-20")), "purchaserName", "address", Some("agentReference"))
    }
  }

}