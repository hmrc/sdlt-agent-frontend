package controllers

import base.SpecBase
import config.FrontendAppConfig
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.http.HttpVerbs.GET
import views.html.UnauthorisedIndividualView

class UnauthorisedIndividualAffinityControllerSpec extends SpecBase with MockitoSugar {


  "UnauthorisedIndividualAffinityController" - {

    "must return OK and the correct view for GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.routes.UnauthorisedIndividualAffinityController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[UnauthorisedIndividualView]

        val frontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

        status(result) mustEqual OK

        contentAsString(result) mustEqual view()(request, frontendAppConfig, messages(application)).toString


      }

    }
  }

}
