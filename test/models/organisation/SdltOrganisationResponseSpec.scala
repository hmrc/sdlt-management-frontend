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

package models.organisation

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.Json

import java.time.LocalDate

class SdltOrganisationResponseSpec extends AnyFreeSpec with Matchers {

  val createdAgent = CreatedAgent("storn", Some("agentId"), "name", Some("houseNumber"), "address1", Some("address2"), Some("address3"), Some("address4"), Some("postcode"), Some("phone"), Some("email"), Some("dxAddress"), "agentResourceReference")

  "SdltOrganisationResponse" - {
    "should serialize to JSON" in {
      val formatter = SdltOrganisationResponse.format
      val orgResponse = SdltOrganisationResponse("storn", Some("version"), Some("isReturnUser"), Some("doNotDisplayWelcomePage"), Seq(createdAgent))

      val json = formatter.writes(orgResponse)

      (json \ "storn").as[String] mustBe "storn"
      (json \ "version").as[String] mustBe "version"
      (json \ "isReturnUser").as[String] mustBe "isReturnUser"
      (json \ "doNotDisplayWelcomePage").as[String] mustBe "doNotDisplayWelcomePage"
      (json \ "agents").as[Seq[CreatedAgent]] mustBe Seq(createdAgent)

    }
    "should deserialize from JSON" in {
      val json =
        """
              {
                "storn": "storn",
                "version": "version",
                "isReturnUser": "isReturnUser",
                "doNotDisplayWelcomePage": "doNotDisplayWelcomePage",
                "agents": [
                  {
                    "storn": "storn",
                    "agentId": "agentId",
                    "name": "name",
                    "houseNumber": "houseNumber",
                    "address1": "address1",
                    "address2": "address2",
                    "address3": "address3",
                    "address4": "address4",
                    "postcode": "postcode",
                    "phone": "phone",
                    "email": "email",
                    "dxAddress": "dxAddress",
                    "agentResourceReference": "agentResourceReference"
                  }
                ]
              }
        """

      val orgResponse = Json.parse(json).as[SdltOrganisationResponse]

      orgResponse mustBe SdltOrganisationResponse("storn", Some("version"), Some("isReturnUser"), Some("doNotDisplayWelcomePage"), Seq(createdAgent))
    }
  }


}