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

package models

import models.response.Country

object Countries {

  val all: Seq[Country] = Seq(
    Country(
      code = "AE",
      description = "United Arab Emirates",
      alternativeName = Some("UAE")
    ),
    Country(
      code = "AX",
      description = "Aland Islands"
    ),
    Country(
      code = "CC",
      description = "Cocos (Keeling) Islands",
      alternativeName = Some("Keeling Islands:Cocos Islands")
    ),
    Country(
      code = "CF",
      description = "Central African Republic",
      alternativeName = Some("The Central African Republic")
    ),
    Country(
      code = "CG",
      description = "Congo"
    ),
    Country(
      code = "CK",
      description = "Cook Islands"
    ),
    Country(
      code = "DO",
      description = "Dominican Republic",
      alternativeName = Some("The Dominican Republic")
    ),
    Country(
      code = "FK",
      description = "Falkland Islands"
    ),
    Country(
      code = "FO",
      description = "Faroe Islands"
    ),
    Country(
      code = "GS",
      description = "South Georgia and South Sandwich Islands"
    ),
    Country(
      code = "HM",
      description = "Heard and McDonald Islands"
    ),
    Country(
      code = "IO",
      description = "British Indian Ocean Territory"
    ),
    Country(
      code = "KY",
      description = "Cayman Islands"
    ),
    Country(
      code = "MH",
      description = "Marshall Islands",
      alternativeName = Some("The Marshall Islands")
    ),
    Country(
      code = "MP",
      description = "Northern Mariana Islands"
    ),
    Country(
      code = "NL",
      description = "Netherlands"
    ),
    Country(
      code = "PH",
      description = "Philippines"
    ),
    Country(
      code = "PN",
      description = "Pitcairn, Henderson, Ducie and Oeno Islands"
    ),
    Country(
      code = "PS",
      description = "State of Palestine"
    ),
    Country(
      code = "SB",
      description = "Solomon Islands"
    ),
    Country(
      code = "TC",
      description = "Turks and Caicos Islands"
    ),
    Country(
      code = "TF",
      description = "French Southern Territories"
    ),
    Country(
      code = "UM",
      description = "US Minor Outlying Islands",
      alternativeName = Some("United States Minor Outlying Islands")
    ),
    Country(
      code = "US",
      description = "United States",
      alternativeName = Some("USA")
    ),
    Country(
      code = "VG",
      description = "British Virgin Islands"
    ),
    Country(
      code = "VI",
      description = "United States Virgin Islands",
      alternativeName = Some("US Virgin Islands")
    ),
    Country(
      code = "WF",
      description = "Wallis and Futuna Islands"
    ),
    Country(
      code = "CD",
      description = "Congo (Democratic Republic)",
      alternativeName = Some("The Democratic Republic of the Congo:Democratic Republic of the Congo")
    ),
    Country(
      code = "AD",
      description = "Andorra"
    ),
    Country(
      code = "AF",
      description = "Afghanistan"
    ),
    Country(
      code = "AG",
      description = "Antigua and Barbuda"
    ),
    Country(
      code = "AI",
      description = "Anguilla"
    ),
    Country(
      code = "AL",
      description = "Albania"
    ),
    Country(
      code = "AM",
      description = "Armenia"
    ),
    Country(
      code = "AO",
      description = "Angola"
    ),
    Country(
      code = "AQ",
      description = "Antarctica"
    ),
    Country(
      code = "AR",
      description = "Argentina"
    ),
    Country(
      code = "AS",
      description = "American Samoa"
    ),
    Country(
      code = "AT",
      description = "Austria"
    ),
    Country(
      code = "AU",
      description = "Australia"
    ),
    Country(
      code = "AW",
      description = "Aruba"
    ),
    Country(
      code = "AZ",
      description = "Azerbaijan"
    ),
    Country(
      code = "BA",
      description = "Bosnia and Herzegovina"
    ),
    Country(
      code = "BB",
      description = "Barbados"
    ),
    Country(
      code = "BD",
      description = "Bangladesh"
    ),
    Country(
      code = "BE",
      description = "Belgium"
    ),
    Country(
      code = "BF",
      description = "Burkina Faso"
    ),
    Country(
      code = "BG",
      description = "Bulgaria"
    ),
    Country(
      code = "BH",
      description = "Bahrain"
    ),
    Country(
      code = "BI",
      description = "Burundi"
    ),
    Country(
      code = "BJ",
      description = "Benin"
    ),
    Country(
      code = "BL",
      description = "St Barts",
      alternativeName = Some("Saint Barthelemy:Saint Barts")
    ),
    Country(
      code = "BM",
      description = "Bermuda"
    ),
    Country(
      code = "BN",
      description = "Brunei"
    ),
    Country(
      code = "BO",
      description = "Bolivia"
    ),
    Country(
      code = "BQ",
      description = "Bonaire, Sint Eustatius and Saba"
    ),
    Country(
      code = "BR",
      description = "Brazil"
    ),
    Country(
      code = "BS",
      description = "The Bahamas"
    ),
    Country(
      code = "BT",
      description = "Bhutan"
    ),
    Country(
      code = "BV",
      description = "Bouvet Island"
    ),
    Country(
      code = "BW",
      description = "Botswana"
    ),
    Country(
      code = "BY",
      description = "Belarus"
    ),
    Country(
      code = "BZ",
      description = "Belize"
    ),
    Country(
      code = "CA",
      description = "Canada"
    ),
    Country(
      code = "CH",
      description = "Switzerland"
    ),
    Country(
      code = "CI",
      description = "Ivory Coast",
      alternativeName = Some("The Republic of Cote D'Ivoire:Republic of Cote D'Ivoire:Cote D'Ivoire")
    ),
    Country(
      code = "CL",
      description = "Chile"
    ),
    Country(
      code = "CM",
      description = "Cameroon"
    ),
    Country(
      code = "CN",
      description = "China"
    ),
    Country(
      code = "CO",
      description = "Colombia"
    ),
    Country(
      code = "CR",
      description = "Costa Rica"
    ),
    Country(
      code = "CU",
      description = "Cuba"
    ),
    Country(
      code = "CV",
      description = "Cape Verde"
    ),
    Country(
      code = "CW",
      description = "Curacao"
    ),
    Country(
      code = "CX",
      description = "Christmas Island"
    ),
    Country(
      code = "CY",
      description = "Cyprus"
    ),
    Country(
      code = "CZ",
      description = "Czechia"
    ),
    Country(
      code = "DE",
      description = "Germany"
    ),
    Country(
      code = "DJ",
      description = "Djibouti"
    ),
    Country(
      code = "DK",
      description = "Denmark"
    ),
    Country(
      code = "DM",
      description = "Dominica"
    ),
    Country(
      code = "DZ",
      description = "Algeria"
    ),
    Country(
      code = "EC",
      description = "Ecuador"
    ),
    Country(
      code = "EE",
      description = "Estonia"
    ),
    Country(
      code = "EG",
      description = "Egypt"
    ),
    Country(
      code = "EH",
      description = "Western Sahara"
    ),
    Country(
      code = "ER",
      description = "Eritrea"
    ),
    Country(
      code = "ES",
      description = "Spain"
    ),
    Country(
      code = "ET",
      description = "Ethiopia"
    ),
    Country(
      code = "FI",
      description = "Finland"
    ),
    Country(
      code = "FJ",
      description = "Fiji"
    ),
    Country(
      code = "FM",
      description = "Micronesia",
      alternativeName = Some("Federated States of Micronesia:The Federated States of Micronesia")
    ),
    Country(
      code = "FR",
      description = "France"
    ),
    Country(
      code = "GA",
      description = "Gabon"
    ),
    Country(
      code = "GD",
      description = "Grenada"
    ),
    Country(
      code = "GE",
      description = "Georgia"
    ),
    Country(
      code = "GF",
      description = "French Guiana"
    ),
    Country(
      code = "GH",
      description = "Ghana"
    ),
    Country(
      code = "GI",
      description = "Gibraltar"
    ),
    Country(
      code = "GL",
      description = "Greenland"
    ),
    Country(
      code = "GM",
      description = "The Gambia"
    ),
    Country(
      code = "GN",
      description = "Guinea"
    ),
    Country(
      code = "GP",
      description = "Guadeloupe"
    ),
    Country(
      code = "GQ",
      description = "Equatorial Guinea"
    ),
    Country(
      code = "GR",
      description = "Greece"
    ),
    Country(
      code = "GT",
      description = "Guatemala"
    ),
    Country(
      code = "GU",
      description = "Guam"
    ),
    Country(
      code = "GW",
      description = "Guinea-Bissau"
    ),
    Country(
      code = "GY",
      description = "Guyana"
    ),
    Country(
      code = "HK",
      description = "Hong Kong"
    ),
    Country(
      code = "HN",
      description = "Honduras"
    ),
    Country(
      code = "HR",
      description = "Croatia"
    ),
    Country(
      code = "HT",
      description = "Haiti"
    ),
    Country(
      code = "HU",
      description = "Hungary"
    ),
    Country(
      code = "ID",
      description = "Indonesia"
    ),
    Country(
      code = "IE",
      description = "Ireland"
    ),
    Country(
      code = "IL",
      description = "Israel"
    ),
    Country(
      code = "IN",
      description = "India"
    ),
    Country(
      code = "IQ",
      description = "Iraq"
    ),
    Country(
      code = "IR",
      description = "Iran"
    ),
    Country(
      code = "IS",
      description = "Iceland"
    ),
    Country(
      code = "IT",
      description = "Italy"
    ),
    Country(
      code = "JM",
      description = "Jamaica"
    ),
    Country(
      code = "JO",
      description = "Jordan"
    ),
    Country(
      code = "JP",
      description = "Japan"
    ),
    Country(
      code = "KE",
      description = "Kenya"
    ),
    Country(
      code = "KG",
      description = "Kyrgyzstan"
    ),
    Country(
      code = "KH",
      description = "Cambodia"
    ),
    Country(
      code = "KI",
      description = "Kiribati"
    ),
    Country(
      code = "KM",
      description = "Comoros"
    ),
    Country(
      code = "KN",
      description = "St Kitts and Nevis",
      alternativeName = Some("Saint Kitts and Nevis")
    ),
    Country(
      code = "KP",
      description = "North Korea"
    ),
    Country(
      code = "KR",
      description = "South Korea"
    ),
    Country(
      code = "KW",
      description = "Kuwait"
    ),
    Country(
      code = "KZ",
      description = "Kazakhstan"
    ),
    Country(
      code = "LA",
      description = "Laos"
    ),
    Country(
      code = "LB",
      description = "Lebanon"
    ),
    Country(
      code = "LC",
      description = "St Lucia",
      alternativeName = Some("Saint Lucia")
    ),
    Country(
      code = "LI",
      description = "Liechtenstein"
    ),
    Country(
      code = "LK",
      description = "Sri Lanka"
    ),
    Country(
      code = "LR",
      description = "Liberia"
    ),
    Country(
      code = "LS",
      description = "Lesotho"
    ),
    Country(
      code = "LT",
      description = "Lithuania"
    ),
    Country(
      code = "LU",
      description = "Luxembourg"
    ),
    Country(
      code = "LV",
      description = "Latvia"
    ),
    Country(
      code = "LY",
      description = "Libya"
    ),
    Country(
      code = "MA",
      description = "Morocco"
    ),
    Country(
      code = "MC",
      description = "Monaco"
    ),
    Country(
      code = "MD",
      description = "Moldova"
    ),
    Country(
      code = "ME",
      description = "Montenegro"
    ),
    Country(
      code = "MF",
      description = "St Martin (French part)",
      alternativeName = Some("Saint Martin (French part)")
    ),
    Country(
      code = "MG",
      description = "Madagascar"
    ),
    Country(
      code = "MK",
      description = "North Macedonia"
    ),
    Country(
      code = "ML",
      description = "Mali"
    ),
    Country(
      code = "MM",
      description = "Myanmar (Burma)"
    ),
    Country(
      code = "MN",
      description = "Mongolia"
    ),
    Country(
      code = "MO",
      description = "Macao"
    ),
    Country(
      code = "MQ",
      description = "Martinique"
    ),
    Country(
      code = "MR",
      description = "Mauritania"
    ),
    Country(
      code = "MS",
      description = "Montserrat"
    ),
    Country(
      code = "MT",
      description = "Malta"
    ),
    Country(
      code = "MU",
      description = "Mauritius"
    ),
    Country(
      code = "MV",
      description = "Maldives"
    ),
    Country(
      code = "MW",
      description = "Malawi"
    ),
    Country(
      code = "MX",
      description = "Mexico"
    ),
    Country(
      code = "MY",
      description = "Malaysia"
    ),
    Country(
      code = "MZ",
      description = "Mozambique"
    ),
    Country(
      code = "NA",
      description = "Namibia"
    ),
    Country(
      code = "NC",
      description = "New Caledonia"
    ),
    Country(
      code = "NE",
      description = "Niger"
    ),
    Country(
      code = "NF",
      description = "Norfolk Island"
    ),
    Country(
      code = "NG",
      description = "Nigeria"
    ),
    Country(
      code = "NI",
      description = "Nicaragua"
    ),
    Country(
      code = "NO",
      description = "Norway"
    ),
    Country(
      code = "NP",
      description = "Nepal"
    ),
    Country(
      code = "NR",
      description = "Nauru"
    ),
    Country(
      code = "NU",
      description = "Niue"
    ),
    Country(
      code = "NZ",
      description = "New Zealand"
    ),
    Country(
      code = "OM",
      description = "Oman"
    ),
    Country(
      code = "PA",
      description = "Panama"
    ),
    Country(
      code = "PE",
      description = "Peru"
    ),
    Country(
      code = "PF",
      description = "French Polynesia"
    ),
    Country(
      code = "PG",
      description = "Papua New Guinea"
    ),
    Country(
      code = "PK",
      description = "Pakistan"
    ),
    Country(
      code = "PL",
      description = "Poland"
    ),
    Country(
      code = "PM",
      description = "St Pierre and Miquelon",
      alternativeName = Some("Saint Pierre and Miquelon")
    ),
    Country(
      code = "PR",
      description = "Puerto Rico"
    ),
    Country(
      code = "PT",
      description = "Portugal"
    ),
    Country(
      code = "PW",
      description = "Palau"
    ),
    Country(
      code = "PY",
      description = "Paraguay"
    ),
    Country(
      code = "QA",
      description = "Qatar"
    ),
    Country(
      code = "RE",
      description = "Reunion Island"
    ),
    Country(
      code = "RO",
      description = "Romania"
    ),
    Country(
      code = "RS",
      description = "Serbia"
    ),
    Country(
      code = "RU",
      description = "Russia"
    ),
    Country(
      code = "RW",
      description = "Rwanda"
    ),
    Country(
      code = "SA",
      description = "Saudi Arabia"
    ),
    Country(
      code = "SC",
      description = "Seychelles"
    ),
    Country(
      code = "SD",
      description = "Sudan"
    ),
    Country(
      code = "SE",
      description = "Sweden"
    ),
    Country(
      code = "SG",
      description = "Singapore"
    ),
    Country(
      code = "SH",
      description = "St Helena",
      alternativeName = Some("Saint Helena")
    ),
    Country(
      code = "SI",
      description = "Slovenia"
    ),
    Country(
      code = "SJ",
      description = "Svalbard and Jan Mayen"
    ),
    Country(
      code = "SK",
      description = "Slovakia"
    ),
    Country(
      code = "SL",
      description = "Sierra Leone"
    ),
    Country(
      code = "SM",
      description = "San Marino"
    ),
    Country(
      code = "SN",
      description = "Senegal"
    ),
    Country(
      code = "SO",
      description = "Somalia"
    ),
    Country(
      code = "SR",
      description = "Suriname"
    ),
    Country(
      code = "SS",
      description = "South Sudan"
    ),
    Country(
      code = "ST",
      description = "Sao Tome and Principe"
    ),
    Country(
      code = "SV",
      description = "El Salvador"
    ),
    Country(
      code = "SX",
      description = "Sint Maarten (Dutch part)"
    ),
    Country(
      code = "SY",
      description = "Syria"
    ),
    Country(
      code = "SZ",
      description = "Eswatini"
    ),
    Country(
      code = "TD",
      description = "Chad"
    ),
    Country(
      code = "TG",
      description = "Togo"
    ),
    Country(
      code = "TH",
      description = "Thailand"
    ),
    Country(
      code = "TJ",
      description = "Tajikistan"
    ),
    Country(
      code = "TK",
      description = "Tokelau"
    ),
    Country(
      code = "TL",
      description = "East Timor"
    ),
    Country(
      code = "TM",
      description = "Turkmenistan"
    ),
    Country(
      code = "TN",
      description = "Tunisia"
    ),
    Country(
      code = "TO",
      description = "Tonga"
    ),
    Country(
      code = "TR",
      description = "Turkey"
    ),
    Country(
      code = "TT",
      description = "Trinidad and Tobago"
    ),
    Country(
      code = "TV",
      description = "Tuvalu"
    ),
    Country(
      code = "TW",
      description = "Taiwan"
    ),
    Country(
      code = "TZ",
      description = "Tanzania"
    ),
    Country(
      code = "UA",
      description = "Ukraine"
    ),
    Country(
      code = "UG",
      description = "Uganda"
    ),
    Country(
      code = "UY",
      description = "Uruguay"
    ),
    Country(
      code = "UZ",
      description = "Uzbekistan"
    ),
    Country(
      code = "VA",
      description = "Vatican City"
    ),
    Country(
      code = "VC",
      description = "St Vincent",
      alternativeName = Some("Saint Vincent")
    ),
    Country(
      code = "VE",
      description = "Venezuela"
    ),
    Country(
      code = "VN",
      description = "Vietnam"
    ),
    Country(
      code = "VU",
      description = "Vanuatu"
    ),
    Country(
      code = "WS",
      description = "Samoa"
    ),
    Country(
      code = "XK",
      description = "Kosovo"
    ),
    Country(
      code = "YE",
      description = "Yemen"
    ),
    Country(
      code = "YT",
      description = "Mayotte"
    ),
    Country(
      code = "ZA",
      description = "South Africa"
    ),
    Country(
      code = "ZM",
      description = "Zambia"
    ),
    Country(
      code = "ZW",
      description = "Zimbabwe"
    )
  )
}
