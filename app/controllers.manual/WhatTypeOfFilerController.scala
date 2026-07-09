package controllers.manual

import controllers.actions._
import forms..manual.WhatTypeOfFilerFormProvider
import javax.inject.Inject
import models.{Mode, ReportId}
import navigation.ManualSubmissionNavigator
import pages.manual.WhatTypeOfFilerPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import connectors.DatabaseConnector
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.manual.WhatTypeOfFilerView

import scala.concurrent.{ExecutionContext, Future}

class WhatTypeOfFilerController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       sessionRepository: DatabaseConnector,
                                       navigator: ManualSubmissionNavigator,
                                       identify: IdentifierAction,
                                       getData: DataRetrievalAction,
                                       requireData: DataRequiredAction,
                                       reportIdAction: ReportIdRequiredAction,
                                       formProvider: WhatTypeOfFilerFormProvider,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: WhatTypeOfFilerView
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData andThen reportIdAction) {
    implicit request =>
      implicit val reportId: ReportId = request.reportId
      val preparedForm = request.userAnswers.get(WhatTypeOfFilerPage()) match {
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
            updatedAnswers <- Future.fromTry(request.userAnswers.set(WhatTypeOfFilerPage(), value))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(WhatTypeOfFilerPage(), mode, updatedAnswers))
      )
  }
}
