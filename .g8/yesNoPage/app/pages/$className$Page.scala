package pages

import models.ReportId

object $className$Page {

  def apply()(implicit reportId: ReportId): ReportPage[Boolean] =
    ReportPage("$className;format="decap"$")
}
