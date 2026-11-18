package models

import models.SubmissionsConstants.RegimeType
import play.api.libs.json.{Format, JsError, JsResult, JsValue, Json, OFormat}

trait AddressModel {
  implicit val regime: RegimeType
}

object AddressModel {

  implicit val addressFormatter: Format[AddressModel] = new Format[AddressModel] {
    override def writes(address: AddressModel): JsValue = address match {
      case uk: UkAddress       => UkAddress.format.writes(uk) ++ Json.obj("type" -> "uk")
      case nonUk: AddressNonUk => AddressNonUk.format.writes(nonUk) ++ Json.obj("type" -> "nonUk")
    }

    override def reads(json: JsValue): JsResult[AddressModel] =
      (json \ "type").validate[String].flatMap {
        case "uk"    => json.validate[UkAddress]
        case "nonUk" => json.validate[AddressNonUk]
        case other   => JsError(s"Unknown address type: $other")
      }

  }
}
