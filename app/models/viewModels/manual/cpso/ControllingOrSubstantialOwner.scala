/*
 * Copyright 2026 HM Revenue & Customs
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

package models.viewModels.manual.cpso

import models.manual.cpso.IndividualName
import models.{ReportId, UserAnswers}
import pages.manual.cpso.ControllingOrSubstantialOwnersPage
import play.api.libs.json.OFormat.oFormatFromReadsAndOWrites
import play.api.libs.json.*

case class ControllingOrSubstantialOwner(accountHolders: Option[Seq[String]], individualName: Option[IndividualName], organisationName: Option[String])

object ControllingOrSubstantialOwner {
  implicit val format: OFormat[ControllingOrSubstantialOwner] = Json.format[ControllingOrSubstantialOwner]
}

case class ControllingOrSubstantialOwners(currentCpsoId: Option[CPSOId], cpsos: Map[String, ControllingOrSubstantialOwner])

object ControllingOrSubstantialOwners {

  private val CurrentcpsoId = "currentCPSOId"

  implicit val format: OFormat[ControllingOrSubstantialOwners] = new OFormat[ControllingOrSubstantialOwners] {

    override def reads(json: JsValue): JsResult[ControllingOrSubstantialOwners] =
      json.validate[JsObject].flatMap {
        obj =>

          val cpsoId =
            (obj \ CurrentcpsoId).validateOpt[CPSOId]

          val cpsoEntries =
            JsObject(
              obj.fields.filterNot(_._1 == CurrentcpsoId)
            ).validate[Map[String, ControllingOrSubstantialOwner]]

          for {
            currentId <- cpsoId
            cpso      <- cpsoEntries
          } yield ControllingOrSubstantialOwners(currentId, cpso)
      }

    override def writes(value: ControllingOrSubstantialOwners): JsObject = {
      val cpsoJson =
        JsObject(
          value.cpsos.map {
            case (id, cpso) =>
              id -> Json.toJson(cpso)
          }
        )

      value.currentCpsoId match {
        case Some(id) =>
          cpsoJson + (CurrentcpsoId -> Json.toJson(id))

        case None =>
          cpsoJson
      }
    }
  }

  def existingCpsos(userAnswers: UserAnswers)(implicit reportId: ReportId): Seq[(CPSOId, ControllingOrSubstantialOwner)] =
    userAnswers
      .get(ControllingOrSubstantialOwnersPage())
      .map(
        _.cpsos
          .map(
            (cpsoId, controllingOrSubstantialOwner) => (CPSOId(cpsoId), controllingOrSubstantialOwner)
          )
          .toSeq
      )
      .getOrElse(Seq.empty)
}
