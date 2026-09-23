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

package models.manual.accountHolders

import models.{Enumerable, WithName}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem
import utils.ReportingConstants.REPORTING_THRESHOLD_YEAR

sealed trait SelfCertification

object SelfCertification extends Enumerable.Implicits {

  case object Yes extends WithName("CRS901") with SelfCertification
  case object No extends WithName("CRS902") with SelfCertification
  case object NotReported extends WithName("CRS900") with SelfCertification

  val baseValues: Seq[SelfCertification] = Seq(Yes, No)

  val allValidValues: Seq[SelfCertification] = Seq(
    Yes,
    No,
    NotReported
  )

  def options(reportingPeriod: Int)(implicit messages: Messages): Seq[RadioItem] = {
    val values = if (reportingPeriod >= REPORTING_THRESHOLD_YEAR) baseValues else allValidValues
    values.zipWithIndex.map {
      case (value, index) =>
        RadioItem(
          content = Text(messages(s"accountHolders.selfCertification.${value.toString}")),
          value = Some(value.toString),
          id = Some(s"value_$index")
        )
    }
  }

  implicit val enumerable: Enumerable[SelfCertification] =
    Enumerable(
      allValidValues.map(
        v => v.toString -> v
      ): _*
    )
}
