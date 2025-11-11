package controllers.manage

import base.SpecBase
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.StampDutyLandTaxService
import views.html.manage.AtAGlanceView
import play.api.inject.bind
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class AtAGlanceControllerSpec extends SpecBase with MockitoSugar {

  private val mockService = mock[StampDutyLandTaxService]

  private val atAGlanceUrl = "/stamp-duty-land-tax-management"

    // controllers.manage.routes.AtAGlanceController.onPageLoad().url

  "At A Glance Controller" - {

    "must return OK and the correct view for a GET" in {

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[StampDutyLandTaxService].toInstance(mockService))
          .build()

      when(mockService.getAllAgents(any[String])(any[HeaderCarrier]))
        .thenReturn(Future.successful(Right(List.empty)))

      when(mockService.getReturn(any[String], any[String])(any[HeaderCarrier]))
        .thenReturn(Future.successful(Right(List.empty)))

      running(application) {
        val request = FakeRequest(GET, atAGlanceUrl)
        println(request)
        println("^^^^^^^^^^^^^^^^^^^ REQUEST")
        val result = route(application, request).value

        val view = application.injector.instanceOf[AtAGlanceView]
        val expected = view(
          storn = "STN001",
          agentMsg = "Manage agents",
          inProgressMsg = "Returns in progress",
          submittedMsg = "Submitted returns",
          dueForDeletionMsg = "Returns due for deletion",
          feedbackUrl = ""
        )(request, messages(application))

        status(result) mustEqual OK
        contentAsString(result) mustEqual expected.toString
      }
    }
  }
}
