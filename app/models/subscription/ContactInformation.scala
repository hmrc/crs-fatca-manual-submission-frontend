/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package models.subscription

import play.api.libs.json.*

import scala.language.implicitConversions

sealed trait ContactType
case class OrganisationDetails(name: String) extends ContactType
case class IndividualDetails(firstName: String, lastName: String, middleName: Option[String] = None) extends ContactType

object ContactType:

  given Reads[ContactType] = Reads {
    json =>
      (json \ "individual")
        .validate[IndividualDetails]
        .orElse((json \ "organisation").validate[OrganisationDetails])
  }

  given Writes[ContactType] = Writes {
    case individualDetails: IndividualDetails =>
      Json.obj("individual" -> Json.toJson(individualDetails))

    case organisationDetails: OrganisationDetails =>
      Json.obj("organisation" -> Json.toJson(organisationDetails))
  }

object OrganisationDetails:
  given OFormat[OrganisationDetails] = Json.format[OrganisationDetails]

object IndividualDetails:
  given OFormat[IndividualDetails] = Json.format[IndividualDetails]

case class ContactInformation(contactInformation: ContactType, email: String, phone: Option[String], mobile: Option[String])

object ContactInformation:

  given OFormat[ContactInformation] = new OFormat[ContactInformation] {
    def reads(json: JsValue): JsResult[ContactInformation] =
      for {
        contactInformation <- ContactType.given_Reads_ContactType.reads(json)
        email              <- (json \ "email").validate[String]
        phone              <- (json \ "phone").validateOpt[String]
        mobile             <- (json \ "mobile").validateOpt[String]
      } yield ContactInformation(contactInformation, email, phone, mobile)

    def writes(contactInformation: ContactInformation): JsObject =
      val phone = contactInformation.phone match {
        case Some(value) => Json.obj("phone" -> value)
        case _           => Json.obj()
      }
      val mobile = contactInformation.mobile match {
        case Some(value) => Json.obj("mobile" -> value)
        case _           => Json.obj()
      }
      Json.toJson(contactInformation.contactInformation).as[JsObject] ++
        Json.obj(
          "email" -> contactInformation.email
        ) ++ phone ++ mobile
  }
