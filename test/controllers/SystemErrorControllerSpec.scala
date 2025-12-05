package controllers

import base.SpecBase
import config.FrontendAppConfig
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.http.HttpVerbs.GET

class SystemErrorControllerSpec extends SpecBase with MockitoSugar {

  "SystemErrorController" - {
    val mockAppConfig = mock[FrontendAppConfig]

    "SystemErrorController " - {
      "must return Ok and correct view for Get" in {
        val app = applicationBuilder(Some(emptyUserAnswers)).build()

        when(mockAppConfig.hmrcOnlineServiceDeskUrl)
          .thenReturn("https://www.gov.uk")

        running(app) {
          val request = FakeRequest(GET, routes.SystemErrorController.onPageLoad().url)

          val result = route(app, request).value

          status(result) mustEqual OK

        }

      }

    }
  }
}
