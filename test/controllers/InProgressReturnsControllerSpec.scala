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
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.Application
import services.InProgressReturnsService
import views.html.InProgressReturnView
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*

class InProgressReturnsControllerSpec extends SpecBase {

  trait Fixture {
    val service: InProgressReturnsService = mock[InProgressReturnsService]

    val app: Application =
      applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[InProgressReturnsService].toInstance(service))
        .build()
  }

  "InProgress Returns Controller " - {
    "return OK for GET" in new Fixture {

      running(app) {
        val request = FakeRequest(GET, manage.routes.InProgressReturnsController.onPageLoad().url)

        val result = route(app, request).value

        val _ = app.injector.instanceOf[InProgressReturnView]
        //val list = SummaryListViewModel(Seq.empty)

        status(result) mustEqual OK
//        contentAsString(result) mustEqual view(list)(request, messages(application)).toString
      }
    }

  }


}
