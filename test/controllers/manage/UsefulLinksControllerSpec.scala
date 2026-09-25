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

package controllers.manage

import base.SpecBase
import config.FrontendAppConfig
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.manage.UsefulLinksView

class UsefulLinksControllerSpec extends SpecBase {

  private val mockAppConfig = mock[FrontendAppConfig]

  "UsefulLinks Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder().build()
      when(mockAppConfig.sdltOnlineUrl).thenReturn("https://www.gov.uk/guidance/stamp-duty-land-tax-online-and-paper-returns")
      when(mockAppConfig.subleaseUrl).thenReturn("https://www.gov.uk/hmrc-internal-manuals/stamp-duty-land-tax-manual/sdltm62045")
      when(mockAppConfig.sdlt1Url).thenReturn("https://www.gov.uk/government/publications/sdlt-guide-for-completing-paper-sdlt1-return")
      when(mockAppConfig.sdlt4Url).thenReturn("https://www.gov.uk/government/publications/sdlt-guide-for-completing-form-sdlt4")
      when(mockAppConfig.sdltManualUrl).thenReturn("https://www.gov.uk/hmrc-internal-manuals/stamp-duty-land-tax-manual")
      when(mockAppConfig.ratesUrl).thenReturn("https://www.gov.uk/government/publications/rates-and-allowances-stamp-duty-land-tax")
      when(mockAppConfig.valuationUrl).thenReturn("https://www.gov.uk/government/organisations/valuation-office-agency")
      when(mockAppConfig.thirdPartyUrl).thenReturn("https://www.gov.uk/government/publications/stamp-duty-commercial-software-suppliers")

      running(application) {
        val request = FakeRequest(GET, controllers.manage.routes.UsefulLinksController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[UsefulLinksView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view()(request, messages(application), mockAppConfig).toString
      }
    }
  }
}
