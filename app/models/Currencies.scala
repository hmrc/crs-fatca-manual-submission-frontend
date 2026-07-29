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

object Currencies {

  val all: Seq[Currency] = Seq(
    Currency(
      code = "AED",
      displayName = "UAE Dirham (AED)",
      alias = Some("United Arab Emirates Dirham")
    ),
    Currency(
      code = "AFN",
      displayName = "Afghanistan Afghani (AFN)",
      alias = Some("Afghan Afghani")
    ),
    Currency(
      code = "ALL",
      displayName = "Albanian Lek (ALL)"
    ),
    Currency(
      code = "AMD",
      displayName = "Armenian Dram (AMD)"
    ),
    Currency(
      code = "ANG",
      displayName = "Netherlands Antillean Guilder (ANG)",
      alias = Some("Dutch Guilder, Netherlands Antilles Guilder")
    ),
    Currency(
      code = "AOA",
      displayName = "Angolan Kwanza (AOA)"
    ),
    Currency(
      code = "ARS",
      displayName = "Argentine Peso (ARS)",
      alias = Some("Argentina Peso")
    ),
    Currency(
      code = "AUD",
      displayName = "Australian Dollar (AUD)"
    ),
    Currency(
      code = "AWG",
      displayName = "Aruban Florin (AWG)",
      alias = Some("Aruban Guilder")
    ),
    Currency(
      code = "AZN",
      displayName = "Azerbaijani Manat (AZN)"
    ),
    Currency(
      code = "BAM",
      displayName = "Bosnia-Herzegovina Convertible Mark (BAM)",
      alias = Some("Bosnian Convertible Mark")
    ),
    Currency(
      code = "BBD",
      displayName = "Barbados Dollar (BBD)",
      alias = Some("Barbadian Dollar, Bajan Dollar")
    ),
    Currency(
      code = "BDT",
      displayName = "Bangladeshi Taka (BDT)"
    ),
    Currency(
      code = "BGN",
      displayName = "Bulgarian Lev (BGN)"
    ),
    Currency(
      code = "BHD",
      displayName = "Bahraini Dinar (BHD)"
    ),
    Currency(
      code = "BIF",
      displayName = "Burundian Franc (BIF)"
    ),
    Currency(
      code = "BMD",
      displayName = "Bermudian Dollar (BMD)",
      alias = Some("Bermuda Dollar")
    ),
    Currency(
      code = "BND",
      displayName = "Bruneian Dollar (BND)"
    ),
    Currency(
      code = "BOB",
      displayName = "Bolivian Boliviano (BOB)"
    ),
    Currency(
      code = "BOV",
      displayName = "Bolivian Mvdol (BOV)"
    ),
    Currency(
      code = "BRL",
      displayName = "Brazilian Real (BRL)"
    ),
    Currency(
      code = "BSD",
      displayName = "Bahamian Dollar (BSD)",
      alias = Some("Bahamas Dollar")
    ),
    Currency(
      code = "BTN",
      displayName = "Bhutanese Ngultrum (BTN)"
    ),
    Currency(
      code = "BWP",
      displayName = "Botswanan Pula (BWP)"
    ),
    Currency(
      code = "BYN",
      displayName = "Belarusian Ruble (BYN)"
    ),
    Currency(
      code = "BYR",
      displayName = "Belarusian Ruble - Old (BYR)"
    ),
    Currency(
      code = "BZD",
      displayName = "Belize Dollar (BZD)"
    ),
    Currency(
      code = "CAD",
      displayName = "Canadian Dollar (CAD)",
      alias = Some("Canada Dollar")
    ),
    Currency(
      code = "CDF",
      displayName = "Congolese Franc (CDF)"
    ),
    Currency(
      code = "CHE",
      displayName = "Swiss WIR Euro (CHE)",
      alias = Some("Switzerland WIR Euro")
    ),
    Currency(
      code = "CHF",
      displayName = "Swiss Franc (CHF)",
      alias = Some("Liechtenstein Franc")
    ),
    Currency(
      code = "CHW",
      displayName = "Swiss WIR Franc (CHW)",
      alias = Some("Switzerland WIR Franc")
    ),
    Currency(
      code = "CLF",
      displayName = "Chilean Unidad de Fomento (CLF)",
      alias = Some("Chilean Unit of Account")
    ),
    Currency(
      code = "CLP",
      displayName = "Chilean Peso (CLP)"
    ),
    Currency(
      code = "CNY",
      displayName = "Chinese Yuan (CNY)",
      alias = Some("Chinese Yuan Renminbi")
    ),
    Currency(
      code = "COP",
      displayName = "Colombian Peso (COP)"
    ),
    Currency(
      code = "COU",
      displayName = "Colombian Unidad de Valor Real (COU)",
      alias = Some("Columbian Unit of Account")
    ),
    Currency(
      code = "CRC",
      displayName = "Costa Rican Colon (CRC)"
    ),
    Currency(
      code = "CUC",
      displayName = "Cuban Convertible Peso (CUC)",
      alias = Some("Cuba Convertible Peso")
    ),
    Currency(
      code = "CUP",
      displayName = "Cuban Peso (CUP)",
      alias = Some("Cuba Peso")
    ),
    Currency(
      code = "CVE",
      displayName = "Cabo Verde Escudo (CVE)",
      alias = Some("Cape Verde Escudo")
    ),
    Currency(
      code = "CZK",
      displayName = "Czech Koruna (CZK)"
    ),
    Currency(
      code = "DJF",
      displayName = "Djiboutian Franc (DJF)"
    ),
    Currency(
      code = "DKK",
      displayName = "Danish Krone (DKK)",
      alias = Some("Faroe Island Krone, Greenland Krone")
    ),
    Currency(
      code = "DOP",
      displayName = "Dominican Peso (DOP)",
      alias = Some("Dominican Republic Peso")
    ),
    Currency(
      code = "DZD",
      displayName = "Algerian Dinar (DZD)"
    ),
    Currency(
      code = "EGP",
      displayName = "Egyptian Pound (EGP)"
    ),
    Currency(
      code = "ERN",
      displayName = "Eritrean Nakfa (ERN)"
    ),
    Currency(
      code = "ETB",
      displayName = "Ethiopian Birr (ETB)"
    ),
    Currency(
      code = "EUR",
      displayName = "Euro (EUR)"
    ),
    Currency(
      code = "FJD",
      displayName = "Fijian Dollar (FJD)"
    ),
    Currency(
      code = "FKP",
      displayName = "Falkland Islands Pound (FKP)",
      alias = Some("Falkland Island Pound")
    ),
    Currency(
      code = "GBP",
      displayName = "British Pound Sterling (GBP)"
    ),
    Currency(
      code = "GEL",
      displayName = "Georgian Lari (GEL)"
    ),
    Currency(
      code = "GHS",
      displayName = "Ghanaian Cedi (GHS)"
    ),
    Currency(
      code = "GIP",
      displayName = "Gibraltar Pound (GIP)"
    ),
    Currency(
      code = "GMD",
      displayName = "Gambian Dalasi (GMD)"
    ),
    Currency(
      code = "GNF",
      displayName = "Guinean Franc (GNF)"
    ),
    Currency(
      code = "GTQ",
      displayName = "Guatemalan Quetzal (GTQ)"
    ),
    Currency(
      code = "GYD",
      displayName = "Guyanese Dollar (GYD)",
      alias = Some("Guyana Dollar")
    ),
    Currency(
      code = "HKD",
      displayName = "Hong Kong Dollar (HKD)"
    ),
    Currency(
      code = "HNL",
      displayName = "Honduran Lempira (HNL)",
      alias = Some("Honduras Lempira")
    ),
    Currency(
      code = "HRK",
      displayName = "Croatian Kuna (HRK)"
    ),
    Currency(
      code = "HTG",
      displayName = "Haitian Gourde (HTG)"
    ),
    Currency(
      code = "HUF",
      displayName = "Hungarian Forint (HUF)",
      alias = Some("Hungary Forint")
    ),
    Currency(
      code = "IDR",
      displayName = "Indonesian Rupiah (IDR)"
    ),
    Currency(
      code = "ILS",
      displayName = "Israeli Shekel (ILS)",
      alias = Some("Israeli New Shekel, Israeli Sheqel, Israeli New Sheqel")
    ),
    Currency(
      code = "INR",
      displayName = "Indian Rupee (INR)"
    ),
    Currency(
      code = "IQD",
      displayName = "Iraqi Dinar (IQD)"
    ),
    Currency(
      code = "IRR",
      displayName = "Iranian Rial (IRR)"
    ),
    Currency(
      code = "ISK",
      displayName = "Icelandic Krona (ISK)"
    ),
    Currency(
      code = "JMD",
      displayName = "Jamaican Dollar (JMD)"
    ),
    Currency(
      code = "JOD",
      displayName = "Jordanian Dinar (JOD)"
    ),
    Currency(
      code = "JPY",
      displayName = "Japanese Yen (JPY)"
    ),
    Currency(
      code = "KES",
      displayName = "Kenyan Shilling (KES)"
    ),
    Currency(
      code = "KGS",
      displayName = "Kyrgyzstani Som (KGS)"
    ),
    Currency(
      code = "KHR",
      displayName = "Cambodian Riel (KHR)"
    ),
    Currency(
      code = "KMF",
      displayName = "Comorian Franc (KMF)",
      alias = Some("Comoro Franc")
    ),
    Currency(
      code = "KPW",
      displayName = "North Korean Won (KPW)"
    ),
    Currency(
      code = "KRW",
      displayName = "South Korean Won (KRW)"
    ),
    Currency(
      code = "KWD",
      displayName = "Kuwaiti Dinar (KWD)"
    ),
    Currency(
      code = "KYD",
      displayName = "Cayman Islands Dollar (KYD)",
      alias = Some("Caymanian Dollar")
    ),
    Currency(
      code = "KZT",
      displayName = "Kazakhstani Tenge (KZT)"
    ),
    Currency(
      code = "LAK",
      displayName = "Laotian Kip (LAK)"
    ),
    Currency(
      code = "LBP",
      displayName = "Lebanese Pound (LBP)"
    ),
    Currency(
      code = "LKR",
      displayName = "Sri Lankan Rupee (LKR)"
    ),
    Currency(
      code = "LRD",
      displayName = "Liberian Dollar (LRD)"
    ),
    Currency(
      code = "LSL",
      displayName = "Lesotho Loti (LSL)"
    ),
    Currency(
      code = "LTL",
      displayName = "Lithuanian Litas - Old (LTL)",
      alias = Some("Lithuanian Litai")
    ),
    Currency(
      code = "LVL",
      displayName = "Latvian Lats - Old (LVL)",
      alias = Some("Latvian Lati")
    ),
    Currency(
      code = "LYD",
      displayName = "Libyan Dinar (LYD)"
    ),
    Currency(
      code = "MAD",
      displayName = "Moroccan Dirham (MAD)"
    ),
    Currency(
      code = "MDL",
      displayName = "Moldovan Leu (MDL)",
      alias = Some("Moldovan Lei")
    ),
    Currency(
      code = "MGA",
      displayName = "Madagascar Malagasy Ariary (MGA)"
    ),
    Currency(
      code = "MKD",
      displayName = "Macedonian Denar (MKD)"
    ),
    Currency(
      code = "MMK",
      displayName = "Myanmar Kyat (MMK)"
    ),
    Currency(
      code = "MNT",
      displayName = "Mongolian Tugrik (MNT)"
    ),
    Currency(
      code = "MOP",
      displayName = "Macanese Pataca (MOP)"
    ),
    Currency(
      code = "MRO",
      displayName = "Mauritanian Ouguiya - Old (MRO)"
    ),
    Currency(
      code = "MRU",
      displayName = "Mauritanian Ouguiya (MRU)"
    ),
    Currency(
      code = "MUR",
      displayName = "Mauritian Rupee (MUR)",
      alias = Some("Mauritius Rupee")
    ),
    Currency(
      code = "MVR",
      displayName = "Maldivian Rufiyaa (MVR)",
      alias = Some("Maldives Rufiyaa")
    ),
    Currency(
      code = "MWK",
      displayName = "Malawian Kwacha (MWK)"
    ),
    Currency(
      code = "MXN",
      displayName = "Mexican Peso (MXN)",
      alias = Some("Mexico Peso")
    ),
    Currency(
      code = "MXV",
      displayName = "Mexican Unidad de Inversion (MXV)",
      alias = Some("Mexico Unidad de Inversion, Mexican Unit of Funds, Mexico Unit of Funds")
    ),
    Currency(
      code = "MYR",
      displayName = "Malaysian Ringgit (MYR)"
    ),
    Currency(
      code = "MZN",
      displayName = "Mozambican Metical (MZN)",
      alias = Some("Mozambique Metical")
    ),
    Currency(
      code = "NAD",
      displayName = "Namibian Dollar (NAD)"
    ),
    Currency(
      code = "NGN",
      displayName = "Nigerian Naira (NGN)"
    ),
    Currency(
      code = "NIO",
      displayName = "Nicaraguan Cordoba (NIO)",
      alias = Some("Gold Cordoba")
    ),
    Currency(
      code = "NOK",
      displayName = "Norwegian Krone (NOK)"
    ),
    Currency(
      code = "NPR",
      displayName = "Nepalese Rupee (NPR)"
    ),
    Currency(
      code = "NZD",
      displayName = "New Zealand Dollar (NZD)"
    ),
    Currency(
      code = "OMR",
      displayName = "Omani Rial (OMR)"
    ),
    Currency(
      code = "PAB",
      displayName = "Panamanian Balboa (PAB)",
      alias = Some("Panamese Balboa")
    ),
    Currency(
      code = "PEN",
      displayName = "Peruvian Sol (PEN)",
      alias = Some("Nuevo Sol")
    ),
    Currency(
      code = "PGK",
      displayName = "Papua New Guinean Kina (PGK)"
    ),
    Currency(
      code = "PHP",
      displayName = "Philippine Peso (PHP)"
    ),
    Currency(
      code = "PKR",
      displayName = "Pakistani Rupee (PKR)"
    ),
    Currency(
      code = "PLN",
      displayName = "Polish Zloty (PLN)",
      alias = Some("Poland Zloty")
    ),
    Currency(
      code = "PYG",
      displayName = "Paraguayan Guarani (PYG)"
    ),
    Currency(
      code = "QAR",
      displayName = "Qatari Rial (QAR)",
      alias = Some("Qatari Riyal")
    ),
    Currency(
      code = "RON",
      displayName = "Romanian Leu (RON)"
    ),
    Currency(
      code = "RSD",
      displayName = "Serbian Dinar (RSD)"
    ),
    Currency(
      code = "RUB",
      displayName = "Russian Ruble (RUB)"
    ),
    Currency(
      code = "RWF",
      displayName = "Rwandan Franc (RWF)"
    ),
    Currency(
      code = "SAR",
      displayName = "Saudi Arabian Riyal (SAR)",
      alias = Some("Saudi Riyal")
    ),
    Currency(
      code = "SBD",
      displayName = "Solomon Islands Dollar (SBD)"
    ),
    Currency(
      code = "SCR",
      displayName = "Seychellois Rupee (SCR)",
      alias = Some("Seychelles Rupee")
    ),
    Currency(
      code = "SDG",
      displayName = "Sudanese Pound (SDG)"
    ),
    Currency(
      code = "SEK",
      displayName = "Swedish Krona (SEK)"
    ),
    Currency(
      code = "SGD",
      displayName = "Singapore Dollar (SGD)"
    ),
    Currency(
      code = "SHP",
      displayName = "Saint Helenian Pound (SHP)",
      alias = Some("Saint Helena Pound, St Helena Pound, St Helenian Pound")
    ),
    Currency(
      code = "SLL",
      displayName = "Sierra Leonean Leone (SLL)"
    ),
    Currency(
      code = "SOS",
      displayName = "Somali Shilling (SOS)",
      alias = Some("Somalia Shilling")
    ),
    Currency(
      code = "SRD",
      displayName = "Surinamese Dollar (SRD)"
    ),
    Currency(
      code = "SSP",
      displayName = "South Sudanese Pound (SSP)"
    ),
    Currency(
      code = "STD",
      displayName = "Sao Tomean Dobra - Old (STD)",
      alias = Some("Sao Tome and Principe Dobra")
    ),
    Currency(
      code = "STN",
      displayName = "Sao Tomean Dobra (STN)",
      alias = Some("Sao Tome and Principe Dobra")
    ),
    Currency(
      code = "SVC",
      displayName = "Salvadoran Colon (SVC)",
      alias = Some("El Salvador Colon")
    ),
    Currency(
      code = "SYP",
      displayName = "Syrian Pound (SYP)"
    ),
    Currency(
      code = "SZL",
      displayName = "Eswatini Swazi Lilangeni (SZL)",
      alias = Some("Swaziland Lilangeni")
    ),
    Currency(
      code = "THB",
      displayName = "Thai Baht (THB)",
      alias = Some("Thailand Baht")
    ),
    Currency(
      code = "TJS",
      displayName = "Tajikistani Somoni (TJS)"
    ),
    Currency(
      code = "TMT",
      displayName = "Turkmenistani Manat (TMT)",
      alias = Some("Turkmenistani New Manat")
    ),
    Currency(
      code = "TND",
      displayName = "Tunisian Dinar (TND)"
    ),
    Currency(
      code = "TOP",
      displayName = "Tongan Pa'anga (TOP)"
    ),
    Currency(
      code = "TRY",
      displayName = "Turkish Lira (TRY)",
      alias = Some("Turkey Lira")
    ),
    Currency(
      code = "TTD",
      displayName = "Trinidad and Tobago Dollar (TTD)"
    ),
    Currency(
      code = "TWD",
      displayName = "New Taiwan Dollar (TWD)"
    ),
    Currency(
      code = "TZS",
      displayName = "Tanzanian Shilling (TZS)"
    ),
    Currency(
      code = "UAH",
      displayName = "Ukrainian Hryvnia (UAH)",
      alias = Some("Ukraine Hryvnia")
    ),
    Currency(
      code = "UGX",
      displayName = "Ugandan Shilling (UGX)"
    ),
    Currency(
      code = "USD",
      displayName = "United States Dollar (USD)",
      alias = Some("US Dollar")
    ),
    Currency(
      code = "USN",
      displayName = "United States Dollar - Next Day (USN)",
      alias = Some("US Dollar Next Day")
    ),
    Currency(
      code = "USS",
      displayName = "United States Dollar - Same Day - Old (USS)",
      alias = Some("US Dollar Same Day")
    ),
    Currency(
      code = "UYI",
      displayName = "Uruguay Peso en Unidades Indexadas (UYI)"
    ),
    Currency(
      code = "UYU",
      displayName = "Uruguayan Peso (UYU)"
    ),
    Currency(
      code = "UYW",
      displayName = "Uruguayan Unidad Previsional (UYW)"
    ),
    Currency(
      code = "UZS",
      displayName = "Uzbekistani Som (UZS)",
      alias = Some("Uzbekistani Sum")
    ),
    Currency(
      code = "VEF",
      displayName = "Venezuelan Bolivar - Old (VEF)"
    ),
    Currency(
      code = "VES",
      displayName = "Venezuelan Bolivar (VES)"
    ),
    Currency(
      code = "VND",
      displayName = "Vietnamese Dong (VND)"
    ),
    Currency(
      code = "VUV",
      displayName = "Vanuatu Vatu (VUV)"
    ),
    Currency(
      code = "WST",
      displayName = "Samoan Tala (WST)"
    ),
    Currency(
      code = "XAF",
      displayName = "Central African CFA Franc (XAF)",
      alias = Some("Central African Franc")
    ),
    Currency(
      code = "XAG",
      displayName = "Silver in troy ounces (XAG)",
      alias = Some("Silver Ounce")
    ),
    Currency(
      code = "XAU",
      displayName = "Gold in troy ounces (XAU)",
      alias = Some("Gold Ounce")
    ),
    Currency(
      code = "XBA",
      displayName = "European Composite Unit EURCO (XBA)"
    ),
    Currency(
      code = "XBB",
      displayName = "European Monetary Unit 6 EMU-6 (XBB)"
    ),
    Currency(
      code = "XBC",
      displayName = "European Unit of Account 9 EUA-9 (XBC)"
    ),
    Currency(
      code = "XBD",
      displayName = "European Unit of Account 17 EUA-17 (XBD)"
    ),
    Currency(
      code = "XCD",
      displayName = "East Caribbean Dollar (XCD)"
    ),
    Currency(
      code = "XDR",
      displayName = "IMF Special Drawing Rights (XDR)"
    ),
    Currency(
      code = "XFU",
      displayName = "UIC Franc - Old (XFU)"
    ),
    Currency(
      code = "XOF",
      displayName = "West African CFA Franc (XOF)",
      alias = Some("West African Franc")
    ),
    Currency(
      code = "XPD",
      displayName = "Palladium in troy ounces (XPD)",
      alias = Some("Palladium Ounce")
    ),
    Currency(
      code = "XPF",
      displayName = "CFP Franc (XPF)"
    ),
    Currency(
      code = "XPT",
      displayName = "Platinum in troy ounces (XPT)",
      alias = Some("Platinum Ounce")
    ),
    Currency(
      code = "XSU",
      displayName = "Sucre (XSU)"
    ),
    Currency(
      code = "XUA",
      displayName = "African Development Bank Unit of Account (XUA)",
      alias = Some("ADB Unit of Account")
    ),
    Currency(
      code = "YER",
      displayName = "Yemeni Rial (YER)"
    ),
    Currency(
      code = "ZAR",
      displayName = "South African Rand (ZAR)",
      alias = Some("South Africa Rand")
    ),
    Currency(
      code = "ZMW",
      displayName = "Zambian Kwacha (ZMW)"
    ),
    Currency(
      code = "ZWL",
      displayName = "Zimbabwean Dollar (ZWL)"
    )
  )
}
