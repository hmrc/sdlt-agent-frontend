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
import models.{AgentDetailsRequest, AgentDetailsResponse}
import models.responses.SubmitAgentDetailsResponse
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class StampDutyLandTaxService @Inject() (
  stampDutyLandTaxConnector: StampDutyLandTaxConnector
)(implicit ec: ExecutionContext) {
  
  def getAgentDetails(storn: String, agentReferenceNumber: String)
                     (implicit headerCarrier: HeaderCarrier): Future[Option[AgentDetailsResponse]] =
    stampDutyLandTaxConnector
      .getSdltOrganisation(storn)
      .map(_.agents.find(_.agentReferenceNumber == agentReferenceNumber))

  def getAllAgentDetails(storn: String)
                        (implicit headerCarrier: HeaderCarrier): Future[Seq[AgentDetailsResponse]] =
    stampDutyLandTaxConnector
      .getSdltOrganisation(storn)
      .map(_.agents)
    
  def submitAgentDetails(agentDetails: AgentDetailsRequest)
                        (implicit headerCarrier: HeaderCarrier): Future[SubmitAgentDetailsResponse] =
    stampDutyLandTaxConnector
      .submitAgentDetails(agentDetails)

  def removeAgentDetails(storn: String, agentReferenceNumber: String)
                        (implicit headerCarrier: HeaderCarrier): Future[Boolean] =
    stampDutyLandTaxConnector
      .removeAgentDetails(storn, agentReferenceNumber)
      
  def isDuplicate(storn: String, name: String)
                 (implicit headerCarrier: HeaderCarrier): Future[Boolean] =
    stampDutyLandTaxConnector
      .getSdltOrganisation(storn)
      .map(_.agents.exists(_.agentName == name))

  // TODO: TO BE REMOVED - DO NOT USE
  @deprecated
  def getAgentDetailsLegacy(storn: String, agentReferenceNumber: String)
                           (implicit headerCarrier: HeaderCarrier): Future[Option[AgentDetailsResponse]] =
    stampDutyLandTaxConnector
      .getAgentDetails(storn, agentReferenceNumber)

  // TODO: TO BE REMOVED - DO NOT USE
  @deprecated
  def getAllAgentDetailsLegacy(storn: String)
                              (implicit headerCarrier: HeaderCarrier): Future[List[AgentDetailsResponse]] =
    stampDutyLandTaxConnector
      .getAllAgentDetailsLegacy(storn)
}
