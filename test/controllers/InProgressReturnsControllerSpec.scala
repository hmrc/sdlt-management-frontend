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
import models.responses.{SdltReturnInfoResponse, UniversalStatus}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.Application
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.InProgressReturnsService

import java.time.LocalDate
import scala.concurrent.Future

class InProgressReturnsControllerSpec extends SpecBase with MockitoSugar {

  trait Fixture {
    val mockService: InProgressReturnsService = mock[InProgressReturnsService]
    val mockSessionRepository: SessionRepository = mock[SessionRepository]

    val application: Application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
      .overrides(bind[InProgressReturnsService].toInstance(mockService))
      .build()

    val expectedData = List[SdltReturnInfoResponse](
      SdltReturnInfoResponse(
        address = "13: 29 Acacia Road",
        agentReference = "B4N4NM4N",
        dateSubmitted = LocalDate.parse("2025-01-01"),
        utrn = "UTRN001",
        purchaserName = "Wimp",
        status = UniversalStatus.ACCEPTED,
        returnReference = "RETREF001",
        returnId = "RETID001"
      )
    )
  }

  "InProgress Returns Controller " - {

    "return OK for GET" in new Fixture {
      when(mockSessionRepository.set(any()))
        .thenReturn(Future.successful(true))

      when(mockService.getAllReturns(any()))
        .thenReturn( Future.successful(Right(expectedData)) )

      when(mockService.getRowForPageSelected(any(), any(), any()))
        .thenReturn(expectedData)

      running(application) {

        val request = FakeRequest(GET, manage.routes.InProgressReturnsController.onPageLoad(None).url)

        val result = route(application, request).value

        //redirectLocation(result).value mustEqual "/stamp-duty-land-tax-management/manage/unauthorised/organisation"

        status(result) mustEqual OK
//        contentAsString(result) mustEqual view(list)(request, messages(application)).toString
      }
    }

  }


}
