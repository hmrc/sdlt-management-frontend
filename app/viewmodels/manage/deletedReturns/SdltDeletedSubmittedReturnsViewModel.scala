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

package viewmodels.manage.deletedReturns

import models.manage.ReturnSummary
import uk.gov.hmrc.govukfrontend.views.viewmodels.pagination.Pagination

case class SdltDeletedSubmittedReturnsViewModel(
                                                address: String,
                                                utrn: String,
                                                purchaserName: String
                                               )

object SdltDeletedSubmittedReturnsViewModel {

  def convertResponseToSubmittedView(submittedReturns: List[ReturnSummary]): List[SdltDeletedSubmittedReturnsViewModel] =

    for {
      rec     <- submittedReturns
      utrn    <- rec.utrn
      address  = rec.address
    } yield SdltDeletedSubmittedReturnsViewModel(
      address       = address,
      utrn          = utrn,
      purchaserName = rec.purchaserName
    )
}

case class PaginatedDeletedSubmittedReturnsViewModel(
  submittedReturnsViewModel: List[SdltDeletedSubmittedReturnsViewModel],
  paginator: Option[Pagination],
  paginationText: Option[String]
)

