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

package controllers

import controllers.actions.*
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.ElectionsService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.ManageElectionsView

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.ExecutionContext

class ManageElectionsController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: ManageElectionsView,
  service: ElectionsService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(year: Int, fiId: String, fiName: String): Action[AnyContent] = identify.async {
    implicit request =>
      val currentYear = LocalDate.now().getYear
      val years       = currentYear - 12 to currentYear

      for {
        rows <- service.getElectionsRows(fiId, year)
      } yield Ok(view(years, rows, year, fiName))
  }
}
