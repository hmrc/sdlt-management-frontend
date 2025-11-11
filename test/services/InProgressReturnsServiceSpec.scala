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
import models.responses.{SdltInProgressReturnViewRow, UniversalStatus}
import org.mockito.ArgumentMatchers.eq as eqTo
import org.mockito.Mockito.{mock, times, verify, when}
import org.scalatest.EitherValues
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import uk.gov.hmrc.http.HeaderCarrier

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

  trait Fixture {
    val storn: String = "SN001"
    val id: String = "idToExtractAddress"
    val userId: String = "userId"
    val userAnswer = UserAnswers(userId)

    val connector: StampDutyLandTaxConnector = mock(classOf[StampDutyLandTaxConnector])
    val service: InProgressReturnsService = new InProgressReturnsService(connector)

    val instant: Instant = Instant.now.truncatedTo(ChronoUnit.MILLIS)
    val pageSize : Int = 9
  }

//  "Get all inProgressReturns records" should {
//    "return list of records on success" in new Fixture {
//
//      when(connector.getAll(eqTo(storn)))
//        .thenReturn(Future.successful(Right( List.empty )))
//
//      val result: Either[Throwable, List[SdltReturnViewRow]] = service.getAllReturns(storn).futureValue
//
//      result mustBe a[Either[Throwable, List[SdltReturnViewRow]]]
//
//      result mustBe Right(List.empty)
//
//      verify(connector, times(1)).getAll(eqTo(storn))
//    }
//
//    "return error on failure" in new Fixture  {
//      when(connector.getAll(eqTo(storn)))
//        .thenReturn(Future.successful(Left(new Error("Some error"))))
//
//      val result: Either[Throwable, List[SdltReturnViewRow]] = service.getAllReturns(storn).futureValue
//
//      result mustBe a[Either[Throwable, List[SdltReturnViewRow]]]
//
//      result.left.value.getMessage mustBe "Some error"
//
//    }
//  }
//
//  "slice TaxReturns records into paged data: empty input" in new Fixture {
//    val result: List[SdltReturnViewRow] = service.getPageRows( List.empty, 1, pageSize = 10)
//    result mustBe List.empty
//  }
//
//  "slice all TaxReturn into paged data: 17 records" in new Fixture {
//    val expectedDataPaginationOn: List[SdltReturnViewRow] = {
//      (0 to 17).toList.map(index =>
//        SdltReturnViewRow(
//          address = s"$index Riverside Drive",
//          agentReference = "B4C72F7T3",
//          dateSubmitted = LocalDate.parse("2025-04-05"),
//          utrn = "UTRN003",
//          purchaserName = "Brown",
//          status = UniversalStatus.ACCEPTED,
//          returnReference = "RETREF003",
//          returnId = s"RETID$index"
//        )
//      )
//    }
//
//    val pageOne: List[SdltReturnViewRow] = service.getPageRows(expectedDataPaginationOn, 1, pageSize = pageSize)
//    pageOne mustBe expectedDataPaginationOn.take(pageSize)
//
//    val pageTwo: List[SdltReturnViewRow] = service.getPageRows(expectedDataPaginationOn, 2, pageSize = pageSize)
//    pageTwo mustBe expectedDataPaginationOn.takeRight(expectedDataPaginationOn.length - pageSize)
//
//  }

}
