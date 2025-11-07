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
import models.manageReturns.InProgressReturnsResponse
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.mockito.Mockito.*
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.Future

class StampDutyLandTaxServiceSpec extends AnyWordSpec with ScalaFutures with Matchers {

  implicit val hc: HeaderCarrier = HeaderCarrier()


  private def newService(): (StampDutyLandTaxService, StampDutyLandTaxConnector) = {
    val connector = mock(classOf[StampDutyLandTaxConnector])
    val service = new StampDutyLandTaxService(connector)
    (service, connector)
  }

  private val storn = "STN001"
  private val utrn = "123456789MC"

  "getInProgressReturns" should {
    "invoke the connector with given storn and fetch the in progress returns successfully" in {

      val (service, connector) = newService()

      val progressReturns = Some(InProgressReturnsResponse(
        utrn = utrn,
        one = "one",
        two = 2
      ))

      when(connector.getInProgressReturns(eqTo(storn), eqTo(utrn))(any[HeaderCarrier]))
        .thenReturn(Future.successful(progressReturns))

      val result = service.getInProgressReturns(storn, utrn).futureValue
      result mustBe progressReturns

      verify(connector).getInProgressReturns(eqTo(storn), eqTo(utrn))(any[HeaderCarrier])
      verifyNoMoreInteractions(connector)
    }

    "retrieve error messages and failures from the connector" in {

      val (service, connector) = newService()

      when(connector.getInProgressReturns(eqTo(storn), eqTo(utrn))(any[HeaderCarrier]))
        .thenReturn(Future.failed(new RuntimeException("Error")))

      val ex = intercept[RuntimeException] {
        service.getInProgressReturns(storn, utrn).futureValue
      }

        ex.getMessage must include("Error")
    }
  }
}
