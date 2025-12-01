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

package controllers.manage

import base.SpecBase
import models.manage.ReturnSummary
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import play.api.Application
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.StampDutyLandTaxService
import viewmodels.manage.deletedReturns.{
  PaginatedDeletedInProgressReturnsViewModel,
  PaginatedDeletedSubmittedReturnsViewModel
}
import views.html.manage.DueForDeletionReturnsView

import scala.concurrent.Future

class DueForDeletionReturnsControllerSpec
  extends SpecBase
    with MockitoSugar {

  private val mockService = mock[StampDutyLandTaxService]

  private def application: Application =
    applicationBuilder(userAnswers = Some(emptyUserAnswers))
      .overrides(
        bind[StampDutyLandTaxService].toInstance(mockService)
      )
      .build()

  private val baseRoute =
    controllers.manage.routes.DueForDeletionReturnsController.onPageLoad(None, None).url

  "DueForDeletionReturnsController.onPageLoad" - {

    "must return OK and render the correct view when both services return empty lists and no indices are provided" in {
      reset(mockService)

      when(mockService.getSubmittedReturnsDueForDeletion(any(), any()))
        .thenReturn(Future.successful(List.empty[ReturnSummary]))

      when(mockService.getInProgressReturnsDueForDeletion(any(), any()))
        .thenReturn(Future.successful(List.empty[ReturnSummary]))

      val app = application

      running(app) {
        val request = FakeRequest(GET, baseRoute)

        val result = route(app, request).value

        status(result) mustEqual OK

        val view = app.injector.instanceOf[DueForDeletionReturnsView]

        val expectedInProgress =
          PaginatedDeletedInProgressReturnsViewModel(Nil, None, None)
        val expectedSubmitted =
          PaginatedDeletedSubmittedReturnsViewModel(Nil, None, None)

        contentAsString(result) mustEqual
          view(expectedInProgress, expectedSubmitted)(request, messages(app)).toString

        verify(mockService, times(1)).getSubmittedReturnsDueForDeletion(any(), any())
        verify(mockService, times(1)).getInProgressReturnsDueForDeletion(any(), any())
      }
    }

    "must return OK and render the correct view when indices are provided but lists are empty" in {
      reset(mockService)

      when(mockService.getSubmittedReturnsDueForDeletion(any(), any()))
        .thenReturn(Future.successful(List.empty[ReturnSummary]))

      when(mockService.getInProgressReturnsDueForDeletion(any(), any()))
        .thenReturn(Future.successful(List.empty[ReturnSummary]))

      val inProgressIndex  = Some(3)
      val submittedIndex   = Some(2)

      val app = application

      running(app) {
        val request = FakeRequest(
          GET,
          controllers.manage.routes.DueForDeletionReturnsController
            .onPageLoad(inProgressIndex, submittedIndex)
            .url
        )

        val result = route(app, request).value

        status(result) mustEqual OK

        val view = app.injector.instanceOf[DueForDeletionReturnsView]

        val expectedInProgress =
          PaginatedDeletedInProgressReturnsViewModel(Nil, None, None)
        val expectedSubmitted =
          PaginatedDeletedSubmittedReturnsViewModel(Nil, None, None)

        contentAsString(result) mustEqual
          view(expectedInProgress, expectedSubmitted)(request, messages(app)).toString

        verify(mockService, times(1)).getSubmittedReturnsDueForDeletion(any(), any())
        verify(mockService, times(1)).getInProgressReturnsDueForDeletion(any(), any())
      }
    }

    "must redirect to JourneyRecoveryController when getSubmittedReturnsDueForDeletion fails" in {
      reset(mockService)

      when(mockService.getSubmittedReturnsDueForDeletion(any(), any()))
        .thenReturn(Future.failed(new RuntimeException("boom-submitted")))

      when(mockService.getInProgressReturnsDueForDeletion(any(), any()))
        .thenReturn(Future.successful(List.empty[ReturnSummary]))

      val app = application

      running(app) {
        val request = FakeRequest(GET, baseRoute)

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.routes.JourneyRecoveryController.onPageLoad().url

        verify(mockService, times(1)).getSubmittedReturnsDueForDeletion(any(), any())
        verify(mockService, times(0)).getInProgressReturnsDueForDeletion(any(), any())
      }
    }

    "must redirect to JourneyRecoveryController when getInProgressReturnsDueForDeletion fails" in {
      reset(mockService)

      when(mockService.getSubmittedReturnsDueForDeletion(any(), any()))
        .thenReturn(Future.successful(List.empty[ReturnSummary]))

      when(mockService.getInProgressReturnsDueForDeletion(any(), any()))
        .thenReturn(Future.failed(new RuntimeException("boom-in-progress")))

      val app = application

      running(app) {
        val request = FakeRequest(GET, baseRoute)

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.routes.JourneyRecoveryController.onPageLoad().url

        verify(mockService, times(1)).getSubmittedReturnsDueForDeletion(any(), any())
        verify(mockService, times(1)).getInProgressReturnsDueForDeletion(any(), any())
      }
    }
  }
}
