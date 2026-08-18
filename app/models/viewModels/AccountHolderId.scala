package models.viewModels

import play.api.libs.json.{Format, JsString, Reads, Writes}

import scala.annotation.tailrec
import scala.util.Random

case class AccountHolderId(value: String)

object AccountHolderId {

  implicit val format: Format[AccountHolderId] =
    Format(Reads.StringReads.map(AccountHolderId.apply),
      Writes(
        id => JsString(id.value)
      )
    )

  private val random = new Random()

  @tailrec
  final def generate(existingIds: Set[String]): AccountHolderId = {

    val accountHolderId = AccountHolderId(random.nextInt(99).toString)

    if (existingIds.contains(accountHolderId.value)) {
      generate(existingIds)
    } else {
      accountHolderId
    }
  }
}