package pages

import models.ReportId

object $className$Page {

  def apply()(implicit reportId: ReportId): ReportPage[Int] =
    ReportPage("$className;format="decap"$")
}
