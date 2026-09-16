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

package config

import com.google.inject.{Inject, Singleton}
import play.api.Configuration
import play.api.mvc.RequestHeader

@Singleton
class FrontendAppConfig @Inject() (configuration: Configuration) {

  val host: String    = configuration.get[String]("host")
  val appName: String = configuration.get[String]("appName")

  private val contactHost = configuration.get[String]("contact-frontend.host")
  private val addTaxesHost = configuration.get[String]("add-taxes-frontend.host")
  private val businessTaxAccount = configuration.get[String]("business-tax-account.host")
  private val contactFormServiceIdentifier = "sdlt-management-frontend"

  def userResearchBannerEnabled: Boolean =
    configuration.get[Boolean]("features.user-research-banner")

  def userResearchBannerUrl: String =
    configuration.get[String]("urls.user-research-banner")

  def feedbackUrl(implicit request: RequestHeader): String =
    s"$contactHost/contact/beta-feedback?service=$contactFormServiceIdentifier&backUrl=${host + request.uri}"

  lazy val feedbackFrontend: String = configuration.get[String]("feedback-frontend.host")

  val loginUrl: String                      = configuration.get[String]("urls.login")
  val loginContinueUrl: String              = configuration.get[String]("urls.loginContinue")
  val signOutUrl: String                    = configuration.get[String]("urls.signOut")
  val govUkSDLTGuidanceUrl: String          = configuration.get[String]("urls.govUkSDLTGuidance")
  lazy val govUKUrl: String                 = configuration.get[String]("urls.govUK")
  lazy val howToPayUrl: String              = configuration.get[String]("urls.howToPay")
  lazy val contactHmrcUrl: String           = configuration.get[String]("urls.contactHmrc")
  lazy val hmrcOnlineServiceDeskUrl: String = configuration.get[String]("urls.hmrcOnlineServiceDesk")
  lazy val hmrcOnlineHelpDesk: String       = configuration.get[String]("urls.hmrcOnlineHelpDesk")
  lazy val hmrcTaxServiceBusinessAccount: String       = s"$businessTaxAccount/business-account"
  lazy val hmrcTaxServiceAddSdlt: String       = s"$addTaxesHost/business-account/add-tax/other/land/stamp-duty"
  
  lazy val sdltOnlineUrl: String  = configuration.get[String]("urls.sdltOnline")
  lazy val subleaseUrl: String    = configuration.get[String]("urls.sublease")
  lazy val sdlt1Url: String       = configuration.get[String]("urls.sdlt1")
  lazy val sdlt4Url: String       = configuration.get[String]("urls.sdlt4")
  lazy val sdltManualUrl: String  = configuration.get[String]("urls.sdltManual")
  lazy val ratesUrl: String       = configuration.get[String]("urls.rates")
  lazy val valuationUrl: String   = configuration.get[String]("urls.valuation")
  lazy val thirdPartyUrl: String  = configuration.get[String]("urls.thirdParty")

  private val agentServiceBaseUrl: String       = configuration.get[String]("stamp-duty-land-tax-agent.host")
  val startAddAgentUrl: String                  = s"$agentServiceBaseUrl/stamp-duty-land-tax-agent/manage-agents/start-add-agent"
  val agentOverviewUrl: String                  = s"$agentServiceBaseUrl/stamp-duty-land-tax-agent"

  private val filingServiceBaseUrl: String = configuration.get[String]("stamp-duty-land-tax-filing.host")
  val startNewReturnUrl: String            = s"$filingServiceBaseUrl/stamp-duty-land-tax-filing"
  
  def returnTaskListUrl(returnReference:String):String = s"$filingServiceBaseUrl/stamp-duty-land-tax-filing/task-list?returnId=$returnReference"
  
  val exitSurveyUrl: String             = s"$feedbackFrontend/feedback/stamp-duty-land-tax"

  val timeout: Int   = configuration.get[Int]("timeout-dialog.timeout")
  val countdown: Int = configuration.get[Int]("timeout-dialog.countdown")

  val cacheTtl: Long = configuration.get[Int]("mongodb.timeToLiveInSeconds")
}

