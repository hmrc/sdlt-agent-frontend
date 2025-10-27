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
import models.AgentDetails
import models.responses.SubmitAgentDetailsResponse
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class StampDutyLandTaxService @Inject() (
  stampDutyLandTaxConnector: StampDutyLandTaxConnector
)(implicit ec: ExecutionContext) {

  // TODO: Modify these methods so that we try to retrieve from the session before attempting a BE call
  
  def getAgentDetails(storn: String)
                     (implicit headerCarrier: HeaderCarrier): Future[Option[AgentDetails]] =
    stampDutyLandTaxConnector
      .getAgentDetails(storn)
    
  def getAllAgentDetails(storn: String)
                        (implicit headerCarrier: HeaderCarrier): Future[List[AgentDetails]] =
    stampDutyLandTaxConnector
      .getAllAgentDetails(storn)
    
  def submitAgentDetails(agentDetails: AgentDetails)
                        (implicit headerCarrier: HeaderCarrier): Future[SubmitAgentDetailsResponse] =
    stampDutyLandTaxConnector
      .submitAgentDetails(agentDetails)

  def removeAgentDetails(storn: String, agentReferenceNumber: String)
                        (implicit headerCarrier: HeaderCarrier): Future[Boolean] =
    stampDutyLandTaxConnector
      .removeAgentDetails(storn, agentReferenceNumber)
      
  def isDuplicate(storn: String, name: String)
                 (implicit headerCarrier: HeaderCarrier): Future[Boolean] =
    getAllAgentDetails(storn).map { res =>
      res.exists(agent => agent.name == name )
    }
}
