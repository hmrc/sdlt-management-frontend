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

package controllers

import base.SpecBase
import config.FrontendAppConfig
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.Status.SEE_OTHER
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET, defaultAwaitTimeout, redirectLocation, route, running, status, writeableOf_AnyContentAsEmpty}

class FeedBackControllerSpec extends SpecBase with MockitoSugar{

  "FeedBackController" -{
    "gotToSurvey" - {
      s"must redirect to the Feedback page in a new tab " in {
        val testExitSurveyUrl = "https://sdlt-management-frontend"
        
        val mockFrontendAppConfig = mock[FrontendAppConfig]
        
        when(mockFrontendAppConfig.exitSurveyUrl).thenReturn(testExitSurveyUrl)
        
        val application = applicationBuilder()
          .overrides(
            bind[FrontendAppConfig].toInstance(mockFrontendAppConfig)
          )
          .build()
        
        running(application) {
          val request = FakeRequest(GET, routes.FeedBackController.redirectToFeedback().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual testExitSurveyUrl


        }
      }
    }
  }

}
