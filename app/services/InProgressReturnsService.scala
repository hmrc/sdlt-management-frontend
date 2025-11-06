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

package services

import com.google.inject.Singleton
import connectors.InProgressReturnsConnector
import models.responses.SdltReturnInfoResponse
import play.api.Logger
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewmodels.govuk.all.{ActionItemViewModel, FluentActionItem, FluentKey, FluentValue, KeyViewModel, SummaryListRowViewModel, SummaryListViewModel, ValueViewModel}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*


@Singleton
class InProgressReturnsService @Inject()(
                                        val inProgressReturnsConnector: InProgressReturnsConnector
                                    )(implicit ec: ExecutionContext) {

  def getSummaryList(storn: String)
                    (implicit messages: Messages): Future[Either[Throwable, List[SdltReturnInfoResponse] ]] = {
    Logger("application").info(s"[InProgressReturnsService][getAll] - get all returns")
    inProgressReturnsConnector.getAll(storn).map {
      case Right(data) =>
        Right(
          data
        )
      case Left(ex) =>
        Left(ex)
    }
  }

//  private def createSummeryList(data: List[SdltReturnInfoResponse])
//                          (implicit messages: Messages): Option[SummaryList] = {
//    val res = SummaryListViewModel(
//        rows = data.map { rowOfReturns =>
//          SummaryListRowViewModel(
//            key = KeyViewModel(
//              Text(rowOfReturns.purchaserName)
//            ).withCssClass("govuk-!-width-one-third govuk-!-font-weight-regular hmrc-summary-list__key"),
//
//            value = ValueViewModel(
//              Text(rowOfReturns.address + " data")
//            ).withCssClass("govuk-summary-list__value govuk-!-width-one-third"),
//
////            actions = Seq(
////              ActionItemViewModel(
////                Text(messages("site.change")),
////                "actionUrl"
////              ).withVisuallyHiddenText(rowOfReturns.returnId)
////            )
//
//          )
//        }
//      )
//    Some(res)
//  }

}
