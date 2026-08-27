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

package forms

import forms.behaviours.StringFieldBehaviours
import org.scalatest.matchers.should.Matchers.shouldBe
import models.SubmissionsConstants.{CRS, FATCA, RegimeType}

class IndividualNameFormProviderSpec extends StringFieldBehaviours {

  val form    = new IndividualNameFormProvider()
  val regimes = List(CRS, FATCA)

  private val validData = Map(
    "firstName" -> "some-name",
    "lastName"  -> "some-last-name"
  )

  "IndividualNameFormProvider" - {
    "bind valid data" in {
      regimes.foreach {
        regime =>
          val result = form(regime).bind(validData)
          result.errors shouldBe empty
      }
    }

    "firstName" - {
      "cannot be empty" in {
        regimes.foreach {
          regime =>
            val result = form(regime).bind(validData.updated("firstName", ""))
            result.errors("firstName").map(_.message) shouldBe Seq(s"cpso.individualName.${regime.toString.toLowerCase}.error.firstName.required")
        }
      }

      "must fail when longer than 200 characters" in {
        val firstName = "A" * 201
        regimes.foreach {
          regime =>
            val result = form(regime).bind(validData.updated("firstName", firstName))
            result.errors("firstName").map(_.message) shouldBe Seq(s"cpso.individualName.${regime.toString.toLowerCase}.error.firstName.length")
        }
      }

      "must fail when invalid characters are entered" in {
        val firsName = "Some Name!"
        regimes.foreach {
          regime =>
            val result = form(regime).bind(validData.updated("firstName", firsName))
            result.errors("firstName").map(_.message) shouldBe Seq(s"cpso.individualName.${regime.toString.toLowerCase}.error.firstName.invalid.characters")
        }
      }

      "must fail when it contains a double dash" in {
        val firstName = "first--name"
        regimes.foreach {
          regime =>
            val result = form(regime).bind(validData.updated("firstName", firstName))
            result.errors("firstName").map(_.message) shouldBe Seq(
              s"cpso.individualName.${regime.toString.toLowerCase}.error.firstName.invalid.characters.combination"
            )
        }
      }
    }

    "lastName" - {
      "cannot be empty" in {
        regimes.foreach {
          regime =>
            val result = form(regime).bind(validData.updated("lastName", ""))
            result.errors("lastName").map(_.message) shouldBe Seq(s"cpso.individualName.${regime.toString.toLowerCase}.error.lastName.required")
        }
      }

      "must fail when longer than 200 characters" in {
        val lastName = "A" * 201
        regimes.foreach {
          regime =>
            val result = form(regime).bind(validData.updated("lastName", lastName))
            result.errors("lastName").map(_.message) shouldBe Seq(s"cpso.individualName.${regime.toString.toLowerCase}.error.lastName.length")
        }
      }

      "must fail when invalid characters are entered" in {
        val lastName = "Some Name!"
        regimes.foreach {
          regime =>
            val result = form(regime).bind(validData.updated("lastName", lastName))
            result.errors("lastName").map(_.message) shouldBe Seq(s"cpso.individualName.${regime.toString.toLowerCase}.error.lastName.invalid.characters")
        }
      }

      "must fail when it contains a double dash" in {
        val lastName = "last--name"
        regimes.foreach {
          regime =>
            val result = form(regime).bind(validData.updated("lastName", lastName))
            result.errors("lastName").map(_.message) shouldBe Seq(
              s"cpso.individualName.${regime.toString.toLowerCase}.error.lastName.invalid.characters.combination"
            )
        }
      }
    }

  }

}
