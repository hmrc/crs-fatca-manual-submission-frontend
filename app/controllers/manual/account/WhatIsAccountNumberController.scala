package controllers

import controllers.actions._
import forms.WhatIsAccountNumberFormProvider
import javax.inject.Inject
import models.{Mode, ReportId}
import navigation.ManualSubmissionNavigator
import pages.WhatIsAccountNumberPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import connectors.DatabaseConnector
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.WhatIsAccountNumberView

import scala.concurrent.{ExecutionContext, Future}

class WhatIsAccountNumberController @Inject()(
                                        override val messagesApi: MessagesApi,
                                        repository: DatabaseConnector,
                                        navigator: ManualSubmissionNavigator,
                                        identify: IdentifierAction,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        reportIdAction: ReportIdRequiredAction,
                                        formProvider: WhatIsAccountNumberFormProvider,
                                        val controllerComponents: MessagesControllerComponents,
                                        view: WhatIsAccountNumberView
                                    )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData andThen reportIdAction) {
    implicit request =>

      implicit val reportId: ReportId = request.reportId

      val preparedForm = request.userAnswers.get(WhatIsAccountNumberPage()) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData andThen reportIdAction).async {
    implicit request =>

      implicit val reportId: ReportId = request.reportId

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode))),

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.setWithReportId(WhatIsAccountNumberPage(), value))
            _              <- repository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(WhatIsAccountNumberPage(), mode, updatedAnswers))
      )
  }
}
