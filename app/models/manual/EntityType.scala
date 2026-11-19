package models.manual

sealed trait EntityType
case object Sponsor extends EntityType
case object ReportingFi extends EntityType
case object AccountHolder extends EntityType
case object SubstantialOwner extends EntityType
case object ControllingPerson extends EntityType
