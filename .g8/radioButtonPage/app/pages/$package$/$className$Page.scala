package pages.$package$

import play.api.libs.json.JsPath
import models.ReportId
import models.$package$.$className$
import pages.ReportPage

object $className$Page {

  def apply()(implicit reportId: ReportId): ReportPage[$className$] =
    ReportPage("$className;format="decap"$")
}
