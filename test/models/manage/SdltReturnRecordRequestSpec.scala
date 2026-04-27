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

class SdltReturnRecordRequestSpec extends AnyFreeSpec with Matchers {

  "SdltReturnRecordRequest" - {
    "should serialize to JSON" in {
      val formatter = SdltReturnRecordRequest.format
      val returnRequest = SdltReturnRecordRequest(
        "storn",
        Some("status"),
        true,
        Some("pageType"),
        Some("pageNumber")
      )

      val json = formatter.writes(returnRequest)

      (json \ "storn").as[String] mustBe "storn"
      (json \ "status").as[String] mustBe "status"
      (json \ "deletionFlag").as[Boolean] mustBe true
      (json \ "pageType").as[String] mustBe "pageType"
      (json \ "pageNumber").as[String] mustBe "pageNumber"
    }
    "should deserialize from JSON" in {
      val json =
        """
              {
                    "storn": "storn",
                    "status": "status",
                    "deletionFlag": true,
                    "pageType": "pageType",
                    "pageNumber": "pageNumber"
                  }
        """

      val returnRequest = Json.parse(json).as[SdltReturnRecordRequest]

      returnRequest mustBe SdltReturnRecordRequest(
        "storn",
        Some("status"),
        true,
        Some("pageType"),
        Some("pageNumber")
      )
    }
  }
}
