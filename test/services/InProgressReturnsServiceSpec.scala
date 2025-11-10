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

import connectors.InProgressReturnsConnector
import models.UserAnswers
import models.responses.SdltReturnInfoResponse
import org.mockito.ArgumentMatchers.eq as eqTo
import org.mockito.Mockito.{mock, times, verify, when}
import org.scalatest.EitherValues
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import uk.gov.hmrc.http.HeaderCarrier

import java.time.Instant
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

    val connector: InProgressReturnsConnector = mock(classOf[InProgressReturnsConnector])
    val service: InProgressReturnsService = new InProgressReturnsService(connector)

    val instant: Instant = Instant.now.truncatedTo(ChronoUnit.MILLIS)
  }

  "Get all inProgressReturns records" should {
    "return list of records on success" in new Fixture {

      when(connector.getAll(eqTo(storn)))
        .thenReturn(Future.successful(Right( List.empty )))

      val result: Either[Throwable, List[SdltReturnInfoResponse]] = service.getAllReturns(storn).futureValue

      result mustBe a[Either[Throwable, List[SdltReturnInfoResponse]]]

      result mustBe Right(List.empty)

      verify(connector, times(1)).getAll(eqTo(storn))
    }

    "return error on failure" in new Fixture  {
      when(connector.getAll(eqTo(storn)))
        .thenReturn(Future.successful(Left(new Error("Some error"))))

      val result: Either[Throwable, List[SdltReturnInfoResponse]] = service.getAllReturns(storn).futureValue

      result mustBe a[Either[Throwable, List[SdltReturnInfoResponse]]]

      result.left.value.getMessage mustBe "Some error"

    }
  }
}
