package models.manual

sealed trait XmlErrors
case object MissingMandatoryAccountHolderData extends XmlErrors
case object InvalidObjectForRegime extends XmlErrors
