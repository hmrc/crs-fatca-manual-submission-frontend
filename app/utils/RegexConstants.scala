/*
 * Copyright 2024 HM Revenue & Customs
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

package utils

object RegexConstants:
  val DEFAULT_STRING_FIELD_VALID: String = """^[A-Za-z0-9&'\\^` -]+$"""
  val DOUBLE_DASH_INVALID: String        = """.*--.*"""
  val POSTCODE_VALID: String             = """^[A-Za-z0-9 ]*$"""
  val POSTCODE_FORMAT: String            = """^[A-Za-z]{1,2}\d[A-Za-z0-9]?\s?\d[A-Za-z]{2}$"""
