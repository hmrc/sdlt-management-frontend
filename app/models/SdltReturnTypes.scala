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

package models

// Naming conversion => pageType | status
enum SdltReturnTypes:
  case IN_PROGRESS_RETURNS
  case SUBMITTED_SUBMITTED_RETURNS
  case SUBMITTED_NO_RECEIPT_RETURNS
  case IN_PROGRESS_RETURNS_DUR_FOR_DELETION
  case SUBMITTED_RETURNS_DUR_FOR_DELETION