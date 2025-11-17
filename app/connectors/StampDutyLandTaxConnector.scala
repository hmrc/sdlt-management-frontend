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

import models.manage.SdltReturnRecordResponse
import models.manageAgents.AgentDetailsResponse
import models.responses.SdltOrganisationResponse
import play.api.Logging
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, StringContextOps}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.net.URL
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class StampDutyLandTaxConnector @Inject()(http: HttpClientV2,
                                          config: ServicesConfig)
                                         (implicit ec: ExecutionContext) extends Logging {

  private val base = config.baseUrl("stamp-duty-land-tax")

  // TODO: THIS LOGIC IMPLEMENTATION IS WRONG DUE TO INCORRECT DOCUMENTATION (wrong models) - THIS WILL BE FIXED IN THE NEXT SPRINT

  private val getAllReturnsUrl: String => URL = storn =>
    url"$base/stamp-duty-land-tax/manage-returns/get-returns?storn=$storn"

  private val getAllAgentDetailsUrl: String => URL = storn =>
    url"$base/stamp-duty-land-tax/manage-agents/agent-details/get-all-agents?storn=$storn"

  private val getSdltOrganisationUrl: String => URL = storn =>
    url"$base/stamp-duty-land-tax/manage-agents/get-sdlt-organisation?storn=$storn"

  def getAllReturns(storn: String)
                   (implicit hc: HeaderCarrier): Future[SdltReturnRecordResponse] =
    http
      .get(getAllReturnsUrl(storn))
      .execute[SdltReturnRecordResponse]
      .recover {
        case e: Throwable =>
          logger.error(s"[StampDutyLandTaxConnector][getAllReturns]: ${e.getMessage}")
          throw new RuntimeException(e.getMessage)
      }

  // TODO: REMOVE THIS DEPRECATED CALL
  def getAllAgentDetails(storn: String)
                        (implicit hc: HeaderCarrier): Future[List[AgentDetailsResponse]] =
    http
      .get(getAllAgentDetailsUrl(storn))
      .execute[List[AgentDetailsResponse]]
      .recover {
        case e: Throwable =>
          logger.error(s"[StampDutyLandTaxConnector][getAllAgentDetails]: ${e.getMessage}")
          throw new RuntimeException(e.getMessage)
      }

  def getSdltOrganisation(storn: String)
                         (implicit hc: HeaderCarrier): Future[SdltOrganisationResponse] =
    http
      .get(getSdltOrganisationUrl(storn))
      .execute[SdltOrganisationResponse]
      .recover {
        case e: Throwable =>
          logger.error(s"[StampDutyLandTaxConnector][getSdltOrganisation]: ${e.getMessage}")
          throw new RuntimeException(e.getMessage)
      }
}