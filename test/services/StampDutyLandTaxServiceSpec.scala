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
import models.manage.{ReturnSummary, ReturnSummaryLegacy, SdltReturnRecordResponse, SdltReturnRecordResponseLegacy}
import models.manageAgents.AgentDetailsResponse
import models.requests.DataRequest
import models.responses.organisation.SdltOrganisationResponse
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

  private val summaries: List[ReturnSummaryLegacy] = List(
    ReturnSummaryLegacy(
      returnReference = "RET-001",
      utrn            = "UTRN-001",
      status          = "PENDING",
      dateSubmitted   = LocalDate.parse("2025-10-25"),
      purchaserName   = "John Smith",
      address         = "10 Downing Street, London",
      agentReference  = "Smith & Co Solicitors"
    ),
    ReturnSummaryLegacy(
      returnReference = "RET-002",
      utrn            = "UTRN-002",
      status          = "SUBMITTED",
      dateSubmitted   = LocalDate.parse("2025-10-25"),
      purchaserName   = "Jane Doe",
      address         = "221B Baker Street, London",
      agentReference  = "Anderson Legal LLP"
    ),
    ReturnSummaryLegacy(
      returnReference = "RET-003",
      utrn            = "UTRN-003",
      status          = "ACCEPTED",
      dateSubmitted   = LocalDate.parse("2025-10-25"),
      purchaserName   = "Alice",
      address         = "1 Queen’s Way, Birmingham",
      agentReference  = "Harborview Estates"
    ),
    ReturnSummaryLegacy(
      returnReference = "RET-004",
      utrn            = "UTRN-004",
      status          = "STARTED",
      dateSubmitted   = LocalDate.parse("2025-10-25"),
      purchaserName   = "Bob",
      address         = "Some Address",
      agentReference  = "Harborview Estates"
    ),
    ReturnSummaryLegacy(
      returnReference = "RET-005",
      utrn            = "UTRN-005",
      status          = "IN-PROGRESS",
      dateSubmitted   = LocalDate.parse("2025-10-25"),
      purchaserName   = "Charlie",
      address         = "Another Address",
      agentReference  = "Harborview Estates"
    ),
    ReturnSummaryLegacy(
      returnReference = "RET-006",
      utrn            = "UTRN-006",
      status          = "DUE_FOR_DELETION",
      dateSubmitted   = LocalDate.parse("2025-10-25"),
      purchaserName   = "Eve",
      address         = "Somewhere",
      agentReference  = "Harborview Estates"
    )
  )

  private val aggregateResponse = SdltReturnRecordResponseLegacy(
    storn               = storn,
    returnSummaryCount  = summaries.size,
    returnSummaryList   = summaries
  )

  "getAllPendingReturns (legacy)" should {
    "return only PENDING returns when BE returns Some(response)" in {
      val (service, connector) = newService()

      when(connector.getAllReturnsLegacy(eqTo(storn))(any[HeaderCarrier]))
        .thenReturn(Future.successful(aggregateResponse))

      val result = service.getReturnLegacy(storn, "PENDING").futureValue
      result.map(_.status).distinct mustBe List("PENDING")
      result.map(_.returnReference) mustBe List("RET-001")

      verify(connector).getAllReturnsLegacy(eqTo(storn))(any[HeaderCarrier])
      verifyNoMoreInteractions(connector)
    }
  }

  "getAllSubmittedReturns (legacy)" should {
    "return only SUBMITTED returns when BE returns Some(response)" in {
      val (service, connector) = newService()

      when(connector.getAllReturnsLegacy(eqTo(storn))(any[HeaderCarrier]))
        .thenReturn(Future.successful(aggregateResponse))

      val result = service.getReturnLegacy(storn, "SUBMITTED").futureValue
      result.map(_.status).distinct mustBe List("SUBMITTED")
      result.map(_.returnReference) mustBe List("RET-002")

      verify(connector).getAllReturnsLegacy(eqTo(storn))(any[HeaderCarrier])
      verifyNoMoreInteractions(connector)
    }

    "propagate connector failures" in {
      val (service, connector) = newService()

      when(connector.getAllReturnsLegacy(eqTo(storn))(any[HeaderCarrier]))
        .thenReturn(Future.failed(new RuntimeException("boom")))

      val ex = intercept[RuntimeException] {
        service.getReturnLegacy(storn, "SUBMITTED").futureValue
      }
      ex.getMessage must include ("boom")
    }
  }

  "getAllAcceptedReturns (legacy)" should {
    "return only ACCEPTED returns" in {
      val (service, connector) = newService()

      when(connector.getAllReturnsLegacy(eqTo(storn))(any[HeaderCarrier]))
        .thenReturn(Future.successful(aggregateResponse))

      val result = service.getReturnLegacy(storn, "ACCEPTED").futureValue
      result.map(_.status).distinct mustBe List("ACCEPTED")
      result.map(_.returnReference) mustBe List("RET-003")

      verify(connector).getAllReturnsLegacy(eqTo(storn))(any[HeaderCarrier])
      verifyNoMoreInteractions(connector)
    }
  }

  "getAllStartedReturns (legacy)" should {
    "return only STARTED returns" in {
      val (service, connector) = newService()

      when(connector.getAllReturnsLegacy(eqTo(storn))(any[HeaderCarrier]))
        .thenReturn(Future.successful(aggregateResponse))

      val result = service.getReturnLegacy(storn, "STARTED").futureValue
      result.map(_.status).distinct mustBe List("STARTED")
      result.map(_.returnReference) mustBe List("RET-004")

      verify(connector).getAllReturnsLegacy(eqTo(storn))(any[HeaderCarrier])
      verifyNoMoreInteractions(connector)
    }
  }

  "getAllInProgressReturns (legacy)" should {
    "return only IN-PROGRESS returns" in {
      val (service, connector) = newService()

      when(connector.getAllReturnsLegacy(eqTo(storn))(any[HeaderCarrier]))
        .thenReturn(Future.successful(aggregateResponse))

      val result = service.getReturnLegacy(storn, "IN-PROGRESS").futureValue
      result.map(_.status).distinct mustBe List("IN-PROGRESS")
      result.map(_.returnReference) mustBe List("RET-005")

      verify(connector).getAllReturnsLegacy(eqTo(storn))(any[HeaderCarrier])
      verifyNoMoreInteractions(connector)
    }
  }

  "getReturnsDueForDeletion (legacy)" should {
    "return only DUE_FOR_DELETION returns" in {
      val (service, connector) = newService()

      when(connector.getAllReturnsLegacy(eqTo(storn))(any[HeaderCarrier]))
        .thenReturn(Future.successful(aggregateResponse))

      val result = service.getReturnLegacy(storn, "DUE_FOR_DELETION").futureValue
      result.map(_.status).distinct mustBe List("DUE_FOR_DELETION")
      result.map(_.returnReference) mustBe List("RET-006")

      verify(connector).getAllReturnsLegacy(eqTo(storn))(any[HeaderCarrier])
      verifyNoMoreInteractions(connector)
    }
  }

  "getAllAgents (legacy)" should {
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

      when(connector.getAllAgentDetailsLegacy(eqTo(storn))(any[HeaderCarrier]))
        .thenReturn(Future.successful(payload))

      val result = service.getAllAgentsLegacy(storn).futureValue
      result mustBe payload

      verify(connector).getAllAgentDetailsLegacy(eqTo(storn))(any[HeaderCarrier])
      verifyNoMoreInteractions(connector)
    }

    "propagate failures from the connector" in {
      val (service, connector) = newService()

      when(connector.getAllAgentDetailsLegacy(eqTo(storn))(any[HeaderCarrier]))
        .thenReturn(Future.failed(new RuntimeException("boom")))

      val ex = intercept[RuntimeException] {
        service.getAllAgentsLegacy(storn).futureValue
      }
      ex.getMessage must include ("boom")
    }
  }

  "getInProgressReturns" should {
    "merge ACCEPTED and PENDING IN-PROGRESS returns" in {
      val (service, connector) = newService()
      implicit val request: DataRequest[_] = mock(classOf[DataRequest[_]])

      val acceptedSummary = ReturnSummary(
        returnReference = "RET-ACC-001",
        utrn = Some("UTRN-ACC-001"),
        status = "ACCEPTED",
        dateSubmitted = Some(LocalDate.parse("2025-10-20")),
        purchaserName = "Accepted Buyer",
        address = "1 Accepted Street",
        agentReference = Some("Accepted Agent")
      )

      val pendingSummary = ReturnSummary(
        returnReference = "RET-PEN-001",
        utrn = Some("UTRN-PEN-001"),
        status = "PENDING",
        dateSubmitted = Some(LocalDate.parse("2025-10-21")),
        purchaserName = "Pending Buyer",
        address = "2 Pending Street",
        agentReference = Some("Pending Agent")
      )

      val acceptedResponse = SdltReturnRecordResponse(
        returnSummaryCount = Some(1),
        returnSummaryList = List(acceptedSummary)
      )

      val pendingResponse = SdltReturnRecordResponse(
        returnSummaryCount = Some(1),
        returnSummaryList = List(pendingSummary)
      )

      when(connector.getReturns(eqTo(Some("ACCEPTED")), eqTo(Some("IN-PROGRESS")), eqTo(false))(any[HeaderCarrier], any[DataRequest[_]]))
        .thenReturn(Future.successful(acceptedResponse))

      when(connector.getReturns(eqTo(Some("PENDING")), eqTo(Some("IN-PROGRESS")), eqTo(false))(any[HeaderCarrier], any[DataRequest[_]]))
        .thenReturn(Future.successful(pendingResponse))

      val result = service.getInProgressReturns.futureValue

      result.returnSummaryList must contain theSameElementsInOrderAs List(acceptedSummary, pendingSummary)
      // we don't care about returnSummaryCount here; service doesn't set it
      verify(connector).getReturns(eqTo(Some("ACCEPTED")), eqTo(Some("IN-PROGRESS")), eqTo(false))(any[HeaderCarrier], any[DataRequest[_]])
      verify(connector).getReturns(eqTo(Some("PENDING")), eqTo(Some("IN-PROGRESS")), eqTo(false))(any[HeaderCarrier], any[DataRequest[_]])
      verifyNoMoreInteractions(connector)
    }
  }

  "getSubmittedReturns" should {
    "merge SUBMITTED and SUBMITTED_NO_RECEIPT returns" in {
      val (service, connector) = newService()
      implicit val request: DataRequest[_] = mock(classOf[DataRequest[_]])

      val submitted = ReturnSummary(
        returnReference = "RET-SUB-001",
        utrn = Some("UTRN-SUB-001"),
        status = "SUBMITTED",
        dateSubmitted = Some(LocalDate.parse("2025-10-22")),
        purchaserName = "Submitted Buyer",
        address = "3 Submitted Street",
        agentReference = Some("Submitted Agent")
      )

      val submittedNoReceipt = ReturnSummary(
        returnReference = "RET-SNR-001",
        utrn = Some("UTRN-SNR-001"),
        status = "SUBMITTED_NO_RECEIPT",
        dateSubmitted = Some(LocalDate.parse("2025-10-23")),
        purchaserName = "No Receipt Buyer",
        address = "4 NoReceipt Street",
        agentReference = Some("NoReceipt Agent")
      )

      val submittedResponse = SdltReturnRecordResponse(
        returnSummaryCount = Some(1),
        returnSummaryList = List(submitted)
      )

      val submittedNoReceiptResponse = SdltReturnRecordResponse(
        returnSummaryCount = Some(1),
        returnSummaryList = List(submittedNoReceipt)
      )

      when(connector.getReturns(eqTo(Some("SUBMITTED")), eqTo(Some("SUBMITTED")), eqTo(false))(any[HeaderCarrier], any[DataRequest[_]]))
        .thenReturn(Future.successful(submittedResponse))

      when(connector.getReturns(eqTo(Some("SUBMITTED_NO_RECEIPT")), eqTo(Some("SUBMITTED")), eqTo(false))(any[HeaderCarrier], any[DataRequest[_]]))
        .thenReturn(Future.successful(submittedNoReceiptResponse))

      val result = service.getSubmittedReturns.futureValue

      result.returnSummaryList must contain theSameElementsInOrderAs List(submitted, submittedNoReceipt)

      verify(connector)
        .getReturns(eqTo(Some("SUBMITTED")), eqTo(Some("SUBMITTED")), eqTo(false))(
          any[HeaderCarrier], any[DataRequest[_]]
        )
      verify(connector)
        .getReturns(eqTo(Some("SUBMITTED_NO_RECEIPT")), eqTo(Some("SUBMITTED")), eqTo(false))(
          any[HeaderCarrier], any[DataRequest[_]]
        )
      verifyNoMoreInteractions(connector)
    }
  }

  "getReturnsDueForDeletion" should {
    "delegate to connector with deletionFlag = true and return the response" in {
      val (service, connector) = newService()
      implicit val request: DataRequest[_] = mock(classOf[DataRequest[_]])

      val deletionSummary = ReturnSummary(
        returnReference = "RET-DEL-001",
        utrn = Some("UTRN-DEL-001"),
        status = "DUE_FOR_DELETION",
        dateSubmitted = Some(LocalDate.parse("2025-10-24")),
        purchaserName = "Delete Buyer",
        address = "5 Delete Street",
        agentReference = Some("Delete Agent")
      )

      val deletionResponse = SdltReturnRecordResponse(
        returnSummaryCount = Some(1),
        returnSummaryList = List(deletionSummary)
      )

      when(connector.getReturns(eqTo(None), eqTo(None), eqTo(true))(any[HeaderCarrier], any[DataRequest[_]]))
        .thenReturn(Future.successful(deletionResponse))

      val result = service.getReturnsDueForDeletion.futureValue
      result mustBe deletionResponse

      verify(connector).getReturns(eqTo(None), eqTo(None), eqTo(true))(any[HeaderCarrier], any[DataRequest[_]])
      verifyNoMoreInteractions(connector)
    }
  }

  "getAllAgents (new API)" should {
    "use getSdltOrganisation and return the agents count" in {
      val (service, connector) = newService()
      implicit val request: DataRequest[_] = mock(classOf[DataRequest[_]])

      val agents = Seq(
        AgentDetailsResponse(
          agentReferenceNumber = "ARN001",
          agentName = "Acme Property Agents Ltd",
          addressLine1 = "High Street",
          addressLine2 = Some("Westminster"),
          addressLine3 = "London",
          addressLine4 = Some("Greater London"),
          postcode = Some("SW1A 2AA"),
          phone = Some("02079460000"),
          email = "info@acmeagents.co.uk"
        ),
        AgentDetailsResponse(
          agentReferenceNumber = "ARN002",
          agentName = "Harborview Estates",
          addressLine1 = "Queensway",
          addressLine2 = None,
          addressLine3 = "Birmingham",
          addressLine4 = None,
          postcode = Some("B2 4ND"),
          phone = Some("01214567890"),
          email = "info@harborviewestates.co.uk"
        )
      )

      val orgResponse = SdltOrganisationResponse(
        storn = storn,
        version = 1,
        isReturnUser = "Y",
        doNotDisplayWelcomePage = "N",
        agents = agents
      )

      when(connector.getSdltOrganisation(any[HeaderCarrier], any[DataRequest[_]]))
        .thenReturn(Future.successful(orgResponse))

      val result = service.getAgentCount.futureValue
      result mustBe 2

      verify(connector).getSdltOrganisation(any[HeaderCarrier], any[DataRequest[_]])
      verifyNoMoreInteractions(connector)
    }
  }


}