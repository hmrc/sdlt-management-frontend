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

package connectors

import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, equalTo, get, stubFor, urlPathEqualTo}
import itutil.ApplicationWithWiremock
import models.manage.SdltReturnRecordResponse
import models.manageAgents.AgentDetailsResponse
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.http.Status._
import uk.gov.hmrc.http.HeaderCarrier

class StampDutyLandTaxConnectorISpec extends AnyWordSpec
  with Matchers
  with ScalaFutures
  with IntegrationPatience
  with ApplicationWithWiremock {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  private val connector: StampDutyLandTaxConnector =
    app.injector.instanceOf[StampDutyLandTaxConnector]

  private val storn = "STN001"

  "getAllReturns" should {

    val getAllReturnsUrl = s"/stamp-duty-land-tax/manage-returns/get-returns"

    "return SdltReturnRecordResponse when BE returns 200 with valid JSON" in {
      val validJson =
        """{
          |  "storn": "STN001",
          |  "returnSummaryCount": 2,
          |  "returnSummaryList": [
          |    {
          |      "returnReference": "RET20251101001",
          |      "utrn": "UTRN000001",
          |      "status": "SUBMITTED",
          |      "dateSubmitted": "2025-10-28",
          |      "purchaserName": "John Smith",
          |      "address": "10 Downing Street, London",
          |      "agentReference": "Smith & Co Solicitors"
          |    },
          |    {
          |      "returnReference": "RET20251101002",
          |      "utrn": "UTRN000002",
          |      "status": "ACCEPTED",
          |      "dateSubmitted": "2025-10-25",
          |      "purchaserName": "Jane Doe",
          |      "address": "221B Baker Street, London",
          |      "agentReference": "Anderson Legal LLP"
          |    }
          |  ]
          |}""".stripMargin

      stubFor(
        get(urlPathEqualTo(getAllReturnsUrl))
          .withQueryParam("storn", equalTo(storn))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(validJson)
          )
      )

      val result: SdltReturnRecordResponse =
        connector.getAllReturns(storn).futureValue

      result.storn mustBe "STN001"
      result.returnSummaryCount mustBe 2
      result.returnSummaryList.length mustBe 2
      result.returnSummaryList.head.returnReference mustBe "RET20251101001"
      result.returnSummaryList.head.status mustBe "SUBMITTED"
    }

    "fail when BE returns 200 with invalid JSON" in {
      stubFor(
        get(urlPathEqualTo(getAllReturnsUrl))
          .withQueryParam("storn", equalTo(storn))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody("""{ "unexpectedField": true }""")
          )
      )

      val ex = intercept[Exception] {
        connector.getAllReturns(storn).futureValue
      }
      ex.getMessage.toLowerCase must include ("storn")
    }

    "propagate an upstream error when BE returns 500" in {
      stubFor(
        get(urlPathEqualTo(getAllReturnsUrl))
          .withQueryParam("storn", equalTo(storn))
          .willReturn(
            aResponse()
              .withStatus(INTERNAL_SERVER_ERROR)
              .withBody("boom")
          )
      )

      val ex = intercept[Exception] {
        connector.getAllReturns(storn).futureValue
      }
      ex.getMessage must include ("returned 500")
    }
  }

  "getAllAgentDetails" should {

    val allAgentDetailsUrl = s"/stamp-duty-land-tax/manage-agents/agent-details/get-all-agents"

    "return a list of AgentDetails when BE returns 200 with valid JSON" in {
      stubFor(
        get(urlPathEqualTo(allAgentDetailsUrl))
          .withQueryParam("storn", equalTo(storn))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(
                """[
                  |  {
                  |    "agentReferenceNumber": "ARN001",
                  |    "agentName": "Acme Property Agents Ltd",
                  |    "addressLine1": "High Street",
                  |    "addressLine2": "Westminster",
                  |    "addressLine3": "London",
                  |    "addressLine4": "Greater London",
                  |    "postcode": "SW1A 2AA",
                  |    "phone": "02079460000",
                  |    "email": "info@acmeagents.co.uk"
                  |  },
                  |  {
                  |    "agentReferenceNumber": "ARN002",
                  |    "agentName": "Harborview Estates",
                  |    "houseNumber": "22A",
                  |    "addressLine1": "Queensway",
                  |    "addressLine3": "Birmingham",
                  |    "postcode": "B2 4ND",
                  |    "phone": "01214567890",
                  |    "email": "info@harborviewestates.co.uk"
                  |  }
                  |]""".stripMargin
              )
          )
      )

      val expected = List(
        AgentDetailsResponse(
          agentReferenceNumber = "ARN001",
          agentName            = "Acme Property Agents Ltd",
          addressLine1         = "High Street",
          addressLine2         = Some("Westminster"),
          addressLine3         = "London",
          addressLine4         = Some("Greater London"),
          postcode             = Some("SW1A 2AA"),
          phone                = Some("02079460000"),
          email                = "info@acmeagents.co.uk"
        ),
        AgentDetailsResponse(
          agentReferenceNumber = "ARN002",
          agentName            = "Harborview Estates",
          addressLine1         = "Queensway",
          addressLine2         = None,
          addressLine3         = "Birmingham",
          addressLine4         = None,
          postcode             = Some("B2 4ND"),
          phone                = Some("01214567890"),
          email                = "info@harborviewestates.co.uk"
        )
      )

      val result = connector.getAllAgentDetails(storn).futureValue
      result mustBe expected
    }

    "fail when BE returns 200 with invalid JSON" in {
      stubFor(
        get(urlPathEqualTo(allAgentDetailsUrl))
          .withQueryParam("storn", equalTo(storn))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody("""{ "unexpectedField": true }""")
          )
      )

      val ex = intercept[Exception] {
        connector.getAllAgentDetails(storn).futureValue
      }
      ex.getMessage.toLowerCase must include ("agent")
    }

    "propagate an upstream error when BE returns 500" in {
      stubFor(
        get(urlPathEqualTo(allAgentDetailsUrl))
          .withQueryParam("storn", equalTo(storn))
          .willReturn(
            aResponse()
              .withStatus(INTERNAL_SERVER_ERROR)
              .withBody("boom")
          )
      )

      val ex = intercept[Exception] {
        connector.getAllAgentDetails(storn).futureValue
      }
      ex.getMessage must include ("returned 500")
    }
  }
}