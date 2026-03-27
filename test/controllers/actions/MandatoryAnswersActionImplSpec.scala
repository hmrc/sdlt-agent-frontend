package controllers.actions

import base.SpecBase
import controllers.routes
import models.UserAnswers
import models.requests.DataRequest
import pages.manageAgents.{AgentAddressPage, AgentNamePage}
import play.api.mvc.Result
import play.api.mvc.Results.Redirect
import play.api.test.FakeRequest

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class MandatoryAnswersActionImplSpec extends SpecBase {

  class MandatoryAnswersFake extends MandatoryAnswersActionImpl {
    def transform[A](request: DataRequest[A]): Future[Either[Result, DataRequest[A]]] = refine(request)
  }

  "MandatoryAnswersAction" - {

    "return Right(request) when both AgentName and AgentAddress are present" in {
      val dataRequest = DataRequest(request = FakeRequest(), userId = "id", userAnswers = validUserAnswers, storn = testStorn)
      val action = new MandatoryAnswersFake()
      val result: Either[Result, DataRequest[_]] = action.transform(dataRequest).futureValue

      result mustBe Right(dataRequest)
    }

    "redirect to NoSessionDataController when both AgentName and AgentAddress are missing" in {
      val dataRequest = DataRequest(request = FakeRequest(), userId = "id", userAnswers = emptyUserAnswersWithNoStorn, storn = testStorn)
      val action = new MandatoryAnswersFake()
      val result: Either[Result, DataRequest[_]] = action.transform(dataRequest).futureValue

      result mustBe Left(Redirect(routes.NoSessionDataController.onPageLoad()))
    }

    "redirect to NoSessionDataController when AgentAddress is missing" in {
      val ua = UserAnswers("id")
        .set(AgentNamePage, "John Smith").success.value
      val dataRequest = DataRequest(request = FakeRequest(), userId = "id", userAnswers = ua, storn = testStorn)
      val action = new MandatoryAnswersFake()
      val result: Either[Result, DataRequest[_]] = action.transform(dataRequest).futureValue

      result mustBe Left(Redirect(routes.NoSessionDataController.onPageLoad()))
    }

    "redirect to NoSessionDataController when AgentName is missing" in {
      val ua = UserAnswers("id")
        .set(AgentAddressPage, testAgentAddress).success.value
      val dataRequest = DataRequest(request = FakeRequest(), userId = "id", userAnswers = ua, storn = testStorn)
      val action = new MandatoryAnswersFake()
      val result: Either[Result, DataRequest[_]] = action.transform(dataRequest).futureValue

      result mustBe Left(Redirect(routes.NoSessionDataController.onPageLoad()))
    }
  }
}
