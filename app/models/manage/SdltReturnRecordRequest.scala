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

import models.SdltReturnTypes
import play.api.libs.json.{Json, OFormat}

case class SdltReturnRecordRequest(
                                     storn        : String,
                                     status       : Option[String],
                                     deletionFlag : Boolean,
                                     pageType     : Option[String],
                                     pageNumber   : Option[String] = None
                                   )

object SdltReturnRecordRequest {
  implicit val format: OFormat[SdltReturnRecordRequest] = Json.format[SdltReturnRecordRequest]

  def convertToDataRequest(storn: String, extractType: SdltReturnTypes, pageIndex: Option[Int]): SdltReturnRecordRequest = {
    extractType match {
      case SdltReturnTypes.IN_PROGRESS_RETURNS =>
        SdltReturnRecordRequest(
          storn = storn,
          status = None,
          deletionFlag = false,
          pageType = Some("IN-PROGRESS"),
          pageNumber = pageIndex.map(_.toString))
      case SdltReturnTypes.SUBMITTED_SUBMITTED_RETURNS | SdltReturnTypes.SUBMITTED_NO_RECEIPT_RETURNS =>
        SdltReturnRecordRequest(
          storn = storn,
          deletionFlag = false,
          status = None,
          pageType = Some("SUBMITTED"),
          pageNumber = pageIndex.map(_.toString))
      case SdltReturnTypes.IN_PROGRESS_RETURNS_DUE_FOR_DELETION =>
        SdltReturnRecordRequest(
          storn = storn,
          deletionFlag = true,
          status = None,
          pageType = Some("IN-PROGRESS"),
          pageNumber = pageIndex.map(_.toString))
      case SdltReturnTypes.SUBMITTED_RETURNS_DUE_FOR_DELETION =>
        SdltReturnRecordRequest(
          storn = storn,
          deletionFlag = true,
          status = None,
          pageType = Some("SUBMITTED"),
          pageNumber = pageIndex.map(_.toString))
    }
  }

}