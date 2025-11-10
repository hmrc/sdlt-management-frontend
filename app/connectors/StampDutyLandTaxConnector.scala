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

package connectors

import models.manageReturns.ReturnsResponse
import play.api.Logging
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, StringContextOps}
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.net.URL
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class StampDutyLandTaxConnector @Inject()(http: HttpClientV2,
                                          config: ServicesConfig)
                                         (implicit ec: ExecutionContext) extends Logging {

  private val base = config.baseUrl("stamp-duty-land-tax")

  private val getReturnsUrl: String => URL = storn =>
    url"$base/stamp-duty-land-tax/manage-returns/get-returns?storn=$storn"


  def getReturns(storn: String)
                        (implicit hc: HeaderCarrier): Future[List[ReturnsResponse]] =
    http
      .get(getReturnsUrl(storn))
      .execute[List[ReturnsResponse]]
      .recover {
        case e: Throwable =>
          logger.error(s"[getReturns]: ${e.getMessage}")
          throw new RuntimeException(e.getMessage)
      }
}

