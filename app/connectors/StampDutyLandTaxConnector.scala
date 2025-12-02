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

import models.manage.{SdltReturnRecordRequest, SdltReturnRecordResponse}
import models.organisation.SdltOrganisationResponse
import models.requests.DataRequest
import play.api.Logging
import play.api.libs.json.Json
import play.api.libs.json.OFormat.oFormatFromReadsAndOWrites
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, StringContextOps, UpstreamErrorResponse}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import scala.util.control.NonFatal
import java.net.URL
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class StampDutyLandTaxConnector @Inject()(http: HttpClientV2,
                                          config: ServicesConfig)
                                         (implicit ec: ExecutionContext) extends Logging {

  private val base = config.baseUrl("stamp-duty-land-tax")

  private val getSdltOrganisationUrl: String => URL = storn =>
    url"$base/stamp-duty-land-tax/manage-agents/get-sdlt-organisation?storn=$storn"

  private val getReturnsUrl: URL =
    url"$base/stamp-duty-land-tax/manage-returns/get-returns"

  def getSdltOrganisation(implicit hc: HeaderCarrier, request: DataRequest[_]): Future[SdltOrganisationResponse] =
    http
      .get(getSdltOrganisationUrl(request.storn))
      .execute[Either[UpstreamErrorResponse, SdltOrganisationResponse]]
      .flatMap {
        case Right(resp) => Future.successful(resp)
        case Left(error) => Future.failed(error)
      }
      .recoverWith {
        case NonFatal(e) =>
          logger.error(s"[StampDutyLandTaxConnector][getSdltOrganisation] failed for storn ${request.storn}: ${e.getMessage}", e)
          Future.failed(e)
      }

  def getReturns(status: Option[String], pageType: Option[String], deletionFlag: Boolean)
                (implicit hc: HeaderCarrier, request: DataRequest[_]): Future[SdltReturnRecordResponse] =
    http
      .post(getReturnsUrl)
      .withBody(Json.toJson(
        SdltReturnRecordRequest(
          storn = request.storn,
          status = status,
          deletionFlag = deletionFlag,
          pageType = pageType,
          pageNumber = None))
      )
      .execute[Either[UpstreamErrorResponse, SdltReturnRecordResponse]]
      .flatMap {
        case Right(resp) => Future.successful(resp)
        case Left(error) => Future.failed(error)
      }
      .recoverWith {
        case NonFatal(e) =>
          logger.error(s"[StampDutyLandTaxConnector][getReturns] failed for storn ${request.storn}: ${e.getMessage}", e)
          Future.failed(e)
      }
}
