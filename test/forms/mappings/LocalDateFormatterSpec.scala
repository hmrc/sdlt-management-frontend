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

package forms.mappings

import generators.Generators
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.data.FormError
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages

import java.time.LocalDate

class LocalDateFormatterSpec
    extends AnyFreeSpec
    with Matchers
    with ScalaCheckPropertyChecks
    with Generators
    with Formatters {

  "Default parameters" - {
    "use empty args as default" in {
      implicit val messages: Messages = stubMessages()

      val formatter = new LocalDateFormatter(
        invalidKey = "invalidKey",
        allRequiredKey = "allRequiredKey",
        twoRequiredKey = "twoRequiredKey",
        requiredKey = "requiredKey"
      )

      formatter.bind(
        "field",
        Map("field.day" -> "1", "field.month" -> "3", "field.year" -> "2026")
      ) match {
        case Right(date) =>
          date mustBe LocalDate.of(2026, 3, 1)
        case Left(errors) =>
          fail(s"Unexpected validation errors: $errors")
      }
    }
  }
  "MonthFormatter" - {
    "bind returns error with empty args when value missing" in {
      val formatter = MonthFormatter("error.required")

      val result = formatter.bind("key", Map.empty)

      result match {
        case Left(errors) =>
          assert(errors.head.message == "error.required")
          assert(errors.head.args.isEmpty)
        case _ => fail("Expected Left")
      }
    }
    "unbind returns error with empty args when value missing" in {
      val formatter = MonthFormatter("error.required")

      val result = formatter.unbind("key", 2)

      result mustBe Map("key" -> "2")
    }
  }
}
