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
import models.Enumerable
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.data.FormError

class FormattersSpec
    extends AnyFreeSpec
    with Matchers
    with ScalaCheckPropertyChecks
    with Generators
    with Formatters {

  sealed trait TestEnum
  case object One extends TestEnum
  case object Two extends TestEnum

  implicit val testEnumerable: Enumerable[TestEnum] =
    new Enumerable[TestEnum] {
      val values: Seq[TestEnum] =
        Seq(One, Two)
      def withName(str: String): Option[TestEnum] =
        values.find(_.toString == str)
    }

  "stringFormatter" - {
    "bind returns error with empty args when value missing" in {
      val formatter = stringFormatter("error.required")

      val result = formatter.bind("key", Map.empty)

      result match {
        case Left(errors) =>
          assert(errors.head.message == "error.required")
          assert(errors.head.args.isEmpty)
        case _ => fail("Expected Left")
      }
    }
  }

  "booleanFormatter" - {
    "bind returns error with empty args when value missing" in {
      val formatter = booleanFormatter("requiredKey", "invalidKey")

      val result = formatter.bind("key", Map.empty)

      result match {
        case Left(errors) =>
          assert(errors.head.message == "requiredKey")
          assert(errors.head.args.isEmpty)
        case _ => fail("Expected Left")
      }
    }
  }

  "intFormatter" - {
    "bind returns error with empty args when value missing" in {
      val formatter = intFormatter("requiredKey", "wholeNumKey", "nonNumKey")

      val result = formatter.bind("key", Map.empty)

      result match {
        case Left(errors) =>
          assert(errors.head.message == "requiredKey")
          assert(errors.head.args.isEmpty)
        case _ => fail("Expected Left")
      }
    }
    "bind returns whole number error when value is a decimal" in {
      val formatter = intFormatter("requiredKey", "wholeNumKey", "nonNumKey")

      val result = formatter.bind("key", Map("key" -> "1.5"))

      result match {
        case Left(errors) =>
          assert(errors.head.message == "wholeNumKey")
        case _ =>
          fail("Expected Left for decimal input")
      }
    }
  }

  "enumerableFormatter" - {
    "bind returns error with empty args when value missing" in {
      val formatter = enumerableFormatter[TestEnum]("requiredKey", "invalidKey")

      val result = formatter.bind("key", Map.empty)

      result match {
        case Left(errors) =>
          assert(errors.head.message == "requiredKey")
          assert(errors.head.args.isEmpty)
        case _ => fail("Expected Left")
      }
    }

    "unbind returns error with empty args when value missing" in {
      val formatter =
        enumerableFormatter[TestEnum]("required", "invalid")

      val result =
        formatter.unbind("key", One)

      result mustBe Map("key" -> "One")
    }
  }

  "currencyFormatter" - {
    "bind returns error with empty args when value missing" in {
      val formatter =
        currencyFormatter("requiredKey", "invalidNumKey", "nonNumKey")

      val result = formatter.bind("key", Map.empty)

      result match {
        case Left(errors) =>
          assert(errors.head.message == "requiredKey")
          assert(errors.head.args.isEmpty)
        case _ => fail("Expected Left")
      }
    }
    "bind returns FormError with nonNumericKey when value is not numeric" in {
      val formatter = currencyFormatter(
        "requiredKey",
        "invalidNumKey",
        "nonNumKey",
        Seq("args")
      )

      val result =
        formatter.bind("key", Map("key" -> "£"))

      result mustBe Left(
        Seq(FormError("key", "nonNumKey", Seq("args")))
      )
    }
  }

}
