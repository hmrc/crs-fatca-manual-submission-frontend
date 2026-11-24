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

package controllers.manual.account

import connectors.DatabaseConnector
import controllers.actions.Actions
import forms.manual.account.IsAccountsSectionCompletedFormProvider
import models.ReportId
import models.ServiceErrors.NoFiDetailFound
import models.requests.ReportIdRequest
import pages.manual.account.IsAccountsSectionCompletedPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Results.Status
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.ViewFIService
import services.manual.account.AccountsService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.manual.account.IsAccountsSectionCompletedView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IsAccountsSectionCompletedController @Inject() (
  override val messagesApi: MessagesApi,
  actions: Actions,
  repository: DatabaseConnector,
  formProvider: IsAccountsSectionCompletedFormProvider,
  viewFIService: ViewFIService,
  accountsService: AccountsService,
  val controllerComponents: MessagesControllerComponents,
  view: IsAccountsSectionCompletedView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val form = formProvider()

  def onPageLoad(): Action[AnyContent] =
    actions.withReportIdRequired().async {
      implicit request =>
        if (accountsService.allAccountsComplete(request.userAnswers, request.reportId)) {
          val answer = request.userAnswers.get(
            IsAccountsSectionCompletedPage(request.reportId)
          )

          render(answer.fold(form)(form.fill), Ok)
        } else {
          Future.successful(
            Redirect(controllers.manual.routes.SendAReportController.onPageLoad())
          )
        }
    }

  def onSubmit(): Action[AnyContent] =
    actions.withReportIdRequired().async {
      implicit request =>
        given ReportId = request.reportId

        val taskList = controllers.manual.routes.SendAReportController.onPageLoad()

        if (accountsService.allAccountsComplete(request.userAnswers, request.reportId)) {
          form
            .bindFromRequest()
            .fold(
              errors => render(errors, BadRequest),
              finished =>
                for {
                  answers <- Future.fromTry(
                    request.userAnswers.setWithReportId(
                      IsAccountsSectionCompletedPage(request.reportId),
                      finished
                    )
                  )
                  _ <- repository.set(answers)
                } yield Redirect(taskList)
            )
        } else {
          Future.successful(Redirect(taskList))
        }
    }

  private def render(
    preparedForm: Form[Boolean],
    status: Status
  )(implicit request: ReportIdRequest[AnyContent]): Future[Result] =
    viewFIService
      .getFIDetail(request.fatcaId, request.reportId.fiId)
      .map(
        fi => status(view(preparedForm, fi.FIName))
      )
      .recover {
        case NoFiDetailFound =>
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      }
}
