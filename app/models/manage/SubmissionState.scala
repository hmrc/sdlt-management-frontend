/*
 * Copyright 2026 HM Revenue & Customs
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

import models.responses.UniversalStatus

enum SubmissionState:
  case InProgress
  case AwaitingConfirmation
  case Submitted
  case SubmittedNoReceipt
  case SubmissionFailed
  case Resubmit

object SubmissionState:

  import UniversalStatus.*

  def fromUniversalStatus(status: UniversalStatus): SubmissionState =
    status match
      case IN_PROGRESS                      => InProgress
      case STARTED                          => Resubmit
      case ACCEPTED                         => AwaitingConfirmation
      case SUBMITTED                        => Submitted
      case SUBMITTED_NO_RECEIPT             => SubmittedNoReceipt
      case DEPARTMENTAL_ERROR | FATAL_ERROR => SubmissionFailed
      case _                                => InProgress