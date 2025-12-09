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

package services

import connectors.StampDutyLandTaxConnector
import models.SdltReturnTypes.{
  IN_PROGRESS_RETURNS,
  IN_PROGRESS_RETURNS_DUE_FOR_DELETION,
  SUBMITTED_RETURNS_DUE_FOR_DELETION,
  SUBMITTED_SUBMITTED_RETURNS
}
import models.manage.{ReturnSummary, SdltReturnRecordRequest, SdltReturnRecordResponse}
import models.organisation.{CreatedAgent, SdltOrganisationResponse}
import models.requests.DataRequest
import models.responses.*
import models.responses.UniversalStatus.{ACCEPTED, STARTED, SUBMITTED, SUBMITTED_NO_RECEIPT}
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
      utrn           = Some("UTRN-001"),
      status         = "PENDING",
      dateSubmitted  = Some(LocalDate.parse("2025-10-25")),
      purchaserName  = "John Smith",
      address        = "10 Downing Street, London",
      agentReference = Some("Smith & Co Solicitors")
    ),
    ReturnSummary(
      returnReference = "RET-002",
      utrn           = Some("UTRN-002"),
      status         = "SUBMITTED",
      dateSubmitted  = Some(LocalDate.parse("2025-10-25")),
      purchaserName  = "Jane Doe",
      address        = "221B Baker Street, London",
      agentReference = Some("Anderson Legal LLP")
    ),
    ReturnSummary(
      returnReference = "RET-003",
      utrn           = Some("UTRN-003"),
      status         = "ACCEPTED",
      dateSubmitted  = Some(LocalDate.parse("2025-10-25")),
      purchaserName  = "Alice",
      address        = "1 Queen’s Way, Birmingham",
      agentReference = Some("Harborview Estates")
    ),
    ReturnSummary(
      returnReference = "RET-004",
      utrn           = Some("UTRN-004"),
      status         = "STARTED",
      dateSubmitted  = Some(LocalDate.parse("2025-10-25")),
      purchaserName  = "Bob",
      address        = "Some Address",
      agentReference = Some("Harborview Estates")
    ),
    ReturnSummary(
      returnReference = "RET-005",
      utrn           = Some("UTRN-005"),
      status         = "IN-PROGRESS",
      dateSubmitted  = Some(LocalDate.parse("2025-10-25")),
      purchaserName  = "Charlie",
      address        = "Another Address",
      agentReference = Some("Harborview Estates")
    ),
    ReturnSummary(
      returnReference = "RET-006",
      utrn           = Some("UTRN-006"),
      status         = "DUE_FOR_DELETION",
      dateSubmitted  = Some(LocalDate.parse("2025-10-25")),
      purchaserName  = "Eve",
      address        = "Somewhere",
      agentReference = Some("Harborview Estates")
    )
  )

  private val aggregateResponse = SdltReturnRecordResponse(
    returnSummaryCount = summaries.size,
    returnSummaryList  = summaries
  )

  "getInProgressReturns" should {

    "merge ACCEPTED and STARTED IN-PROGRESS returns" in {
      val (service, connector) = newService()
      implicit val request: DataRequest[_] = mock(classOf[DataRequest[_]])

      val acceptedSummary = ReturnSummary(
        returnReference = "RET-ACC-001",
        utrn           = Some("UTRN-ACC-001"),
        status         = "ACCEPTED",
        dateSubmitted  = Some(LocalDate.parse("2025-10-20")),
        purchaserName  = "Accepted Buyer",
        address        = "1 Accepted Street",
        agentReference = Some("Accepted Agent")
      )

      val pendingSummary = ReturnSummary(
        returnReference = "RET-PEN-001",
        utrn           = Some("UTRN-PEN-001"),
        status         = "STARTED",
        dateSubmitted  = Some(LocalDate.parse("2025-10-21")),
        purchaserName  = "Pending Buyer",
        address        = "2 Pending Street",
        agentReference = Some("Pending Agent")
      )

      val dataRows = List(
        SdltReturnViewRow(
          address        = "1 Accepted Street",
          agentReference = "Accepted Agent",
          purchaserName  = "Accepted Buyer",
          status         = ACCEPTED,
          utrn           = "UTRN-ACC-001"
        ),
        SdltReturnViewRow(
          address        = "2 Pending Street",
          agentReference = "Pending Agent",
          purchaserName  = "Pending Buyer",
          status         = STARTED,
          utrn           = "UTRN-PEN-001"
        )
      )

      val inProgressReturnsResponse = SdltReturnRecordResponse(
        returnSummaryCount = dataRows.length,
        returnSummaryList  = List(acceptedSummary, pendingSummary)
      )

      val inProgressRequest: SdltReturnRecordRequest = SdltReturnRecordRequest(
        storn        = storn,
        status       = None,
        deletionFlag = false,
        pageType     = Some("IN-PROGRESS"),
        pageNumber   = Some("1")
      )

      when(connector.getReturns(eqTo(inProgressRequest))(any[HeaderCarrier]))
        .thenReturn(Future.successful(inProgressReturnsResponse))

      val result = service.getReturnsByTypeViewModel[SdltInProgressReturnViewModel](storn, IN_PROGRESS_RETURNS, Some(1)).futureValue

      val expected = SdltInProgressReturnViewModel(
        extractType   = IN_PROGRESS_RETURNS,
        rows          = dataRows,
        totalRowCount = dataRows.length,
        selectedPageIndex = 1
      )

      result.totalRowCount mustBe expected.totalRowCount
      result.rows must contain theSameElementsAs expected.rows

      verify(connector).getReturns(eqTo(inProgressRequest))(any[HeaderCarrier])
      verifyNoMoreInteractions(connector)
    }
  }

  "getSubmittedReturns" should {

    "merge SUBMITTED and SUBMITTED_NO_RECEIPT returns from a single connector call" in {
      val (service, connector) = newService()
      implicit val request: DataRequest[_] = mock(classOf[DataRequest[_]])

      val submitted = ReturnSummary(
        returnReference = "RET-SUB-001",
        utrn           = Some("UTRN-SUB-001"),
        status         = "SUBMITTED",
        dateSubmitted  = Some(LocalDate.parse("2025-10-22")),
        purchaserName  = "Submitted Buyer",
        address        = "3 Submitted Street",
        agentReference = Some("Submitted Agent")
      )

      val submittedNoReceipt = ReturnSummary(
        returnReference = "RET-SNR-001",
        utrn           = Some("UTRN-SNR-001"),
        status         = "SUBMITTED_NO_RECEIPT",
        dateSubmitted  = Some(LocalDate.parse("2025-10-23")),
        purchaserName  = "No Receipt Buyer",
        address        = "4 NoReceipt Street",
        agentReference = Some("NoReceipt Agent")
      )

      val submittedResponse = SdltReturnRecordResponse(
        returnSummaryCount = 2,
        returnSummaryList  = List(submitted, submittedNoReceipt)
      )

      val expectedRequest: SdltReturnRecordRequest = SdltReturnRecordRequest(
        storn        = storn,
        status       = None,
        deletionFlag = false,
        pageType     = Some("SUBMITTED"),
        pageNumber   = Some("1")
      )

      when(connector.getReturns(eqTo(expectedRequest))(any[HeaderCarrier]))
        .thenReturn(Future.successful(submittedResponse))

      val result = service.getReturnsByTypeViewModel[SdltSubmittedReturnViewModel](storn, SUBMITTED_SUBMITTED_RETURNS, Some(1)).futureValue

      val expectedRows = List(
        SdltReturnViewRow(
          address        = "3 Submitted Street",
          utrn           = "UTRN-SUB-001",
          purchaserName  = "Submitted Buyer",
          status         = SUBMITTED,
          agentReference = "Submitted Agent"
        ),
        SdltReturnViewRow(
          address        = "4 NoReceipt Street",
          utrn           = "UTRN-SNR-001",
          purchaserName  = "No Receipt Buyer",
          status         = SUBMITTED_NO_RECEIPT,
          agentReference = "NoReceipt Agent"
        )
      )

      val expected = SdltSubmittedReturnViewModel(
        extractType   = SUBMITTED_SUBMITTED_RETURNS,
        rows          = expectedRows,
        totalRowCount = 2,
        selectedPageIndex = 1
      )

      result mustBe expected

      verify(connector).getReturns(eqTo(expectedRequest))(any[HeaderCarrier])
      verifyNoMoreInteractions(connector)
    }

    "propagate failures from the connector" in {
      val (service, connector) = newService()
      implicit val request: DataRequest[_] = mock(classOf[DataRequest[_]])

      val expectedRequest: SdltReturnRecordRequest = SdltReturnRecordRequest(
        storn        = storn,
        status       = None,
        deletionFlag = false,
        pageType     = Some("SUBMITTED"),
        pageNumber   = Some("1")
      )

      when(connector.getReturns(eqTo(expectedRequest))(any[HeaderCarrier]))
        .thenReturn(Future.failed(new RuntimeException("Error: Connector issue")))

      val ex = intercept[RuntimeException] {
        service.getReturnsByTypeViewModel[SdltSubmittedReturnViewModel](storn, SUBMITTED_SUBMITTED_RETURNS, Some(1)).futureValue
      }

      ex.getMessage must include("Error: Connector issue")

      verify(connector).getReturns(eqTo(expectedRequest))(any[HeaderCarrier])
      verifyNoMoreInteractions(connector)
    }
  }

  "getSubmittedReturnsDueForDeletion" should {

    "call the connector with deletionFlag = true for SUBMITTED and return the response" in {
      val (service, connector) = newService()
      implicit val request: DataRequest[_] = mock(classOf[DataRequest[_]])

      val submittedDeletionSummary =
        ReturnSummary(
          returnReference = "RET-DEL-001",
          utrn           = Some("UTRN-DEL-001"),
          status         = "SUBMITTED",
          dateSubmitted  = Some(LocalDate.parse("2025-10-24")),
          purchaserName  = "Delete Buyer",
          address        = "5 Delete Street",
          agentReference = Some("Delete Agent")
        )

      val submittedDeletionResponse = SdltReturnRecordResponse(
        returnSummaryCount = 1,
        returnSummaryList  = List(submittedDeletionSummary)
      )

      val submittedRequest: SdltReturnRecordRequest = SdltReturnRecordRequest(
        storn        = storn,
        status       = None,
        deletionFlag = true,
        pageType     = Some("SUBMITTED"),
        pageNumber   = Some("1")
      )

      when(connector.getReturns(eqTo(submittedRequest))(any[HeaderCarrier]))
        .thenReturn(Future.successful(submittedDeletionResponse))

      val result =
        service.getReturnsByTypeViewModel[SdltSubmittedDueForDeletionReturnViewModel](storn, SUBMITTED_RETURNS_DUE_FOR_DELETION, Some(1)).futureValue

      result.rows must contain theSameElementsAs List(
        SdltReturnViewRow(
          utrn           = "UTRN-DEL-001",
          status         = SUBMITTED,
          purchaserName  = "Delete Buyer",
          address        = "5 Delete Street",
          agentReference = "Delete Agent"
        )
      )

      verify(connector).getReturns(eqTo(submittedRequest))(any[HeaderCarrier])
      verifyNoMoreInteractions(connector)
    }
  }

  "getInProgressReturnsDueForDeletion" should {

    "call the connector with deletionFlag = true for IN-PROGRESS and return the response" in {
      val (service, connector) = newService()
      implicit val request: DataRequest[_] = mock(classOf[DataRequest[_]])

      val inProgressDeletionSummary =
        ReturnSummary(
          returnReference = "RET-DEL-002",
          utrn           = Some("UTRN-DEL-002"),
          status         = "IN-PROGRESS",
          dateSubmitted  = Some(LocalDate.parse("2025-10-24")),
          purchaserName  = "In Progress Buyer",
          address        = "6 Delete Street",
          agentReference = Some("Delete Agent 2")
        )

      val inProgressDeletionResponse = SdltReturnRecordResponse(
        returnSummaryCount = 1,
        returnSummaryList  = List(inProgressDeletionSummary)
      )

      val inProgressRequest: SdltReturnRecordRequest = SdltReturnRecordRequest(
        storn        = storn,
        status       = None,
        deletionFlag = true,
        pageType     = Some("IN-PROGRESS"),
        pageNumber   = Some("1")
      )

      when(connector.getReturns(eqTo(inProgressRequest))(any[HeaderCarrier]))
        .thenReturn(Future.successful(inProgressDeletionResponse))

      val result =
        service.getReturnsByTypeViewModel[SdltInProgressDueForDeletionReturnViewModel](storn, IN_PROGRESS_RETURNS_DUE_FOR_DELETION, Some(1)).futureValue

      result.totalRowCount mustBe 1

      verify(connector).getReturns(eqTo(inProgressRequest))(any[HeaderCarrier])
      verifyNoMoreInteractions(connector)
    }
  }

  "getAllAgents" should {

    "use getSdltOrganisation and return the agents count" in {
      val (service, connector) = newService()
      implicit val request: DataRequest[_] = mock(classOf[DataRequest[_]])

      val agents = Seq(
        CreatedAgent(
          storn                  = storn,
          agentId                = None,
          name                   = "Acme Property Agents Ltd",
          houseNumber            = None,
          address1               = "High Street",
          address2               = Some("Westminster"),
          address3               = Some("London"),
          address4               = Some("Greater London"),
          postcode               = Some("SW1A 2AA"),
          phone                  = "02079460000",
          email                  = "info@acmeagents.co.uk",
          dxAddress              = None,
          agentResourceReference = "ARN001"
        ),
        CreatedAgent(
          storn                  = storn,
          agentId                = None,
          name                   = "Harborview Estates",
          houseNumber            = None,
          address1               = "Queensway",
          address2               = None,
          address3               = Some("Birmingham"),
          address4               = None,
          postcode               = Some("B2 4ND"),
          phone                  = "01214567890",
          email                  = "info@harborviewestates.co.uk",
          dxAddress              = None,
          agentResourceReference = "ARN002"
        )
      )

      val orgResponse: SdltOrganisationResponse =
        SdltOrganisationResponse(
          storn                 = storn,
          version               = Some("1"),
          isReturnUser          = Some("Y"),
          doNotDisplayWelcomePage = Some("N"),
          agents                = agents
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