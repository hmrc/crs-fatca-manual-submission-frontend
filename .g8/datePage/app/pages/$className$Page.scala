package pages

import java.time.LocalDate

import models.ReportId

object $className$Page {

  def apply()(implicit reportId: ReportId): ReportPage[LocalDate] =
    ReportPage("$className;format="decap"$")
}
