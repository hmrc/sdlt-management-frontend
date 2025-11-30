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

import models.manage.ReturnSummary
import uk.gov.hmrc.govukfrontend.views.viewmodels.pagination.Pagination

case class SdltInProgressReturnViewRow(
                                        address: String,
                                        purchaserName: String,
                                      )

object SdltInProgressReturnViewRow {
  
  def convertResponseToViewRows(inProgressReturnsList: List[ReturnSummary]): List[SdltInProgressReturnViewRow] = {

    for {
      rec    <- inProgressReturnsList
      arn    <- rec.agentReference
    } yield SdltInProgressReturnViewRow(
      address = rec.address,
      purchaserName = rec.purchaserName
    )
  }
}

case class PaginatedInProgressReturnsViewModel(
                                               inProgressReturnsViewModel: List[SdltInProgressReturnViewRow],
                                               paginator: Option[Pagination],
                                               paginationText: Option[String]
                                             )

