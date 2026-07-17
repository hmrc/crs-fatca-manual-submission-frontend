package pages

import models.{ReportId, $className$}
import play.api.libs.json.JsPath

final case class $className$Page()(implicit reportId: ReportId) extends QuestionPage[Set[$className$]]:

  override def path: JsPath = JsPath \ reportId.mongoKey \ "$className;format="decap"$"

