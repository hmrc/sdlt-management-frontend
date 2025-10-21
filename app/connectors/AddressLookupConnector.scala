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

import config.FrontendAppConfig
import play.api.i18n.MessagesApi
import uk.gov.hmrc.http.client.HttpClientV2
import play.api.libs.json._
import javax.inject.Inject
import scala.concurrent.ExecutionContext

class AddressLookupConnector @Inject()(val appConfig: FrontendAppConfig,
                                       http: HttpClientV2,
                                       val messagesApi: MessagesApi)(implicit ec: ExecutionContext) {

  private def getAddressJson: JsValue = {
    JsObject(
      Seq(
        "version" -> JsNumber(2),
        "options" -> JsObject(
          Seq(
            "continueUrl" -> JsString("..."),
            "homeNavHref" -> JsString("..."),
            "signOutHref" -> JsString("..."),

            "accessibilityFooterUrl" -> JsString("..."),
            "phaseFeedbackLink" -> JsString("/help/alpha"),
            "deskProServiceName" -> JsString("..."),
            "showPhaseBanner" -> JsBoolean(false),
            "alphaPhase" -> JsBoolean(false),
            "disableTranslations" -> JsBoolean(true),
            "showBackButtons" -> JsBoolean(false),
            "includeHMRCBranding" -> JsBoolean(true),

            "allowedCountryCodes" -> JsArray(Seq(JsString("GB"), JsString("FR"))),

            "ukMode" -> JsBoolean(true),

            "selectPageConfig" -> JsObject(
              Seq(
                "proposalListLimit" -> JsNumber(30),
                "showSearchLinkAgain" -> JsBoolean(true)
              )
            ),

            "confirmPageConfig" -> JsObject(
              Seq(
                "showChangeLink" -> JsBoolean(false),
                "showSubHeadingAndInfo" -> JsBoolean(false),
                "showSearchAgainLink" -> JsBoolean(false),
                "showConfirmChangeText" -> JsBoolean(false),
              )
            ),

            "manualAddressEntryConfig" -> JsObject (
              Seq(
                "line1MaxLength" -> JsNumber(255),
                "line2MaxLength" -> JsNumber(255),
                "line3MaxLength" -> JsNumber(255),
                "townMaxLength" -> JsNumber(255)
              )
            ),

          )
        )
      )
    )
  }

  def init: Unit = ???


}