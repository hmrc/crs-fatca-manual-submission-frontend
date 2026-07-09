package pages.manual

import models.{ReportId, WhatTypeOfFiler}

object WhatTypeOfFilerPage {

  def apply()(implicit reportId: ReportId): ReportPage[WhatTypeOfFiler] =
    ReportPage("whatTypeOfFiler")
}
