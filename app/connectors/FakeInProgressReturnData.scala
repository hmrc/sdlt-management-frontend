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

import models.responses.{SdltReturnInfoResponse, UniversalStatus}

import java.time.LocalDate

trait FakeInProgressReturnData {

  /*
  Inprogress - ACCEPTED
  Awaiting confirmation - PENDING
  Submitted - SUBMITTED
   */

  def getData(storn: String):List[SdltReturnInfoResponse] = storn  match {
    case "STN002" =>
      List(
        SdltReturnInfoResponse(
          address = "29 Acacia Road",
          agentReference = "B4N4NM4N",
          dateSubmitted = LocalDate.parse("2025-01-01"),
          utrn = "UTRN001",
          purchaserName = "Wimp",
          status = UniversalStatus.ACCEPTED,
          returnReference = "RETREF001",
          returnId = "RETID001"
        ),
        SdltReturnInfoResponse(
          address = "14 Edgeley Road",
          agentReference = "",
          dateSubmitted = LocalDate.parse("2025-02-02"),
          utrn = "UTRN002",
          purchaserName = "SmithSon",
          status = UniversalStatus.PENDING,
          returnReference = "RETREF002",
          returnId = "RETID002"
        ),
        SdltReturnInfoResponse(
          address = "40 Riverside Drive",
          agentReference = "B4C72F7T3",
          dateSubmitted = LocalDate.parse("2025-04-05"),
          utrn = "UTRN003",
          purchaserName = "Brown",
          status = UniversalStatus.ACCEPTED,
          returnReference = "RETREF003",
          returnId = "RETID003"
        )

      )
    case "STN003" =>
      List(
        SdltReturnInfoResponse(
          address = "29 Acacia Road",
          agentReference = "B4N4NM4N",
          dateSubmitted = LocalDate.parse("2025-01-01"),
          utrn = "UTRN001",
          purchaserName = "Wimp",
          status = UniversalStatus.ACCEPTED,
          returnReference = "RETREF001",
          returnId = "RETID001"
        ),
        SdltReturnInfoResponse(
          address = "14 Edgeley Road",
          agentReference = "",
          dateSubmitted = LocalDate.parse("2025-02-02"),
          utrn = "UTRN002",
          purchaserName = "SmithSon",
          status = UniversalStatus.PENDING,
          returnReference = "RETREF002",
          returnId = "RETID002"
        ),
        SdltReturnInfoResponse(
          address = "40 Riverside Drive",
          agentReference = "B4C72F7T3",
          dateSubmitted = LocalDate.parse("2025-04-05"),
          utrn = "UTRN003",
          purchaserName = "Brown",
          status = UniversalStatus.ACCEPTED,
          returnReference = "RETREF003",
          returnId = "RETID003"
        ),
        SdltReturnInfoResponse(
          address = "29 Acacia Road",
          agentReference = "B4N4NM4N",
          dateSubmitted = LocalDate.parse("2025-01-01"),
          utrn = "UTRN001",
          purchaserName = "Wimp",
          status = UniversalStatus.ACCEPTED,
          returnReference = "RETREF001",
          returnId = "RETID001"
        ),
        SdltReturnInfoResponse(
          address = "14 Edgeley Road",
          agentReference = "",
          dateSubmitted = LocalDate.parse("2025-02-02"),
          utrn = "UTRN002",
          purchaserName = "SmithSon",
          status = UniversalStatus.PENDING,
          returnReference = "RETREF002",
          returnId = "RETID002"
        ),
        SdltReturnInfoResponse(
          address = "40 Riverside Drive",
          agentReference = "B4C72F7T3",
          dateSubmitted = LocalDate.parse("2025-04-05"),
          utrn = "UTRN003",
          purchaserName = "Brown",
          status = UniversalStatus.ACCEPTED,
          returnReference = "RETREF003",
          returnId = "RETID003"
        ),
        SdltReturnInfoResponse(
          address = "29 Acacia Road",
          agentReference = "B4N4NM4N",
          dateSubmitted = LocalDate.parse("2025-01-01"),
          utrn = "UTRN001",
          purchaserName = "Wimp",
          status = UniversalStatus.ACCEPTED,
          returnReference = "RETREF001",
          returnId = "RETID001"
        ),
        SdltReturnInfoResponse(
          address = "14 Edgeley Road",
          agentReference = "",
          dateSubmitted = LocalDate.parse("2025-02-02"),
          utrn = "UTRN002",
          purchaserName = "SmithSon",
          status = UniversalStatus.PENDING,
          returnReference = "RETREF002",
          returnId = "RETID002"
        ),
        SdltReturnInfoResponse(
          address = "40 Riverside Drive",
          agentReference = "B4C72F7T3",
          dateSubmitted = LocalDate.parse("2025-04-05"),
          utrn = "UTRN003",
          purchaserName = "Brown",
          status = UniversalStatus.ACCEPTED,
          returnReference = "RETREF003",
          returnId = "RETID003"
        ),
        SdltReturnInfoResponse(
          address = "29 Acacia Road",
          agentReference = "B4N4NM4N",
          dateSubmitted = LocalDate.parse("2025-01-01"),
          utrn = "UTRN001",
          purchaserName = "Wimp",
          status = UniversalStatus.ACCEPTED,
          returnReference = "RETREF001",
          returnId = "RETID001"
        ),
        SdltReturnInfoResponse(
          address = "14 Edgeley Road",
          agentReference = "",
          dateSubmitted = LocalDate.parse("2025-02-02"),
          utrn = "UTRN002",
          purchaserName = "SmithSon",
          status = UniversalStatus.PENDING,
          returnReference = "RETREF002",
          returnId = "RETID002"
        ),
        SdltReturnInfoResponse(
          address = "40 Riverside Drive",
          agentReference = "B4C72F7T3",
          dateSubmitted = LocalDate.parse("2025-04-05"),
          utrn = "UTRN003",
          purchaserName = "Brown",
          status = UniversalStatus.ACCEPTED,
          returnReference = "RETREF003",
          returnId = "RETID003"
        ),
        SdltReturnInfoResponse(
          address = "29 Acacia Road",
          agentReference = "B4N4NM4N",
          dateSubmitted = LocalDate.parse("2025-01-01"),
          utrn = "UTRN001",
          purchaserName = "Wimp",
          status = UniversalStatus.ACCEPTED,
          returnReference = "RETREF001",
          returnId = "RETID001"
        ),
        SdltReturnInfoResponse(
          address = "14 Edgeley Road",
          agentReference = "",
          dateSubmitted = LocalDate.parse("2025-02-02"),
          utrn = "UTRN002",
          purchaserName = "SmithSon",
          status = UniversalStatus.PENDING,
          returnReference = "RETREF002",
          returnId = "RETID002"
        ),
        SdltReturnInfoResponse(
          address = "40 Riverside Drive",
          agentReference = "B4C72F7T3",
          dateSubmitted = LocalDate.parse("2025-04-05"),
          utrn = "UTRN003",
          purchaserName = "Brown",
          status = UniversalStatus.ACCEPTED,
          returnReference = "RETREF003",
          returnId = "RETID003"
        ),
        SdltReturnInfoResponse(
          address = "29 Acacia Road",
          agentReference = "B4N4NM4N",
          dateSubmitted = LocalDate.parse("2025-01-01"),
          utrn = "UTRN001",
          purchaserName = "Wimp",
          status = UniversalStatus.ACCEPTED,
          returnReference = "RETREF001",
          returnId = "RETID001"
        ),
        SdltReturnInfoResponse(
          address = "14 Edgeley Road",
          agentReference = "",
          dateSubmitted = LocalDate.parse("2025-02-02"),
          utrn = "UTRN002",
          purchaserName = "SmithSon",
          status = UniversalStatus.PENDING,
          returnReference = "RETREF002",
          returnId = "RETID002"
        ),
        SdltReturnInfoResponse(
          address = "40 Riverside Drive",
          agentReference = "B4C72F7T3",
          dateSubmitted = LocalDate.parse("2025-04-05"),
          utrn = "UTRN003",
          purchaserName = "Brown",
          status = UniversalStatus.ACCEPTED,
          returnReference = "RETREF003",
          returnId = "RETID003"
        )
      )
    case _ | "STN001" =>
      List[SdltReturnInfoResponse]()
  }

}
