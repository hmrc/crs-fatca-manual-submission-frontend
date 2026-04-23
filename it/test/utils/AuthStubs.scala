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

package utils

import org.scalatest.Suite

trait AuthStubs {
  this: Suite =>

  val authUrl            = "/auth/authorise"
  val testAuthInternalId = "internalId"

  val authRequest =
    s"""
       |{
       |  "authorise": [
       |    {
       |      "authProviders": [ "GovernmentGateway" ]
       |    },
       |    {
       |      "confidenceLevel": 50
       |    }
       |  ],
       |  "retrieve": [ "internalId", "allEnrolments", "affinityGroup" ]
       |}
       |""".stripMargin

  def authOKResponse(fatcaId: String) =
    s"""
       |{
       |  "internalId" : "$testAuthInternalId",
       |  "allEnrolments" : [ {
       |    "key" : "HMRC-FATCA-ORG",
       |    "identifiers" : [ {
       |      "key" : "FATCAID",
       |      "value" : "$fatcaId"
       |    } ],
       |    "state" : "Activated",
       |    "confidenceLevel" : 50
       |  } ],
       |  "affinityGroup" : "Organisation"
       |}
       |""".stripMargin
}
