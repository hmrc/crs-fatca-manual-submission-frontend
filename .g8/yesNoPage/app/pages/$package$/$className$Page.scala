package pages.$package$

import models.ReportId
import pages.QuestionPage
import play.api.libs.json.JsPath

final case class $className$Page()(implicit reportId: ReportId) extends QuestionPage[Boolean]:

  override def path: JsPath = JsPath \ reportId.mongoKey \ "$className;format="decap"$"
