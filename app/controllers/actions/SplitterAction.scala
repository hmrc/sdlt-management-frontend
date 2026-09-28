/*
 * Copyright 2026 HM Revenue & Customs
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

package controllers.actions

import play.api.mvc.*
import connectors.RateLimitedAllowListConnector
import config.FrontendAppConfig
import play.api.Logging
import uk.gov.hmrc.http.HeaderCarrier
import play.api.mvc.Results.Redirect
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import models.requests.IdentifierRequest
import uk.gov.hmrc.auth.core.AffinityGroup.*

import scala.concurrent.{ExecutionContext, Future}
import javax.inject.{Inject, Singleton}

@Singleton
class SplitterAction @Inject() (
  appConfig: FrontendAppConfig,
  rateLimitedAllowListConnector: RateLimitedAllowListConnector
)(implicit override val executionContext: ExecutionContext)
    extends Logging
    with ActionRefiner[IdentifierRequest, IdentifierRequest] {

  override protected def refine[A](request: IdentifierRequest[A]): Future[Either[Result, IdentifierRequest[A]]] = {
    given HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
    for {
      isAllowed <-
        if appConfig.useRateLimitedAllowList
        then rateLimitedAllowListConnector.checkAllowList(appConfig.splitterAllowListName, request.storn)
        else Future.successful(true)
    } yield
      if isAllowed
      then Right(request)
      else {
        val userTypeText = request.affinityGroup match {
          case Agent => "agent"
          case _     => "org"
        }

        val url = s"${appConfig.legacySdltServiceUrl}/$userTypeText/${request.storn}"
        logger.info(s"Redirecting to sdlt legacy service to $url")

        Left(Redirect(url))
      }
  }
}
