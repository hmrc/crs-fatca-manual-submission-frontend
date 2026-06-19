package pages

import models.{ReportId, $className$}

object $className$Page {

  def apply()(implicit reportId: ReportId): ReportPage[Set[$className$]] =
    ReportPage("$className;format="decap"$")
}

