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

package viewmodels.manage
import models.manage.ReturnSummary
import models.responses.UniversalStatus
import uk.gov.hmrc.govukfrontend.views.viewmodels.pagination.Pagination

case class SdltSubmittedReturnsViewModel(
                                        address: String,
                                        utrn: String,
                                        purchaserName: String,
                                        status: UniversalStatus
                                      )

object SdltSubmittedReturnsViewModel {
  import UniversalStatus.*

  private val acceptableStatus : Seq[UniversalStatus] = Seq(SUBMITTED, SUBMITTED_NO_RECEIPT)

    def convertResponseToSubmittedView(submittedReturns: List[ReturnSummary]): List[SdltSubmittedReturnsViewModel] =

      for {
        rec    <- submittedReturns
        status <- fromString(rec.status)
        utrn   <- rec.utrn
        if acceptableStatus.contains(status)
      } yield SdltSubmittedReturnsViewModel(
        address = rec.address,
        utrn = utrn,
        purchaserName = rec.purchaserName,
        status = status
      )
}

case class PaginatedSubmittedReturnsViewModel(
                                          submittedReturnsViewModel: List[SdltSubmittedReturnsViewModel],
                                          paginator: Option[Pagination],
                                          paginationText: Option[String]
                                        )
