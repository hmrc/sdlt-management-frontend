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

case class SdltReturnRecordResponse(
                                     returnSummaryCount : Option[Int] = None,
                                     returnSummaryList  : List[ReturnSummaryLegacy]
                                   )

object SdltReturnRecordResponse {
  implicit val format: OFormat[SdltReturnRecordResponse] = Json.format[SdltReturnRecordResponse]
}

final case class ReturnSummary(
                                returnReference : String,             // p_return_infos.return_resource_ref  (NOT optional)
                                utrn            : Option[String],     // p_return_infos.utrn                 (null for IN-PROGRESS)
                                status          : String,             // p_return_infos.status
                                dateSubmitted   : Option[LocalDate],  // p_return_infos.submitted_date       (null for IN-PROGRESS)
                                purchaserName   : String,             // p_return_infos.name
                                address         : String,             // p_return_infos.address
                                agentReference  : Option[String]      // p_return_infos.agent                (may be null)
                              )

object ReturnSummary {
  implicit val format: OFormat[ReturnSummary] = Json.format[ReturnSummary]
}
