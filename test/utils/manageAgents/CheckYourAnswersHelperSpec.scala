package utils.manageAgents

import base.SpecBase
import models.UserAnswers
import org.scalatest.matchers.must.Matchers
import pages.manageAgents.{AgentAddressPage, AgentNamePage}
import play.api.Application
import play.api.i18n.Messages
import play.api.mvc.Results.Redirect
import play.api.mvc.Result
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import utils.manageAgents.CheckYourAnswersHelper.*

class CheckYourAnswersHelperSpec extends Matchers with SpecBase {

  "getSummaryListRows" - {

    "include all rows when all answers are present" in new Setup {
      val result: SummaryList = CheckYourAnswersHelper.getSummaryListRows(populatedUserAnswers)
      result.rows.length mustBe 4
    }

    "include all rows when optional answers are empty" in new Setup {
      val ua: UserAnswers = UserAnswers("id")
        .set(AgentNamePage, "Haborview Estates").success.value
        .set(AgentAddressPage, testAgentAddress).success.value

      val result: SummaryList = CheckYourAnswersHelper.getSummaryListRows(ua)
      result.rows.length mustBe 4
    }

  }

  "validateUserAnswers" - {

    "returns Right when required fields exist" in new Setup {
      val result: Either[Result, SummaryList] = validateUserAnswers(validUserAnswers)

      result.isRight mustBe true
    }

    "redirect to NoSessionDataController when AgentName is missing" in new Setup {
      val ua: UserAnswers = UserAnswers("id")
        .set(AgentAddressPage, testAgentAddress).success.value
      val result: Either[Result, SummaryList] = validateUserAnswers(ua)

      result mustBe Left(Redirect(noSessionDataUrl))
    }

    "redirect to NoSessionDataController when AgentAddress is missing" in new Setup {
      val ua: UserAnswers = UserAnswers("id")
        .set(AgentNamePage, "Haborview Estates").success.value
      val result: Either[Result, SummaryList] = validateUserAnswers(ua)

      result mustBe Left(Redirect(noSessionDataUrl))
    }

    "redirect to NoSessionDataController when both answers are missing" in new Setup {
      val result: Either[Result, SummaryList] = validateUserAnswers(emptyUserAnswersWithStorn)

      result mustBe Left(Redirect(noSessionDataUrl))
    }
  }

  trait Setup {
    val app: Application = applicationBuilder().build()
    implicit val messages: Messages = play.api.i18n.MessagesImpl(play.api.i18n.Lang.defaultLang, app.injector.instanceOf[play.api.i18n.MessagesApi])
    val noSessionDataUrl: String = controllers.routes.NoSessionDataController.onPageLoad().url
  }
}
