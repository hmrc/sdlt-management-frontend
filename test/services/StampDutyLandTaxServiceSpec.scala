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

package services

import connectors.StampDutyLandTaxConnector
import models.manage.{ReturnSummary, SdltReturnRecordResponse}
import models.manageAgents.AgentDetailsResponse
import models.responses.UniversalStatus.{SUBMITTED, SUBMITTED_NO_RECEIPT}
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.*
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class StampDutyLandTaxServiceSpec extends AnyWordSpec with ScalaFutures with Matchers {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  private def newService(): (StampDutyLandTaxService, StampDutyLandTaxConnector) = {
    val connector = mock(classOf[StampDutyLandTaxConnector])
    val service   = new StampDutyLandTaxService(connector)
    (service, connector)
  }

  private val storn = "STN001"

  private val summaries: List[ReturnSummary] = List(
    ReturnSummary(
      returnReference = "RET-001",
      utrn            = "UTRN-001",
      status          = "PENDING",
      dateSubmitted   = LocalDate.parse("2025-10-25"),
      purchaserName   = "John Smith",
      address         = "10 Downing Street, London",
      agentReference  = "Smith & Co Solicitors"
    ),
    ReturnSummary(
      returnReference = "RET-002",
      utrn            = "UTRN-002",
      status          = "SUBMITTED",
      dateSubmitted   = LocalDate.parse("2025-10-25"),
      purchaserName   = "Jane Doe",
      address         = "221B Baker Street, London",
      agentReference  = "Anderson Legal LLP"
    ),
    ReturnSummary(
      returnReference = "RET-003",
      utrn            = "UTRN-003",
      status          = "ACCEPTED",
      dateSubmitted   = LocalDate.parse("2025-10-25"),
      purchaserName   = "Alice",
      address         = "1 Queen’s Way, Birmingham",
      agentReference  = "Harborview Estates"
    ),
    ReturnSummary(
      returnReference = "RET-004",
      utrn            = "UTRN-004",
      status          = "STARTED",
      dateSubmitted   = LocalDate.parse("2025-10-25"),
      purchaserName   = "Bob",
      address         = "Some Address",
      agentReference  = "Harborview Estates"
    ),
    ReturnSummary(
      returnReference = "RET-005",
      utrn            = "UTRN-005",
      status          = "IN-PROGRESS",
      dateSubmitted   = LocalDate.parse("2025-10-25"),
      purchaserName   = "Charlie",
      address         = "Another Address",
      agentReference  = "Harborview Estates"
    ),
    ReturnSummary(
      returnReference = "RET-006",
      utrn            = "UTRN-006",
      status          = "DUE_FOR_DELETION",
      dateSubmitted   = LocalDate.parse("2025-10-25"),
      purchaserName   = "Eve",
      address         = "Somewhere",
      agentReference  = "Harborview Estates"
    )
  )

  private val aggregateResponse = SdltReturnRecordResponse(
    storn               = storn,
    returnSummaryCount  = summaries.size,
    returnSummaryList   = summaries
  )

  "getAllPendingReturns" should {
    "return only PENDING returns when BE returns Some(response)" in {
      val (service, connector) = newService()

      when(connector.getAllReturns(eqTo(storn))(any[HeaderCarrier]))
        .thenReturn(Future.successful(aggregateResponse))

      val result = service.getReturn(storn, "PENDING").futureValue
      result.map(_.status).distinct mustBe List("PENDING")
      result.map(_.returnReference) mustBe List("RET-001")

      verify(connector).getAllReturns(eqTo(storn))(any[HeaderCarrier])
      verifyNoMoreInteractions(connector)
    }
  }

  "getAllSubmittedReturns" should {
    "return only SUBMITTED returns when BE returns Some(response)" in {
      val (service, connector) = newService()

      when(connector.getAllReturns(eqTo(storn))(any[HeaderCarrier]))
        .thenReturn(Future.successful(aggregateResponse))

      val result = service.getReturn(storn, "SUBMITTED").futureValue
      result.map(_.status).distinct mustBe List("SUBMITTED")
      result.map(_.returnReference) mustBe List("RET-002")

      verify(connector).getAllReturns(eqTo(storn))(any[HeaderCarrier])
      verifyNoMoreInteractions(connector)
    }

    "propagate connector failures" in {
      val (service, connector) = newService()

      when(connector.getAllReturns(eqTo(storn))(any[HeaderCarrier]))
        .thenReturn(Future.failed(new RuntimeException("boom")))

      val ex = intercept[RuntimeException] {
        service.getReturn(storn, "SUBMITTED").futureValue
      }
      ex.getMessage must include ("boom")
    }
  }

  "getAllAcceptedReturns" should {
    "return only ACCEPTED returns" in {
      val (service, connector) = newService()

      when(connector.getAllReturns(eqTo(storn))(any[HeaderCarrier]))
        .thenReturn(Future.successful(aggregateResponse))

      val result = service.getReturn(storn, "ACCEPTED").futureValue
      result.map(_.status).distinct mustBe List("ACCEPTED")
      result.map(_.returnReference) mustBe List("RET-003")

      verify(connector).getAllReturns(eqTo(storn))(any[HeaderCarrier])
      verifyNoMoreInteractions(connector)
    }
  }

  "getAllStartedReturns" should {
    "return only STARTED returns" in {
      val (service, connector) = newService()

      when(connector.getAllReturns(eqTo(storn))(any[HeaderCarrier]))
        .thenReturn(Future.successful(aggregateResponse))

      val result = service.getReturn(storn, "STARTED").futureValue
      result.map(_.status).distinct mustBe List("STARTED")
      result.map(_.returnReference) mustBe List("RET-004")

      verify(connector).getAllReturns(eqTo(storn))(any[HeaderCarrier])
      verifyNoMoreInteractions(connector)
    }
  }

  "getAllInProgressReturns" should {
    "return only IN-PROGRESS returns" in {
      val (service, connector) = newService()

      when(connector.getAllReturns(eqTo(storn))(any[HeaderCarrier]))
        .thenReturn(Future.successful(aggregateResponse))

      val result = service.getReturn(storn, "IN-PROGRESS").futureValue
      result.map(_.status).distinct mustBe List("IN-PROGRESS")
      result.map(_.returnReference) mustBe List("RET-005")

      verify(connector).getAllReturns(eqTo(storn))(any[HeaderCarrier])
      verifyNoMoreInteractions(connector)
    }
  }

  "getReturnsDueForDeletion" should {
    "return only DUE_FOR_DELETION returns" in {
      val (service, connector) = newService()

      when(connector.getAllReturns(eqTo(storn))(any[HeaderCarrier]))
        .thenReturn(Future.successful(aggregateResponse))

      val result = service.getReturn(storn, "DUE_FOR_DELETION").futureValue
      result.map(_.status).distinct mustBe List("DUE_FOR_DELETION")
      result.map(_.returnReference) mustBe List("RET-006")

      verify(connector).getAllReturns(eqTo(storn))(any[HeaderCarrier])
      verifyNoMoreInteractions(connector)
    }
  }

  "getAllAgents" should {
    "delegate to connector with the given storn and return the payload" in {
      val (service, connector) = newService()

      val payload = List(
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

      when(connector.getAllAgentDetails(eqTo(storn))(any[HeaderCarrier]))
        .thenReturn(Future.successful(payload))

      val result = service.getAllAgents(storn).futureValue
      result mustBe payload

      verify(connector).getAllAgentDetails(eqTo(storn))(any[HeaderCarrier])
      verifyNoMoreInteractions(connector)
    }

    "propagate failures from the connector" in {
      val (service, connector) = newService()

      when(connector.getAllAgentDetails(eqTo(storn))(any[HeaderCarrier]))
        .thenReturn(Future.failed(new RuntimeException("boom")))

      val ex = intercept[RuntimeException] {
        service.getAllAgents(storn).futureValue
      }
      ex.getMessage must include ("boom")
    }
  }

  "getSubmittedReturnsView" should {
    "return all returns but filter for all SUBMITTED and SUBMITTED_NO_RECEIPT returns when converted to SdltSubmittedReturnsViewModel" in {
      val (service, connector) = newService()

      when(connector.getAllReturns(eqTo(storn))(any[HeaderCarrier]))
        .thenReturn(Future.successful(aggregateResponse))

      val result = service.getSubmittedReturnsView(storn).futureValue

      println(result.map(_.status).distinct)
      println(SUBMITTED)
      println(SUBMITTED_NO_RECEIPT)

      val statuses = result.map(_.status).distinct
      statuses.forall(s => s == SUBMITTED || s == SUBMITTED_NO_RECEIPT) mustBe true

      verify(connector).getAllReturns(eqTo(storn))(any[HeaderCarrier])
      verifyNoMoreInteractions(connector)
    }

    "propagate failures from the connector" in {
      val (service, connector) = newService()

      when(connector.getAllReturns(eqTo(storn))(any[HeaderCarrier]))
        .thenReturn(Future.failed(new RuntimeException("Error: Connector issue")))

      val ex = intercept[RuntimeException] {
        service.getSubmittedReturnsView(storn).futureValue
      }
      ex.getMessage must include("Error: Connector issue")
    }
  }
}