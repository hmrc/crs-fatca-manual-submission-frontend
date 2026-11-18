package models.manual

import models.elections.RegimeType
import models.elections.RegimeType.FATCA
import models.{AddressModel, ReportId, TaxResidentInformation}

case class OrganisationType (name: String, address:AddressModel, residency: Seq[TaxResidentInformation], reportId: ReportId)

