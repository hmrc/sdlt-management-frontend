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

package models.manage

import play.api.libs.json.{Json, OFormat}

import java.time.LocalDate

@deprecated("Use SdltReturnRecordResponse")
case class SdltReturnRecordResponseLegacy(
                                           storn              : String,
                                           returnSummaryCount : Int,
                                           returnSummaryList  : List[ReturnSummaryLegacy]
                                         )

@deprecated("Use SdltReturnRecordResponse")
object SdltReturnRecordResponseLegacy {
  implicit val format: OFormat[SdltReturnRecordResponseLegacy] = Json.format[SdltReturnRecordResponseLegacy]
}

@deprecated("Use ReturnSummary")
case class ReturnSummaryLegacy(
                                returnReference : String,
                                utrn            : String,
                                status          : String,
                                dateSubmitted   : LocalDate,
                                purchaserName   : String,
                                address         : String,
                                agentReference  : String
                              )

@deprecated("Use ReturnSummary")
object ReturnSummaryLegacy {
  implicit val format: OFormat[ReturnSummaryLegacy] = Json.format[ReturnSummaryLegacy]
}
