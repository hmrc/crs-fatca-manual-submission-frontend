package pages

import models.ReportId

object $className$Page {

  def apply()(implicit reportId: ReportId): ReportPage[BigDecimal] =
    ReportPage("$className;format="decap"$")
}