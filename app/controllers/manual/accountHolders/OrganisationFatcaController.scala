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

package controllers.manual.accountHolders

import connectors.DatabaseConnector
import controllers.actions.*
import forms.manual.accountHolders.OrganisationFatcaFormProvider
import models.{Mode, ReportId}
import navigation.ManualSubmissionNavigator
import pages.manual.accountHolders.{AccountHolderOrganisationNamePage, OrganisationFatcaPage}
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.manual.accountHolders.OrganisationFatcaView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class OrganisationFatcaController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: DatabaseConnector,
  navigator: ManualSubmissionNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  reportIdAction: ReportIdRequiredAction,
  accountHolderIdCreationAction: AccountHolderIdCreationAction,
  formProvider: OrganisationFatcaFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: OrganisationFatcaView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData andThen reportIdAction andThen accountHolderIdCreationAction) {
    implicit request =>
      implicit val reportId: ReportId = request.reportId
      val preparedForm = request.userAnswers.get(OrganisationFatcaPage(request.accountHolderId)) match {
        case None        => form
        case Some(value) => form.fill(value)
      }
      val organisationName = request.userAnswers.get(AccountHolderOrganisationNamePage(request.accountHolderId))
      organisationName match {
        case Some(orgName) => Ok(view(preparedForm, mode, orgName))
        case _ =>
          logger.warn("Missing organisation name")
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      }

  }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen reportIdAction andThen accountHolderIdCreationAction).async {
      implicit request =>
        implicit val reportId: ReportId = request.reportId
        val organisationName            = request.userAnswers.get(AccountHolderOrganisationNamePage(request.accountHolderId))
        organisationName match {
          case Some(orgName) =>
            form
              .bindFromRequest()
              .fold(
                formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, orgName))),
                value =>
                  for {
                    updatedAnswers <- Future.fromTry(request.userAnswers.setWithReportId(OrganisationFatcaPage(request.accountHolderId), value))
                    _              <- sessionRepository.set(updatedAnswers)
                  } yield Redirect(navigator.nextPage(OrganisationFatcaPage(request.accountHolderId), mode, updatedAnswers))
              )
          case _ =>
            logger.warn("Missing organisation name")
            Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
        }

    }
}
