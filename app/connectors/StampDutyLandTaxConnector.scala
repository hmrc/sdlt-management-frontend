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

import models.manageReturns.{DBSubmittedReturnsResponse, DueDeletionReturnsResponse, InProgressReturnsResponse, SubmittedReturnsResponse}
import models.requests.ReturnsRequest
import play.api.Logging
import play.api.libs.json.Json
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, StringContextOps}
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue
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

  private val getInProgressReturnsUrl: (String, String) => URL = (storn, utrn) =>
    url"$base/stamp-duty-land-tax/manage-returns/in-progress-returns/get?storn=$storn&utrn=$utrn"

  private val getAllInProgressReturnsUrl: String => URL = storn =>
    url"$base/stamp-duty-land-tax/manage-returns/in-progress-returns/get?storn=$storn"


  private val getSubmittedReturnsUrl: (String, String) => URL = (storn, utrn) =>
    url"$base/stamp-duty-land-tax/manage-returns/submitted-returns/get?storn=$storn&utrn=$utrn"

  private val getAllSubmittedReturnsUrl: String => URL = storn =>
    url"$base/stamp-duty-land-tax/manage-returns/submitted-returns/get?storn=$storn"


  private val getDueDeletionReturnsUrl: (String, String) => URL = (storn, utrn) =>
    url"$base/stamp-duty-land-tax/manage-returns/due-deletion-returns/get?storn=$storn&utrn=$utrn"

  private val getAllDueDeletionReturnsUrl: String => URL = storn =>
    url"$base/stamp-duty-land-tax/manage-returns/due-deletion-returns/get?storn=$storn"


  private val startReturnUrl: URL =
    url"$base/stamp-duty-land-tax/manage-returns/start-return/submit"

  private val deleteReturnUrl: (String, String) => URL = (storn, utrn) =>
    url"$base/stamp-duty-land-tax/manage-returns/delete-return/delete?storn=$storn&utrn=$utrn"



  def getInProgressReturns(storn: String, utrn: String)
                     (implicit hc: HeaderCarrier): Future[Option[InProgressReturnsResponse]] =
    http
      .get(getInProgressReturnsUrl(storn, utrn))
      .execute[Option[InProgressReturnsResponse]]
      .recover {
        case e: Throwable =>
          logger.error(s"[getInProgressReturns]: ${e.getMessage}")
          throw new RuntimeException(e.getMessage)
      }


  def getAllInProgressReturns(storn: String)
                        (implicit hc: HeaderCarrier): Future[List[InProgressReturnsResponse]] =
    http
      .get(getAllInProgressReturnsUrl(storn))
      .execute[List[InProgressReturnsResponse]]
      .recover {
        case e: Throwable =>
          logger.error(s"[getAllInProgressReturns]: ${e.getMessage}")
          throw new RuntimeException(e.getMessage)
      }



  def getSubmittedReturns(storn: String, utrn: String)
                          (implicit hc: HeaderCarrier): Future[Option[SubmittedReturnsResponse]] =
    http
      .get(getSubmittedReturnsUrl(storn, utrn))
      .execute[Option[SubmittedReturnsResponse]]
      .recover {
        case e: Throwable =>
          logger.error(s"[getSubmittedReturns]: ${e.getMessage}")
          throw new RuntimeException(e.getMessage)
      }


  def getAllSubmittedReturns(storn: String)
                             (implicit hc: HeaderCarrier): Future[List[SubmittedReturnsResponse]] =
    http
      .get(getAllSubmittedReturnsUrl(storn))
      .execute[List[SubmittedReturnsResponse]]
      .recover {
        case e: Throwable =>
          logger.error(s"[getAllSubmittedReturns]: ${e.getMessage}")
          throw new RuntimeException(e.getMessage)
      }


  
  def getDueDeletionReturns(storn: String, utrn: String)
                         (implicit hc: HeaderCarrier): Future[Option[DueDeletionReturnsResponse]] =
    http
      .get(getDueDeletionReturnsUrl(storn, utrn))
      .execute[Option[DueDeletionReturnsResponse]]
      .recover {
        case e: Throwable =>
          logger.error(s"[getDueDeletionReturns]: ${e.getMessage}")
          throw new RuntimeException(e.getMessage)
      }


  def getAllDueDeletionReturns(storn: String)
                            (implicit hc: HeaderCarrier): Future[List[DueDeletionReturnsResponse]] =
    http
      .get(getAllDueDeletionReturnsUrl(storn))
      .execute[List[DueDeletionReturnsResponse]]
      .recover {
        case e: Throwable =>
          logger.error(s"[getAllDueDeletionReturns]: ${e.getMessage}")
          throw new RuntimeException(e.getMessage)
      }
  
  

  def startReturn(returnsRequest: ReturnsRequest)
                        (implicit hc: HeaderCarrier): Future[DBSubmittedReturnsResponse] =
    http
      .post(startReturnUrl)
      .withBody(Json.toJson(returnsRequest))
      .execute[DBSubmittedReturnsResponse]
      .recover {
        case e: Throwable =>
          logger.error(s"[startReturn]: ${e.getMessage}")
          throw new RuntimeException(e.getMessage)
      }

  def deleteReturn(storn: String, utrn: String)
                        (implicit hc: HeaderCarrier): Future[Boolean] =
    http
      .get(deleteReturnUrl(storn, utrn))
      .execute[Boolean]
      .recover {
        case e: Throwable =>
          logger.error(s"[deleteReturn]: ${e.getMessage}")
          throw new RuntimeException(e.getMessage)
      }
}

