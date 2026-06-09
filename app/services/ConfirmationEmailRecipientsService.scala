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

package services

import models.subscription.SubscriptionID
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ConfirmationEmailRecipientsService @Inject() (
  viewFIService: ViewFIService,
  subscriptionService: SubscriptionService
)(implicit ec: ExecutionContext) {

  def getEmailRecipients(fiId: String, subscriptionId: String)(implicit hc: HeaderCarrier): Future[Seq[String]] =
    for {
      fiDetail     <- viewFIService.getFIDetail(subscriptionId, fiId)
      subscription <- subscriptionService.subscription(SubscriptionID(subscriptionId))
    } yield {
      val subscriptionDetails = subscription.success.crfaSubscriptionDetails

      val userEmails = Seq(
        Some(subscriptionDetails.primaryContact.email),
        subscriptionDetails.secondaryContact.map(_.email)
      )

      val fiEmails =
        if (fiDetail.IsFIUser) {
          Set.empty
        } else {
          Seq(
            fiDetail.PrimaryContactDetails.map(_.EmailAddress),
            fiDetail.SecondaryContactDetails.map(_.EmailAddress)
          )
        }

      (userEmails ++ fiEmails).flatten.distinct
    }
}
