
package forms.manageAgents

import forms.mappings.Mappings
import models.manageAgents.RemoveAgent
import play.api.data.Form

import javax.inject.Inject

class RemoveAgentFormProvider @Inject() extends Mappings {

  def apply(): Form[RemoveAgent] =
    Form(
      "value" -> enumerable[RemoveAgent]("manageAgents.removeAgent.error.required")
    )
}
