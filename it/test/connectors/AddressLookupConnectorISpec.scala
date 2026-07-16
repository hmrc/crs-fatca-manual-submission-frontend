/*
 * Copyright 2025 HM Revenue & Customs
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

package connectors

import com.github.tomakehurst.wiremock.http.Fault
import models.ServiceErrors.{AddressLookup_Error, Other_Error}
import models.response.{AddressLookup, Country}
import org.scalatest.RecoverMethods.recoverToExceptionIf
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers.{a, include, must, mustBe, mustEqual}
import play.api.http.Status.*
import utils.ISpecBase

import scala.concurrent.ExecutionContext.Implicits.global

class AddressLookupConnectorISpec extends ISpecBase {

  lazy val connector: AddressLookupConnector = app.injector.instanceOf[AddressLookupConnector]
  val findByPostCode                               = "/lookup"
  val postcode: String                       = "ZZ1 1ZZ"

  def addressJson: String =
    s"""[{
       |  "id": "GB200000698110",
       |  "uprn": 200000706253,
       |  "address": {
       |     "lines": [
       |         "1 Address line 1 Road",
       |         "Address line 2 Road"
       |     ],
       |     "town": "Town",
       |     "county": "County",
       |     "postcode": "$postcode",
       |     "subdivision": {
       |         "code": "GB-ENG",
       |         "name": "England"
       |     },
       |     "country": {
       |         "code": "GB",
       |         "name": "United Kingdom"
       |     }
       |  },
       |  "localCustodian": {
       |      "code": 1760,
       |      "name": "Test Valley"
       |  },
       |  "location": [
       |      50.9986451,
       |      -1.4690977
       |  ],
       |  "language": "en"
       |}]""".stripMargin

  override def beforeEach(): Unit =
    server.resetAll()

  "AddressLookupConnector" - {

    "findByPostCode" - {
      "should return the Response when address lookup service returns empty response" in {
        stubPostResponse(findByPostCode, OK, "[]")

        val result = connector.findByPostCode(postcode)
        result.futureValue mustBe Nil
      }

      "should return the Response when address lookup service returns response" in {
        stubPostResponse(findByPostCode, OK, addressJson)

        val addressLookupResult = Seq(
          AddressLookup(200000706253L,Some("1 Address line 1 Road"), None, Some("Address line 2 Road"), None, "Town", Some("County"), postcode, Some(Country.GB))
        )

        val result = connector.findByPostCode(postcode)
        result.futureValue mustBe addressLookupResult
      }

      "must throw an exception when address lookup returns a 400 (BAD_REQUEST) status" in {
        stubPostResponse(findByPostCode, BAD_REQUEST, "Some error")

        val ex = recoverToExceptionIf[AddressLookup_Error.type] {
            connector.findByPostCode(postcode)
          }.futureValue

        ex mustBe AddressLookup_Error
      }

      "must throw an exception when address lookup returns a 404 (NOT_FOUND) status" in {
        stubPostResponse(findByPostCode, NOT_FOUND, "Some error")

        val ex = recoverToExceptionIf[AddressLookup_Error.type] {
            connector.findByPostCode(postcode)
          }.futureValue

        ex mustBe AddressLookup_Error
      }

      "must throw an exception when address lookup returns a 405 (METHOD_NOT_ALLOWED) status" in {
        stubPostResponse(findByPostCode, METHOD_NOT_ALLOWED, "Some error")

        val ex = recoverToExceptionIf[AddressLookup_Error.type] {
            connector.findByPostCode(postcode)
          }.futureValue

        ex mustBe AddressLookup_Error
      }

      "must throw an exception when address lookup returns a 500 (INTERNAL_SERVER_ERROR) status" in {
        stubPostResponse(findByPostCode, INTERNAL_SERVER_ERROR, "Some error")

        val ex =
          recoverToExceptionIf[AddressLookup_Error.type] {
            connector.findByPostCode(postcode)
          }.futureValue

        ex mustBe AddressLookup_Error

      }

      "must throw an exception when connector has exception" in {
        stubPostFault(findByPostCode, Fault.EMPTY_RESPONSE)

        val ex = recoverToExceptionIf[Other_Error.type] {
            connector.findByPostCode(postcode)
          }.futureValue

        ex mustBe Other_Error

      }

      def addressesJson: String =
        s"""[
           |{
           |  "id": "GB200000698110",
           |  "uprn": 200000706260,
           |  "address": {
           |     "lines": [
           |         "Flat 3",
           |         "7 Other place",
           |         "Some District"
           |     ],
           |     "town": "Town",
           |     "county": "County",
           |     "postcode": "$postcode",
           |     "subdivision": {
           |         "code": "GB-ENG",
           |         "name": "England"
           |     },
           |     "country": {
           |         "code": "GB",
           |         "name": "United Kingdom"
           |     }
           |  },
           |  "localCustodian": {
           |      "code": 1760,
           |      "name": "Test Valley"
           |  },
           |  "location": [
           |      50.9986451,
           |      -1.4690977
           |  ],
           |  "language": "en"
           |},
           |{
           |  "id": "GB200000698110",
           |  "uprn": 200000706259,
           |  "address": {
           |     "lines": [
           |         "Flat 2",
           |         "7 Other place",
           |         "Some District"
           |     ],
           |     "town": "Town",
           |     "county": "County",
           |     "postcode": "$postcode",
           |     "subdivision": {
           |         "code": "GB-ENG",
           |         "name": "England"
           |     },
           |     "country": {
           |         "code": "GB",
           |         "name": "United Kingdom"
           |     }
           |  },
           |  "localCustodian": {
           |      "code": 1760,
           |      "name": "Test Valley"
           |  },
           |  "location": [
           |      50.9986451,
           |      -1.4690977
           |  ],
           |  "language": "en"
           |},
           |{
           |  "id": "GB200000698110",
           |  "uprn": 200000706256,
           |  "address": {
           |     "lines": [
           |         "5 Other place",
           |         "Some District"
           |     ],
           |     "town": "Town",
           |     "county": "County",
           |     "postcode": "$postcode",
           |     "subdivision": {
           |         "code": "GB-ENG",
           |         "name": "England"
           |     },
           |     "country": {
           |         "code": "GB",
           |         "name": "United Kingdom"
           |     }
           |  },
           |  "localCustodian": {
           |      "code": 1760,
           |      "name": "Test Valley"
           |  },
           |  "location": [
           |      50.9986451,
           |      -1.4690977
           |  ],
           |  "language": "en"
           |},
           |{
           |  "id": "GB200000698110",
           |  "uprn": 200000706255,
           |  "address": {
           |     "lines": [
           |         "4 Other place",
           |         "Some District"
           |     ],
           |     "town": "Town",
           |     "county": "County",
           |     "postcode": "$postcode",
           |     "subdivision": {
           |         "code": "GB-ENG",
           |         "name": "England"
           |     },
           |     "country": {
           |         "code": "GB",
           |         "name": "United Kingdom"
           |     }
           |  },
           |  "localCustodian": {
           |      "code": 1760,
           |      "name": "Test Valley"
           |  },
           |  "location": [
           |      50.9986451,
           |      -1.4690977
           |  ],
           |  "language": "en"
           |},
           |{
           |  "id": "GB200000698110",
           |  "uprn": 200000706254,
           |  "address": {
           |     "lines": [
           |         "3 Other place",
           |         "Some District"
           |     ],
           |     "town": "Town",
           |     "county": "County",
           |     "postcode": "$postcode",
           |     "subdivision": {
           |         "code": "GB-ENG",
           |         "name": "England"
           |     },
           |     "country": {
           |         "code": "GB",
           |         "name": "United Kingdom"
           |     }
           |  },
           |  "localCustodian": {
           |      "code": 1760,
           |      "name": "Test Valley"
           |  },
           |  "location": [
           |      50.9986451,
           |      -1.4690977
           |  ],
           |  "language": "en"
           |},
           |{
           |  "id": "GB200000698110",
           |  "uprn": 200000706258,
           |  "address": {
           |     "lines": [
           |         "Flat 1",
           |         "7 Other place",
           |         "Some District"
           |     ],
           |     "town": "Town",
           |     "county": "County",
           |     "postcode": "$postcode",
           |     "subdivision": {
           |         "code": "GB-ENG",
           |         "name": "England"
           |     },
           |     "country": {
           |         "code": "GB",
           |         "name": "United Kingdom"
           |     }
           |  },
           |  "localCustodian": {
           |      "code": 1760,
           |      "name": "Test Valley"
           |  },
           |  "location": [
           |      50.9986451,
           |      -1.4690977
           |  ],
           |  "language": "en"
           |},
           |{
           |  "id": "GB200000698110",
           |  "uprn": 200000706253,
           |  "address": {
           |     "lines": [
           |         "2 Other place",
           |         "Some District"
           |     ],
           |     "town": "Town",
           |     "county": "County",
           |     "postcode": "$postcode",
           |     "subdivision": {
           |         "code": "GB-ENG",
           |         "name": "England"
           |     },
           |     "country": {
           |         "code": "GB",
           |         "name": "United Kingdom"
           |     }
           |  },
           |  "localCustodian": {
           |      "code": 1760,
           |      "name": "Test Valley"
           |  },
           |  "location": [
           |      50.9986451,
           |      -1.4690977
           |  ],
           |  "language": "en"
           |},
           |{
           |  "id": "GB200000698110",
           |  "uprn": 200000706261,
           |  "address": {
           |     "lines": [
           |         "8 Other place",
           |         "Some District"
           |     ],
           |     "town": "Town",
           |     "county": "County",
           |     "postcode": "$postcode",
           |     "subdivision": {
           |         "code": "GB-ENG",
           |         "name": "England"
           |     },
           |     "country": {
           |         "code": "GB",
           |         "name": "United Kingdom"
           |     }
           |  },
           |  "localCustodian": {
           |      "code": 1760,
           |      "name": "Test Valley"
           |  },
           |  "location": [
           |      50.9986451,
           |      -1.4690977
           |  ],
           |  "language": "en"
           |},
           |{
           |  "id": "GB200000698110",
           |  "uprn": 200000706262,
           |  "address": {
           |     "lines": [
           |         "9 Other place",
           |         "Some District"
           |     ],
           |     "town": "Town",
           |     "county": "County",
           |     "postcode": "$postcode",
           |     "subdivision": {
           |         "code": "GB-ENG",
           |         "name": "England"
           |     },
           |     "country": {
           |         "code": "GB",
           |         "name": "United Kingdom"
           |     }
           |  },
           |  "localCustodian": {
           |      "code": 1760,
           |      "name": "Test Valley"
           |  },
           |  "location": [
           |      50.9986451,
           |      -1.4690977
           |  ],
           |  "language": "en"
           |},
           |{
           |  "id": "GB200000698110",
           |  "uprn": 200000706263,
           |  "address": {
           |     "lines": [
           |         "10 Other place",
           |         "Some District"
           |     ],
           |     "town": "Town",
           |     "county": "County",
           |     "postcode": "$postcode",
           |     "subdivision": {
           |         "code": "GB-ENG",
           |         "name": "England"
           |     },
           |     "country": {
           |         "code": "GB",
           |         "name": "United Kingdom"
           |     }
           |  },
           |  "localCustodian": {
           |      "code": 1760,
           |      "name": "Test Valley"
           |  },
           |  "location": [
           |      50.9986451,
           |      -1.4690977
           |  ],
           |  "language": "en"
           |},
           |{
           |  "id": "GB200000698110",
           |  "uprn": 200000706257,
           |  "address": {
           |     "lines": [
           |         "6 Other place",
           |         "Some District"
           |     ],
           |     "town": "Town",
           |     "county": "County",
           |     "postcode": "$postcode",
           |     "subdivision": {
           |         "code": "GB-ENG",
           |         "name": "England"
           |     },
           |     "country": {
           |         "code": "GB",
           |         "name": "United Kingdom"
           |     }
           |  },
           |  "localCustodian": {
           |      "code": 1760,
           |      "name": "Test Valley"
           |  },
           |  "location": [
           |      50.9986451,
           |      -1.4690977
           |  ],
           |  "language": "en"
           |},
           |
           |
           |
           |{
           |  "id": "GB200000698110",
           |  "uprn": 200000706274,
           |  "address": {
           |     "lines": [
           |         "Efer House 137a",
           |         "Back High Street",
           |         "Gosforth"
           |     ],
           |     "town": "Newcastle upon Tyne",
           |     "county": "County",
           |     "postcode": "$postcode",
           |     "subdivision": {
           |         "code": "GB-ENG",
           |         "name": "England"
           |     },
           |     "country": {
           |         "code": "GB",
           |         "name": "United Kingdom"
           |     }
           |  },
           |  "localCustodian": {
           |      "code": 1760,
           |      "name": "Test Valley"
           |  },
           |  "location": [
           |      50.9986451,
           |      -1.4690977
           |  ],
           |  "language": "en"
           |},
           |{
           |  "id": "GB200000698110",
           |  "uprn": 200000706272,
           |  "address": {
           |     "lines": [
           |         "99-99a",
           |         "Back High Street",
           |         "Gosforth"
           |     ],
           |     "town": "Newcastle upon Tyne",
           |     "county": "County",
           |     "postcode": "$postcode",
           |     "subdivision": {
           |         "code": "GB-ENG",
           |         "name": "England"
           |     },
           |     "country": {
           |         "code": "GB",
           |         "name": "United Kingdom"
           |     }
           |  },
           |  "localCustodian": {
           |      "code": 1760,
           |      "name": "Test Valley"
           |  },
           |  "location": [
           |      50.9986451,
           |      -1.4690977
           |  ],
           |  "language": "en"
           |},
           |{
           |  "id": "GB200000698110",
           |  "uprn": 200000706273,
           |  "address": {
           |     "lines": [
           |         "135 Back High Street",
           |         "Gosforth"
           |     ],
           |     "town": "Newcastle upon Tyne",
           |     "county": "County",
           |     "postcode": "$postcode",
           |     "subdivision": {
           |         "code": "GB-ENG",
           |         "name": "England"
           |     },
           |     "country": {
           |         "code": "GB",
           |         "name": "United Kingdom"
           |     }
           |  },
           |  "localCustodian": {
           |      "code": 1760,
           |      "name": "Test Valley"
           |  },
           |  "location": [
           |      50.9986451,
           |      -1.4690977
           |  ],
           |  "language": "en"
           |},
           |{
           |  "id": "GB200000698110",
           |  "uprn": 200000706275,
           |  "address": {
           |     "lines": [
           |         "141 Back High Street",
           |         "Gosforth"
           |     ],
           |     "town": "Newcastle upon Tyne",
           |     "county": "County",
           |     "postcode": "$postcode",
           |     "subdivision": {
           |         "code": "GB-ENG",
           |         "name": "England"
           |     },
           |     "country": {
           |         "code": "GB",
           |         "name": "United Kingdom"
           |     }
           |  },
           |  "localCustodian": {
           |      "code": 1760,
           |      "name": "Test Valley"
           |  },
           |  "location": [
           |      50.9986451,
           |      -1.4690977
           |  ],
           |  "language": "en"
           |},
           |{
           |  "id": "GB200000698110",
           |  "uprn": 200000706276,
           |  "address": {
           |     "lines": [
           |         "143 Back High Street",
           |         "Gosforth"
           |     ],
           |     "town": "Newcastle upon Tyne",
           |     "county": "County",
           |     "postcode": "$postcode",
           |     "subdivision": {
           |         "code": "GB-ENG",
           |         "name": "England"
           |     },
           |     "country": {
           |         "code": "GB",
           |         "name": "United Kingdom"
           |     }
           |  },
           |  "localCustodian": {
           |      "code": 1760,
           |      "name": "Test Valley"
           |  },
           |  "location": [
           |      50.9986451,
           |      -1.4690977
           |  ],
           |  "language": "en"
           |},
           |{
           |  "id": "GB200000698110",
           |  "uprn": 200000706277,
           |  "address": {
           |     "lines": [
           |         "153 Back High Street",
           |         "Gosforth"
           |     ],
           |     "town": "Newcastle upon Tyne",
           |     "county": "County",
           |     "postcode": "$postcode",
           |     "subdivision": {
           |         "code": "GB-ENG",
           |         "name": "England"
           |     },
           |     "country": {
           |         "code": "GB",
           |         "name": "United Kingdom"
           |     }
           |  },
           |  "localCustodian": {
           |      "code": 1760,
           |      "name": "Test Valley"
           |  },
           |  "location": [
           |      50.9986451,
           |      -1.4690977
           |  ],
           |  "language": "en"
           |},
           |
           |
           |
           |
           |
           |
           |
           |
           |
           |
           |{
           |  "id": "GB200000698110",
           |  "uprn": 200000706269,
           |  "address": {
           |     "lines": [
           |         "Apartment 301",
           |         "11 Waterloo Street",
           |         "Some District"
           |     ],
           |     "town": "Town",
           |     "county": "County",
           |     "postcode": "$postcode",
           |     "subdivision": {
           |         "code": "GB-ENG",
           |         "name": "England"
           |     },
           |     "country": {
           |         "code": "GB",
           |         "name": "United Kingdom"
           |     }
           |  },
           |  "localCustodian": {
           |      "code": 1760,
           |      "name": "Test Valley"
           |  },
           |  "location": [
           |      50.9986451,
           |      -1.4690977
           |  ],
           |  "language": "en"
           |},
           |{
           |  "id": "GB200000698110",
           |  "uprn": 200000706270,
           |  "address": {
           |     "lines": [
           |         "Apartment 302",
           |         "11 Waterloo Street",
           |         "Some District"
           |     ],
           |     "town": "Town",
           |     "county": "County",
           |     "postcode": "$postcode",
           |     "subdivision": {
           |         "code": "GB-ENG",
           |         "name": "England"
           |     },
           |     "country": {
           |         "code": "GB",
           |         "name": "United Kingdom"
           |     }
           |  },
           |  "localCustodian": {
           |      "code": 1760,
           |      "name": "Test Valley"
           |  },
           |  "location": [
           |      50.9986451,
           |      -1.4690977
           |  ],
           |  "language": "en"
           |},
           |{
           |  "id": "GB200000698110",
           |  "uprn": 200000706265,
           |  "address": {
           |     "lines": [
           |         "Unit 1",
           |         "11 Waterloo Street",
           |         "Some District"
           |     ],
           |     "town": "Town",
           |     "county": "County",
           |     "postcode": "$postcode",
           |     "subdivision": {
           |         "code": "GB-ENG",
           |         "name": "England"
           |     },
           |     "country": {
           |         "code": "GB",
           |         "name": "United Kingdom"
           |     }
           |  },
           |  "localCustodian": {
           |      "code": 1760,
           |      "name": "Test Valley"
           |  },
           |  "location": [
           |      50.9986451,
           |      -1.4690977
           |  ],
           |  "language": "en"
           |},
           |{
           |  "id": "GB200000698110",
           |  "uprn": 200000706264,
           |  "address": {
           |     "lines": [
           |         "Suite 1",
           |         "11 Waterloo Street",
           |         "Some District"
           |     ],
           |     "town": "Town",
           |     "county": "County",
           |     "postcode": "$postcode",
           |     "subdivision": {
           |         "code": "GB-ENG",
           |         "name": "England"
           |     },
           |     "country": {
           |         "code": "GB",
           |         "name": "United Kingdom"
           |     }
           |  },
           |  "localCustodian": {
           |      "code": 1760,
           |      "name": "Test Valley"
           |  },
           |  "location": [
           |      50.9986451,
           |      -1.4690977
           |  ],
           |  "language": "en"
           |},
           |{
           |  "id": "GB200000698110",
           |  "uprn": 200000706268,
           |  "address": {
           |     "lines": [
           |         "Suite 3",
           |         "11 Waterloo Street",
           |         "Some District"
           |     ],
           |     "town": "Town",
           |     "county": "County",
           |     "postcode": "$postcode",
           |     "subdivision": {
           |         "code": "GB-ENG",
           |         "name": "England"
           |     },
           |     "country": {
           |         "code": "GB",
           |         "name": "United Kingdom"
           |     }
           |  },
           |  "localCustodian": {
           |      "code": 1760,
           |      "name": "Test Valley"
           |  },
           |  "location": [
           |      50.9986451,
           |      -1.4690977
           |  ],
           |  "language": "en"
           |},
           |{
           |  "id": "GB200000698110",
           |  "uprn": 200000706266,
           |  "address": {
           |     "lines": [
           |         "Suite 2",
           |         "11 Waterloo Street",
           |         "Some District"
           |     ],
           |     "town": "Town",
           |     "county": "County",
           |     "postcode": "$postcode",
           |     "subdivision": {
           |         "code": "GB-ENG",
           |         "name": "England"
           |     },
           |     "country": {
           |         "code": "GB",
           |         "name": "United Kingdom"
           |     }
           |  },
           |  "localCustodian": {
           |      "code": 1760,
           |      "name": "Test Valley"
           |  },
           |  "location": [
           |      50.9986451,
           |      -1.4690977
           |  ],
           |  "language": "en"
           |},
           |{
           |  "id": "GB200000698110",
           |  "uprn": 200000706267,
           |  "address": {
           |     "lines": [
           |         "Unit 2",
           |         "11 Waterloo Street",
           |         "Some District"
           |     ],
           |     "town": "Town",
           |     "county": "County",
           |     "postcode": "$postcode",
           |     "subdivision": {
           |         "code": "GB-ENG",
           |         "name": "England"
           |     },
           |     "country": {
           |         "code": "GB",
           |         "name": "United Kingdom"
           |     }
           |  },
           |  "localCustodian": {
           |      "code": 1760,
           |      "name": "Test Valley"
           |  },
           |  "location": [
           |      50.9986451,
           |      -1.4690977
           |  ],
           |  "language": "en"
           |},
           |{
           |  "id": "GB200000698110",
           |  "uprn": 200000706271,
           |  "address": {
           |     "lines": [
           |         "Apartment 400",
           |         "11 Waterloo Street",
           |         "Some District"
           |     ],
           |     "town": "Town",
           |     "county": "County",
           |     "postcode": "$postcode",
           |     "subdivision": {
           |         "code": "GB-ENG",
           |         "name": "England"
           |     },
           |     "country": {
           |         "code": "GB",
           |         "name": "United Kingdom"
           |     }
           |  },
           |  "localCustodian": {
           |      "code": 1760,
           |      "name": "Test Valley"
           |  },
           |  "location": [
           |      50.9986451,
           |      -1.4690977
           |  ],
           |  "language": "en"
           |}]""".stripMargin

      "must return sorted addresses" in {
        stubPostResponse(findByPostCode, OK, addressesJson)

        val addressLookupResult = Vector(
          AddressLookup(200000706253L,Some("2 Other place"), None, Some("Some District"), None, "Town", Some("County"), postcode, Some(Country.GB)),
          AddressLookup(200000706254L,Some("3 Other place"), None, Some("Some District"), None, "Town", Some("County"), postcode, Some(Country.GB)),
          AddressLookup(200000706255L,Some("4 Other place"), None, Some("Some District"), None, "Town", Some("County"), postcode, Some(Country.GB)),
          AddressLookup(200000706256L,Some("5 Other place"), None, Some("Some District"), None, "Town", Some("County"), postcode, Some(Country.GB)),
          AddressLookup(200000706257L,Some("6 Other place"), None, Some("Some District"), None, "Town", Some("County"), postcode, Some(Country.GB)),
          AddressLookup(200000706258L,Some("Flat 1"), Some("7 Other place"), Some("Some District"), None, "Town", Some("County"), postcode, Some(Country.GB)),
          AddressLookup(200000706259L,Some("Flat 2"), Some("7 Other place"), Some("Some District"), None, "Town", Some("County"), postcode, Some(Country.GB)),
          AddressLookup(200000706260L,Some("Flat 3"), Some("7 Other place"), Some("Some District"), None, "Town", Some("County"), postcode, Some(Country.GB)),
          AddressLookup(200000706261L,Some("8 Other place"), None, Some("Some District"), None, "Town", Some("County"), postcode, Some(Country.GB)),
          AddressLookup(200000706262L,Some("9 Other place"), None, Some("Some District"), None, "Town", Some("County"), postcode, Some(Country.GB)),
          AddressLookup(200000706263L,Some("10 Other place"), None, Some("Some District"), None, "Town", Some("County"), postcode, Some(Country.GB)),
          AddressLookup(200000706264L,Some("Suite 1"), Some("11 Waterloo Street"), Some("Some District"), None, "Town", Some("County"), postcode, Some(Country.GB)),
          AddressLookup(200000706265L,Some("Unit 1"), Some("11 Waterloo Street"), Some("Some District"), None, "Town", Some("County"), postcode, Some(Country.GB)),
          AddressLookup(200000706266L,Some("Suite 2"), Some("11 Waterloo Street"), Some("Some District"), None, "Town", Some("County"), postcode, Some(Country.GB)),
          AddressLookup(200000706267L,Some("Unit 2"), Some("11 Waterloo Street"), Some("Some District"), None, "Town", Some("County"), postcode, Some(Country.GB)),
          AddressLookup(200000706268L,Some("Suite 3"), Some("11 Waterloo Street"), Some("Some District"), None, "Town", Some("County"), postcode, Some(Country.GB)),
          AddressLookup(200000706269L,Some("Apartment 301"), Some("11 Waterloo Street"), Some("Some District"), None, "Town", Some("County"), postcode, Some(Country.GB)),
          AddressLookup(200000706270L,Some("Apartment 302"), Some("11 Waterloo Street"), Some("Some District"), None, "Town", Some("County"), postcode, Some(Country.GB)),
          AddressLookup(200000706271L,Some("Apartment 400"), Some("11 Waterloo Street"), Some("Some District"), None, "Town", Some("County"), postcode, Some(Country.GB)),
          AddressLookup(200000706272L,Some("99-99a"), Some("Back High Street"), Some("Gosforth"), None, "Newcastle upon Tyne", Some("County"), postcode, Some(Country.GB)),
          AddressLookup(200000706273L,Some("135 Back High Street"), None, Some("Gosforth"), None, "Newcastle upon Tyne", Some("County"), postcode, Some(Country.GB)),
          AddressLookup(200000706274L,Some("Efer House 137a"),
            Some("Back High Street"),
            Some("Gosforth"),
            None,
            "Newcastle upon Tyne",
            Some("County"),
            postcode,
            Some(Country.GB)
          ),
          AddressLookup(200000706275L,Some("141 Back High Street"), None, Some("Gosforth"), None, "Newcastle upon Tyne", Some("County"), postcode, Some(Country.GB)),
          AddressLookup(200000706276L,Some("143 Back High Street"), None, Some("Gosforth"), None, "Newcastle upon Tyne", Some("County"), postcode, Some(Country.GB)),
          AddressLookup(200000706277L,Some("153 Back High Street"), None, Some("Gosforth"), None, "Newcastle upon Tyne", Some("County"), postcode, Some(Country.GB))
        )

        val result = connector.findByPostCode(postcode)

        result.futureValue mustBe addressLookupResult
      }

      def addressesJsonv2: String =
        s"""[
           |{
           |  "id": "GB200000698110",
           |  "uprn": 200000706253,
           |  "address": {
           |     "lines": [
           |         "Apartment 301",
           |         "11 Waterloo Street",
           |         "Some District"
           |     ],
           |     "town": "Town",
           |     "county": "County",
           |     "postcode": "$postcode",
           |     "subdivision": {
           |         "code": "GB-ENG",
           |         "name": "England"
           |     },
           |     "country": {
           |         "code": "GB",
           |         "name": "United Kingdom"
           |     }
           |  },
           |  "localCustodian": {
           |      "code": 1760,
           |      "name": "Test Valley"
           |  },
           |  "location": [
           |      50.9986451,
           |      -1.4690977
           |  ],
           |  "language": "en"
           |},
           |{
           |  "id": "GB200000698110",
           |  "uprn": 200000706253,
           |  "address": {
           |     "lines": [
           |         "Apartment 302",
           |         "11 Waterloo Street",
           |         "Some District"
           |     ],
           |     "town": "Town",
           |     "county": "County",
           |     "postcode": "$postcode",
           |     "subdivision": {
           |         "code": "GB-ENG",
           |         "name": "England"
           |     },
           |     "country": {
           |         "code": "GB",
           |         "name": "United Kingdom"
           |     }
           |  },
           |  "localCustodian": {
           |      "code": 1760,
           |      "name": "Test Valley"
           |  },
           |  "location": [
           |      50.9986451,
           |      -1.4690977
           |  ],
           |  "language": "en"
           |},
           |{
           |  "id": "GB200000698110",
           |  "uprn": 200000706253,
           |  "address": {
           |     "lines": [
           |         "Unit 1",
           |         "11 Waterloo Street",
           |         "Some District"
           |     ],
           |     "town": "Town",
           |     "county": "County",
           |     "postcode": "$postcode",
           |     "subdivision": {
           |         "code": "GB-ENG",
           |         "name": "England"
           |     },
           |     "country": {
           |         "code": "GB",
           |         "name": "United Kingdom"
           |     }
           |  },
           |  "localCustodian": {
           |      "code": 1760,
           |      "name": "Test Valley"
           |  },
           |  "location": [
           |      50.9986451,
           |      -1.4690977
           |  ],
           |  "language": "en"
           |},
           |{
           |  "id": "GB200000698110",
           |  "uprn": 200000706253,
           |  "address": {
           |     "lines": [
           |         "Suite 1",
           |         "11 Waterloo Street",
           |         "Some District"
           |     ],
           |     "town": "Town",
           |     "county": "County",
           |     "postcode": "$postcode",
           |     "subdivision": {
           |         "code": "GB-ENG",
           |         "name": "England"
           |     },
           |     "country": {
           |         "code": "GB",
           |         "name": "United Kingdom"
           |     }
           |  },
           |  "localCustodian": {
           |      "code": 1760,
           |      "name": "Test Valley"
           |  },
           |  "location": [
           |      50.9986451,
           |      -1.4690977
           |  ],
           |  "language": "en"
           |},
           |{
           |  "id": "GB200000698110",
           |  "uprn": 200000706253,
           |  "address": {
           |     "lines": [
           |         "Suite 3",
           |         "11 Waterloo Street",
           |         "Some District"
           |     ],
           |     "town": "Town",
           |     "county": "County",
           |     "postcode": "$postcode",
           |     "subdivision": {
           |         "code": "GB-ENG",
           |         "name": "England"
           |     },
           |     "country": {
           |         "code": "GB",
           |         "name": "United Kingdom"
           |     }
           |  },
           |  "localCustodian": {
           |      "code": 1760,
           |      "name": "Test Valley"
           |  },
           |  "location": [
           |      50.9986451,
           |      -1.4690977
           |  ],
           |  "language": "en"
           |},
           |{
           |  "id": "GB200000698110",
           |  "uprn": 200000706253,
           |  "address": {
           |     "lines": [
           |         "Suite 2",
           |         "11 Waterloo Street",
           |         "Some District"
           |     ],
           |     "town": "Town",
           |     "county": "County",
           |     "postcode": "$postcode",
           |     "subdivision": {
           |         "code": "GB-ENG",
           |         "name": "England"
           |     },
           |     "country": {
           |         "code": "GB",
           |         "name": "United Kingdom"
           |     }
           |  },
           |  "localCustodian": {
           |      "code": 1760,
           |      "name": "Test Valley"
           |  },
           |  "location": [
           |      50.9986451,
           |      -1.4690977
           |  ],
           |  "language": "en"
           |},
           |{
           |  "id": "GB200000698110",
           |  "uprn": 200000706253,
           |  "address": {
           |     "lines": [
           |         "Unit 2",
           |         "11 Waterloo Street",
           |         "Some District"
           |     ],
           |     "town": "Town",
           |     "county": "County",
           |     "postcode": "$postcode",
           |     "subdivision": {
           |         "code": "GB-ENG",
           |         "name": "England"
           |     },
           |     "country": {
           |         "code": "GB",
           |         "name": "United Kingdom"
           |     }
           |  },
           |  "localCustodian": {
           |      "code": 1760,
           |      "name": "Test Valley"
           |  },
           |  "location": [
           |      50.9986451,
           |      -1.4690977
           |  ],
           |  "language": "en"
           |},
           |{
           |  "id": "GB200000698110",
           |  "uprn": 200000706253,
           |  "address": {
           |     "lines": [
           |         "Apartment 400",
           |         "11 Waterloo Street",
           |         "Some District"
           |     ],
           |     "town": "Town",
           |     "county": "County",
           |     "postcode": "$postcode",
           |     "subdivision": {
           |         "code": "GB-ENG",
           |         "name": "England"
           |     },
           |     "country": {
           |         "code": "GB",
           |         "name": "United Kingdom"
           |     }
           |  },
           |  "localCustodian": {
           |      "code": 1760,
           |      "name": "Test Valley"
           |  },
           |  "location": [
           |      50.9986451,
           |      -1.4690977
           |  ],
           |  "language": "en"
           |}]""".stripMargin

      "must return sorted addresses v2" in {
        stubPostResponse(findByPostCode, OK, addressesJsonv2)

        val addressLookupResult = Vector(
          AddressLookup(200000706253L,Some("Suite 1"), Some("11 Waterloo Street"), Some("Some District"), None, "Town", Some("County"), postcode, Some(Country.GB)),
          AddressLookup(200000706253L,Some("Unit 1"), Some("11 Waterloo Street"), Some("Some District"), None, "Town", Some("County"), postcode, Some(Country.GB)),
          AddressLookup(200000706253L,Some("Suite 2"), Some("11 Waterloo Street"), Some("Some District"), None, "Town", Some("County"), postcode, Some(Country.GB)),
          AddressLookup(200000706253L,Some("Unit 2"), Some("11 Waterloo Street"), Some("Some District"), None, "Town", Some("County"), postcode, Some(Country.GB)),
          AddressLookup(200000706253L,Some("Suite 3"), Some("11 Waterloo Street"), Some("Some District"), None, "Town", Some("County"), postcode, Some(Country.GB)),
          AddressLookup(200000706253L,Some("Apartment 301"), Some("11 Waterloo Street"), Some("Some District"), None, "Town", Some("County"), postcode, Some(Country.GB)),
          AddressLookup(200000706253L,Some("Apartment 302"), Some("11 Waterloo Street"), Some("Some District"), None, "Town", Some("County"), postcode, Some(Country.GB)),
          AddressLookup(200000706253L,Some("Apartment 400"), Some("11 Waterloo Street"), Some("Some District"), None, "Town", Some("County"), postcode, Some(Country.GB))
        )

        val result = connector.findByPostCode(postcode)

        result.futureValue mustBe addressLookupResult
      }
    }
  }
}
