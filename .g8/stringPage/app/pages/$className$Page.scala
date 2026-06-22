package pages

import models.ReportId

object $className$Page {

  def apply()(implicit reportId: ReportId): ReportPage[String] =
    ReportPage("$className;format="decap"$")
}
