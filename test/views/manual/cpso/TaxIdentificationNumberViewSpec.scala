/*
 * Copyright 2026 HM Revenue & Customs
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

package views.manual.cpso

import base.SpecBase
import forms.manual.cpso.{SelectAddressFormProvider, TaxIdentificationNumberFormProvider}
import models.ReportId
import models.SubmissionsConstants.CRS
import models.manual.cpso.IndividualOrOrganisation.Individual
import models.response.Country
import play.api.i18n.{Lang, Messages}
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.FakeRequest
import views.html.manual.cpso.TaxIdentificationNumberView

class TaxIdentificationNumberViewSpec extends SpecBase {

  private val application = applicationBuilder().build()
  val reportId: ReportId  = ReportId(CRS, 2025, None, "TestfiID")

  private val view: TaxIdentificationNumberView                          = application.injector.instanceOf[TaxIdentificationNumberView]
  private val messagesControllerComponents: MessagesControllerComponents = application.injector.instanceOf[MessagesControllerComponents]
  val form                                                               = new TaxIdentificationNumberFormProvider()(Country.GB, reportId, Individual)

  implicit private val request: FakeRequest[AnyContent] = FakeRequest()
  implicit private val messages: Messages               = messagesControllerComponents.messagesApi.preferred(Seq(Lang("en")))

  "TaxIdentificationNumberView" - {
    "should render page components" - {}
  }

}
