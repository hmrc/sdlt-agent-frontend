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

package services

import connectors.StampDutyLandTaxConnector
import models.{AgentDetailsAfterCreation, AgentDetailsBeforeCreation, AgentDetailsRequest, AgentDetailsResponse}
import models.responses.SubmitAgentDetailsResponse
import models.requests.CreatePredefinedAgentRequest
import models.responses.CreatePredefinedAgentResponse
import models.requests.DeletePredefinedAgentRequest
import models.responses.DeletePredefinedAgentResponse
import models.responses.organisation.CreatedAgent
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class StampDutyLandTaxService @Inject() (
  stampDutyLandTaxConnector: StampDutyLandTaxConnector
)(implicit ec: ExecutionContext) {
  
  def getAgentDetails(storn: String, agentReferenceNumber: String)
                     (implicit headerCarrier: HeaderCarrier): Future[Option[CreatedAgent]] =
    stampDutyLandTaxConnector
      .getSdltOrganisation(storn)
      .map(_.agents.find(_.agentResourceReference.contains(agentReferenceNumber)))

  def getAllAgentDetails(storn: String)
                        (implicit headerCarrier: HeaderCarrier): Future[Seq[CreatedAgent]] =
    stampDutyLandTaxConnector
      .getSdltOrganisation(storn)
      .map(_.agents)
    
  def submitAgentDetails(agentDetails: CreatePredefinedAgentRequest)
                        (implicit headerCarrier: HeaderCarrier): Future[CreatePredefinedAgentResponse] =
    stampDutyLandTaxConnector
      .submitAgentDetails(agentDetails)

  def updateAgentDetails(agentDetails: AgentDetailsAfterCreation)
                        (implicit headerCarrier: HeaderCarrier): Future[Unit] =
    stampDutyLandTaxConnector
      .updateAgentDetails(agentDetails)

  def deletePredefinedAgent(deletePredefinedAgentRequest: DeletePredefinedAgentRequest)
                           (implicit headerCarrier: HeaderCarrier): Future[DeletePredefinedAgentResponse] =
    stampDutyLandTaxConnector
      .deletePredefinedAgent(deletePredefinedAgentRequest)

  def isDuplicate(storn: String, name: String)
                 (implicit headerCarrier: HeaderCarrier): Future[Boolean] =
    stampDutyLandTaxConnector
      .getSdltOrganisation(storn)
      .map(_.agents.exists(_.name.contains(name)))
}
