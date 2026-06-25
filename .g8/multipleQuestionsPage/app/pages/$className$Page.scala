package pages

import models.{ReportId, $className$}

object $className$Page {

  def apply()(implicit reportId: ReportId): ReportPage[$className$] =
    ReportPage("$className;format="decap"$")
}
