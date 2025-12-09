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

package utils

object PageUrlSelector {

  val inProgressUrlSelector: Int => String = (pageIndex: Int) => controllers.manage.routes.InProgressReturnsController.onPageLoad(Some(pageIndex)).url

  lazy val dueForDeletionInProgressUrlSelector: Option[Int] => Int => String =
    (submittedIndex: Option[Int]) => (index: Int) =>
      s"${controllers.manage.routes.DueForDeletionReturnsController.onPageLoad(Some(index), submittedIndex).url}#in-progress"

  lazy val dueForDeletionSubmittedUrlSelector: Option[Int] => Int => String =
    (inProgressIndex: Option[Int]) => (index: Int) =>
      s"${controllers.manage.routes.DueForDeletionReturnsController.onPageLoad(inProgressIndex, Some(index)).url}#submitted"

  val submittedUrlSelector: Int => String = (paginationIndex: Int) => controllers.manage.routes.SubmittedReturnsController.onPageLoad(Some(paginationIndex)).url
}