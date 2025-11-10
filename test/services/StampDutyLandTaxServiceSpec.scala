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
import models.manageReturns.{ReturnsResponse, ReturnsSummaryList}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.mockito.Mockito.*
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class StampDutyLandTaxServiceSpec extends AnyWordSpec with ScalaFutures with Matchers {

  implicit val hc: HeaderCarrier = HeaderCarrier()


  private def newService(): (StampDutyLandTaxService, StampDutyLandTaxConnector) = {
    val connector = mock(classOf[StampDutyLandTaxConnector])
    val service = new StampDutyLandTaxService(connector)
    (service, connector)
  }

  private val storn = "STN001"
  private val utrn = "123456789MC"

  "getReturns" should {
    "invoke the connector with given storn and fetch the in progress returns successfully" in {

      val (service, connector) = newService()

      val returns = Some(ReturnsResponse(
        returnSummaryCount = 2,
        returnSummaryList = List(ReturnsSummaryList(
          returnReference = "returnReference1",
          utrn = "utrn1",
          status = "status1",
          dateSubmitted = "dateSubmitted1",
          purchaserName = "purchaserName1",
          address = "address1",
          agentReference = "agentReference1"
        ),
          ReturnsSummaryList(
          returnReference = "returnReference2",
          utrn = "utrn2",
          status = "status2",
          dateSubmitted = "dateSubmitted2",
          purchaserName = "purchaserName2",
          address = "address2",
          agentReference = "agentReference2"
        )
      )))

      when(connector.getReturns(eqTo(storn))(any[HeaderCarrier]))
        .thenReturn(Future.successful(returns))

      val result = service.getReturns(storn).futureValue
      result mustBe returns

      verify(connector).getReturns(eqTo(storn))(any[HeaderCarrier])
      verifyNoMoreInteractions(connector)
    }

    "retrieve error messages and failures from the connector" in {

      val (service, connector) = newService()

      when(connector.getReturns(eqTo(storn))(any[HeaderCarrier]))
        .thenReturn(Future.failed(new RuntimeException("Error")))

      val ex = intercept[RuntimeException] {
        service.getReturns(storn).futureValue
      }
      ex.getMessage must include("Error")
    }
  }
}

