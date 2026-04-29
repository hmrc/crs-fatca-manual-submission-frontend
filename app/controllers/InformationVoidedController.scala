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

import controllers.actions._
import javax.inject.Inject
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.InformationVoidedView

class InformationVoidedController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: InformationVoidedView
) extends FrontendBaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] = (identify andThen getData) {
    implicit request =>

      val emails =
        Seq("email1@test.com") // , "email2@test.com", "email2@test.com") //TODO: build this seq from sub and fi contacts | just sub contact is fiisuser
      val emailString = formatEmailList(emails)
      val fiName      = "[fiName]"
      val dateTime    = "[on 28 April 2026 at 3:36pm]" // TODO: Bring over format logic from other repo, into a helper?
      val mris        = Seq("[GB2026GB-ABC1234567890-FATCA_003]") // ,"[GB2026GB-ABC1234567890-FATCA_004]")

      Ok(view(fiName, dateTime, mris, emailString))
  }

  def formatEmailList(emails: Seq[String]): String = emails match { // TODO: put this somewhere? make a formatting helper
    case Seq(a)       => a
    case Seq(a, b)    => s"$a and $b"
    case init :+ last => init.mkString(", ") + s" and $last"
    case _            => ""
  }
}
