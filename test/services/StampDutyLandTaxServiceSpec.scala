package services

import connectors.StampDutyLandTaxConnector
import models.manage.{ReturnSummary, SdltReturnRecordResponse}
import models.manageAgents.AgentDetailsResponse
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

  "getAllReturns" should {
    "delegate to connector with the given storn and return the payload" in {
      val (service, connector) = newService()

      when(connector.getAllReturns(eqTo(storn))(any[HeaderCarrier]))
        .thenReturn(Future.successful(Some(aggregateResponse)))

      val result = service.getAllReturns(storn).futureValue
      result mustBe Some(aggregateResponse)

      verify(connector).getAllReturns(eqTo(storn))(any[HeaderCarrier])
      verifyNoMoreInteractions(connector)
    }

    "propagate failures from the connector" in {
      val (service, connector) = newService()

      when(connector.getAllReturns(eqTo(storn))(any[HeaderCarrier]))
        .thenReturn(Future.failed(new RuntimeException("boom")))

      val ex = intercept[RuntimeException] {
        service.getAllReturns(storn).futureValue
      }
      ex.getMessage must include ("boom")
    }
  }

  "getAllPendingReturns" should {
    "return only PENDING returns when BE returns Some(response)" in {
      val (service, connector) = newService()

      when(connector.getAllReturns(eqTo(storn))(any[HeaderCarrier]))
        .thenReturn(Future.successful(Some(aggregateResponse)))

      val result = service.getAllPendingReturns(storn).futureValue
      result.map(_.status).distinct mustBe List("PENDING")
      result.map(_.returnReference) mustBe List("RET-001")

      verify(connector).getAllReturns(eqTo(storn))(any[HeaderCarrier])
      verifyNoMoreInteractions(connector)
    }

    "fail when BE returns None (collect not defined)" in {
      val (service, connector) = newService()

      when(connector.getAllReturns(eqTo(storn))(any[HeaderCarrier]))
        .thenReturn(Future.successful(None))

      val ex = service.getAllPendingReturns(storn).failed.futureValue
      ex.getMessage must include("partial function is not defined at: None")
    }
  }

  "getAllSubmittedReturns" should {
    "return only SUBMITTED returns when BE returns Some(response)" in {
      val (service, connector) = newService()

      when(connector.getAllReturns(eqTo(storn))(any[HeaderCarrier]))
        .thenReturn(Future.successful(Some(aggregateResponse)))

      val result = service.getAllSubmittedReturns(storn).futureValue
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
        service.getAllSubmittedReturns(storn).futureValue
      }
      ex.getMessage must include ("boom")
    }
  }

  "getAllAcceptedReturns" should {
    "return only ACCEPTED returns" in {
      val (service, connector) = newService()

      when(connector.getAllReturns(eqTo(storn))(any[HeaderCarrier]))
        .thenReturn(Future.successful(Some(aggregateResponse)))

      val result = service.getAllAcceptedReturns(storn).futureValue
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
        .thenReturn(Future.successful(Some(aggregateResponse)))

      val result = service.getAllStartedReturns(storn).futureValue
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
        .thenReturn(Future.successful(Some(aggregateResponse)))

      val result = service.getAllInProgressReturns(storn).futureValue
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
        .thenReturn(Future.successful(Some(aggregateResponse)))

      val result = service.getReturnsDueForDeletion(storn).futureValue
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
          houseNumber          = "42",
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
          houseNumber          = "22A",
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
}