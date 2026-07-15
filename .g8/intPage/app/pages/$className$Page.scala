package pages

import models.ReportId
import play.api.libs.json.JsPath

final case class $className$Page()(implicit reportId: ReportId) extends QuestionPage[Int]:

  override def path: JsPath = JsPath \ reportId.mongoKey \ "$className;format="decap"$"
