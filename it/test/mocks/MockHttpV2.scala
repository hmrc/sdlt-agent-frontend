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

package mocks

import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{mock, reset, when}
import org.mockito.stubbing.OngoingStubbing
import uk.gov.hmrc.http.StringContextOps

import scala.concurrent.Future

trait MockHttpV2  {
  
  lazy val mockHttpClient: HttpClientV2 = mock(classOf[HttpClientV2])
  lazy val mockRequestBuilder: RequestBuilder = mock(classOf[RequestBuilder])

//  override def beforeEach(): Unit = {
//    super.beforeEach()
//    reset(mockHttpClient)
//    reset(mockRequestBuilder)
//  }


  def setupMockHttpPost[T](url: String)(response: T): OngoingStubbing[Future[T]] = {
    when(mockHttpClient.post( ArgumentMatchers.eq(url"$url") )(any())).thenReturn(mockRequestBuilder)
    when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
    when(mockRequestBuilder.execute[T](any(), any())).thenReturn(Future.successful(response))
  }
}
