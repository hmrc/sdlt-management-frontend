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

package models.responses

import play.api.libs.json._
import play.api.libs.json.Reads._

enum UniversalStatus:
  case STARTED
  case VALIDATED
  case PENDING
  case ACCEPTED
  case SUBMITTED
  case SUBMITTED_NO_RECEIPT
  case DEPARTMENTAL_ERROR
  case FATAL_ERROR


object UniversalStatus {
  implicit val universalStatusFormat: Format[UniversalStatus] = new Format[UniversalStatus] {
    def reads(json: JsValue): JsResult[UniversalStatus] = json match {
      case JsString(s) => s.toUpperCase() match {
        case "STARTED" => JsSuccess(UniversalStatus.STARTED)
        case "VALIDATED" => JsSuccess(UniversalStatus.VALIDATED)
        case "PENDING" => JsSuccess(UniversalStatus.PENDING)
        case "SUBMITTED" => JsSuccess(UniversalStatus.SUBMITTED)
        case "SUBMITTED_NO_RECEIPT" => JsSuccess(UniversalStatus.SUBMITTED_NO_RECEIPT)
        case "DEPARTMENTAL_ERROR" => JsSuccess(UniversalStatus.DEPARTMENTAL_ERROR)
        case "FATAL_ERROR" => JsSuccess(UniversalStatus.FATAL_ERROR)
        case _ => JsError("Invalid UniversalStatus string")
      }
      case _ => JsError("Expected JsString")
    }

    def writes(status: UniversalStatus): JsValue =
      JsString(status.toString.toUpperCase())
  }
  
  def fromString(in: String): Option[UniversalStatus] = {
    in.toUpperCase() match {
      case "STARTED" => Some(UniversalStatus.STARTED)
      case "VALIDATED" => Some(UniversalStatus.VALIDATED)
      case "PENDING" => Some(UniversalStatus.PENDING)
      case "SUBMITTED" => Some(UniversalStatus.SUBMITTED)
      case "SUBMITTED_NO_RECEIPT" => Some(UniversalStatus.SUBMITTED_NO_RECEIPT)
      case "DEPARTMENTAL_ERROR" => Some(UniversalStatus.DEPARTMENTAL_ERROR)
      case "FATAL_ERROR" => Some(UniversalStatus.FATAL_ERROR)
      case _ => None
    }
  }
  
}
