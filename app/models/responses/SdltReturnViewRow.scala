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

import models.responses.UniversalStatus.{ACCEPTED, PENDING, SUBMITTED}
import play.api.i18n.Messages
import play.api.libs.json.{Json, OFormat}

import java.time.LocalDate


case class SdltReturnViewRow(
                                   address: String,
                                   agentReference: String,
                                   dateSubmitted: LocalDate,
                                   utrn: String,
                                   purchaserName: String,
                                   status: UniversalStatus,
                                   returnReference: String
                               ) {
  /*
  Inprogress - ACCEPTED
  Awaiting confirmation - PENDING
  Submitted - SUBMITTED
   */
  def getStatusText()
                   (implicit messages: Messages): String = {
    status match {
      case ACCEPTED =>
        messages("manageReturns.inProgressReturns.status.inprogress")
      case PENDING =>
        messages("manageReturns.inProgressReturns.status.awaiting")
      case SUBMITTED =>
        messages("manageReturns.inProgressReturns.status.submitted")
      case _ =>
        ""
    }
  }


  def getStatusStyle(): String = {
    status match {
      case ACCEPTED =>
        "govuk-tag govuk-tag--blue"
      case PENDING =>
        "govuk-tag govuk-tag--grey"
      case _ =>
        ""
    }
  }

}

object JourneyResultAddressModel {
  implicit val format: OFormat[SdltReturnViewRow] = Json.format[SdltReturnViewRow]
}