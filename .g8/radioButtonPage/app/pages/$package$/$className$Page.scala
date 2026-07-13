package pages.$package$

import models.ReportId
import models.$package$.$className$
import pages.ReportPage

object $className$Page {

  def apply()(implicit reportId: ReportId): ReportPage[$className$] =
    ReportPage("$className;format="decap"$")
}
