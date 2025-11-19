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
import models.requests.DataRequest
import models.responses.SdltInProgressReturnViewRow
import models.responses.UniversalStatus.{ACCEPTED, STARTED, SUBMITTED, SUBMITTED_NO_RECEIPT}
import models.responses.organisation.SdltOrganisationResponse
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.*
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.http.HeaderCarrier
import viewmodels.manage.SdltSubmittedReturnsViewModel

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

  "getInProgressReturns" should {
    "merge ACCEPTED and STARTED IN-PROGRESS returns" in {
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
        status = "STARTED",
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

      when(connector.getReturns(eqTo(Some("STARTED")), eqTo(Some("IN-PROGRESS")), eqTo(false))(any[HeaderCarrier], any[DataRequest[_]]))
        .thenReturn(Future.successful(pendingResponse))

      val result = service.getInProgressReturns.futureValue

      val expected = List(
        SdltInProgressReturnViewRow(
          address = "1 Accepted Street",
          agentReference = "Accepted Agent",
          purchaserName = "Accepted Buyer",
          status = ACCEPTED
        ),
        SdltInProgressReturnViewRow(
          address = "2 Pending Street",
          agentReference = "Pending Agent",
          purchaserName = "Pending Buyer",
          status = STARTED
        )
      )

      result must contain theSameElementsAs expected

      verify(connector).getReturns(eqTo(Some("ACCEPTED")), eqTo(Some("IN-PROGRESS")), eqTo(false))(any[HeaderCarrier], any[DataRequest[_]])
      verify(connector).getReturns(eqTo(Some("STARTED")),  eqTo(Some("IN-PROGRESS")), eqTo(false))(any[HeaderCarrier], any[DataRequest[_]])
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

      val expected = List(
        SdltSubmittedReturnsViewModel(
          address = "3 Submitted Street",
          utrn = "UTRN-SUB-001",
          purchaserName = "Submitted Buyer",
          status = SUBMITTED
        ),
        SdltSubmittedReturnsViewModel(
          address = "4 NoReceipt Street",
          utrn = "UTRN-SNR-001",
          purchaserName = "No Receipt Buyer",
          status = SUBMITTED_NO_RECEIPT
        )
      )

      result must contain theSameElementsAs expected

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
        status = "SUBMITTED",
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
      result mustBe deletionResponse.returnSummaryList

      verify(connector).getReturns(eqTo(None), eqTo(None), eqTo(true))(any[HeaderCarrier], any[DataRequest[_]])
      verifyNoMoreInteractions(connector)
    }
  }

  "getAllAgents" should {
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
