package models

import play.api.libs.json.{Json, OFormat}

case class FiDetails(fiId: String, fiName: String)

object FiDetails {
  implicit val format: OFormat[FiDetails] = Json.format[FiDetails]
}
