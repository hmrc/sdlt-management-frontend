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
import models.UserAnswers
import models.manage.{ReturnSummary, SdltReturnRecordResponse}
import models.responses.UniversalStatus.{ACCEPTED, PENDING}
import models.responses.{SdltInProgressReturnViewRow, UniversalStatus}
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{mock, times, verify, when}
import org.scalatest.EitherValues
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import uk.gov.hmrc.http.HeaderCarrier
import utils.PaginationHelper

import java.time.{Instant, LocalDate}
import java.time.temporal.ChronoUnit
import scala.concurrent.{ExecutionContext, Future}

class InProgressReturnsServiceSpec extends AnyWordSpec
  with ScalaFutures
  with Matchers
  with EitherValues {

  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val messages: Messages = stubMessages()
  implicit val ex: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  trait Fixture extends PaginationHelper {
    val storn: String = "SN001"
    val id: String = "idToExtractAddress"
    val userId: String = "userId"
    val userAnswer = UserAnswers(userId)

    val connector: StampDutyLandTaxConnector = mock(classOf[StampDutyLandTaxConnector])
    val service: InProgressReturnsService = new InProgressReturnsService(connector)

    val instant: Instant = Instant.now.truncatedTo(ChronoUnit.MILLIS)
    val pageSize: Int = 10
  }

  "Get all inProgressReturns records" should {

    "return empty list on success :: response with empty list" in new Fixture {

      val emptyResponse = SdltReturnRecordResponse(
        storn = storn,
        returnSummaryCount = 0,
        returnSummaryList = List.empty
      )

      when(connector.getAllReturns(eqTo(storn))(any[HeaderCarrier]))
        .thenReturn(Future.successful(emptyResponse))

      val result: Either[Throwable, List[SdltInProgressReturnViewRow]] = service.getAllReturns(storn).futureValue
      result mustBe a[Either[Throwable, List[SdltInProgressReturnViewRow]]]
      result mustBe Right(List.empty)
      verify(connector, times(1)).getAllReturns(eqTo(storn))(any[HeaderCarrier])
    }

    "return list of recs on success :: response with data" in new Fixture {

      val response = SdltReturnRecordResponse(
        storn = storn,
        returnSummaryCount = 0,
        returnSummaryList = List(
          ReturnSummary(
            returnReference = "REF005",
            utrn = "UTRN005",
            status = "ACCEPTED",
            dateSubmitted = LocalDate.parse("2025-01-02"),
            purchaserName = "Name005",
            address = "Address005",
            agentReference = "AgentRef005"
          ),
          ReturnSummary(
            returnReference = "REF003",
            utrn = "UTRN003",
            status = "PENDING",
            dateSubmitted = LocalDate.parse("2025-01-02"),
            purchaserName = "Name003",
            address = "Address003",
            agentReference = "AgentRef003"
          )
        )
      )
      val expected = List(
        SdltInProgressReturnViewRow("Address005",
          "AgentRef005",
          LocalDate.parse("2025-01-02"),
          "UTRN005",
          "Name005",
          ACCEPTED,
          "REF005"),
        SdltInProgressReturnViewRow("Address003", "AgentRef003",
          LocalDate.parse("2025-01-02"),
          "UTRN003", "Name003", PENDING, "REF003")
      )


      when(connector.getAllReturns(eqTo(storn))(any[HeaderCarrier]))
        .thenReturn(Future.successful(response))

      val result: Either[Throwable, List[SdltInProgressReturnViewRow]] = service.getAllReturns(storn).futureValue
      result mustBe a[Either[Throwable, List[SdltInProgressReturnViewRow]]]
      result mustBe Right(expected)
      verify(connector, times(1)).getAllReturns(eqTo(storn))(any[HeaderCarrier])
    }

    "return error on failure" in new Fixture {
      when(connector.getAllReturns(eqTo(storn))(any[HeaderCarrier]))
        .thenThrow(new Error("Some error"))

      val error: Error = intercept[Error] {
        service.getAllReturns(storn).futureValue
      }

      error.getMessage mustBe "Some error"
    }
  }

  // TODO: move these tests under PaginationHelper / drop extension in the Fixture as well
  "slice TaxReturns records into paged data: empty input" in new Fixture {
    val result: List[SdltInProgressReturnViewRow] = getSelectedPageRows(List.empty, 1)
    result mustBe List.empty
  }

  "slice all TaxReturn into paged data" in new Fixture {
    val expectedDataPaginationOn: List[SdltInProgressReturnViewRow] = {
      (0 to 17).toList.map(index =>
        SdltInProgressReturnViewRow(
          address = s"$index Riverside Drive",
          agentReference = "B4C72F7T3",
          dateSubmitted = LocalDate.parse("2025-04-05"),
          utrn = "UTRN003",
          purchaserName = "Brown",
          status = UniversalStatus.ACCEPTED,
          returnReference = "RETREF003",
        )
      )
    }

    val pageOne: List[SdltInProgressReturnViewRow] = getSelectedPageRows(expectedDataPaginationOn, 1)
    pageOne mustBe expectedDataPaginationOn.take(pageSize)

    val pageTwo: List[SdltInProgressReturnViewRow] = getSelectedPageRows(expectedDataPaginationOn, 2)
    pageTwo mustBe expectedDataPaginationOn.takeRight(expectedDataPaginationOn.length - pageSize)

  }

}
