package controllers.manage

import base.SpecBase
import config.FrontendAppConfig
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.StampDutyLandTaxService
import views.html.manage.AtAGlanceView
import play.api.inject.bind
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.Application
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class AtAGlanceControllerSpec extends SpecBase with MockitoSugar {

  trait Fixture {

    val mockService: StampDutyLandTaxService = mock[StampDutyLandTaxService]

    val application: Application =
      applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[StampDutyLandTaxService].toInstance(mockService))
        .build()

    val atAGlanceUrl: String = controllers.manage.routes.AtAGlanceController.onPageLoad().url
    
  }

  "At A Glance Controller" - {

    "must return OK and the correct view for a GET" in new Fixture {

      when(mockService.getAllAgents(any[String])(any[HeaderCarrier]))
        .thenReturn(Future.successful(Right(Nil)))

      when(mockService.getReturn(any[String], any[String])(any[HeaderCarrier]))
        .thenReturn(Future.successful(Right(Nil)))

      running(application) {
        implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

        val request = FakeRequest(GET, atAGlanceUrl)
        val result = route(application, request).value

        val view = application.injector.instanceOf[AtAGlanceView]
        val expected = view(
          storn = "STN001",
          numAgents = 0,
          numInProgress = 0,
          numSubmitted = 0,
          numDueForDeletion = 0,
          inProgressUrl = "",
          submittedUrl = "",
          dueForDeletionUrl = "",
          feedbackUrl = appConfig.feedbackUrl(request)
        )(request, messages(application)).toString

        status(result) mustEqual OK
        contentAsString(result) mustEqual expected
      }
    }
  }
}
