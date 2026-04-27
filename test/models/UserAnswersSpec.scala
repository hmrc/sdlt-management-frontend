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

package models

import play.api.libs.json._
import scala.util.{Try, Failure, Success}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import queries.Settable

class UserAnswersSpec extends AnyFreeSpec with Matchers {

  class TestPage[A](val path: JsPath, cleanupResult: Try[UserAnswers] = null)
      extends Settable[A] {
    override def cleanup(value: Option[A], ua: UserAnswers): Try[UserAnswers] =
      if (cleanupResult != null) cleanupResult else Success(ua)
  }

  "set" - {
    "must return Failure(JsResultException) when JsError occurs" in {

      val userAnswers = UserAnswers(
        id = "testId",
        data = Json.obj("existing" -> "value")
      )
      val page = new Settable[String] {
        override def path: JsPath = __ \ "existing" \ "data"
      }

      val result: Try[UserAnswers] = userAnswers.set(page, "newValue")

      result match {
        case Failure(ex: JsResultException) =>
          ex.getMessage must include("cannot set a key on \"value\"")
        case _ =>
          fail("Expected Failure(JsResultException)")
      }
    }
  }

  "remove" - {
    "remove an existing key successfully" in {
      val userAnswers = UserAnswers("id", Json.obj("key" -> "value"))
      val page = new TestPage[String](__ \ "key")

      val result: Try[UserAnswers] = userAnswers.remove(page)

      result match {
        case Success(updated) =>
          updated.data mustBe Json.obj()
        case _ =>
          fail("Expected Success(UserAnswers)")
      }
    }

    "return unchanged data if key does not exist" in {
      val userAnswers = UserAnswers("id", Json.obj("key" -> "value"))
      val page = new TestPage[String](__ \ "missing")

      val result: Try[UserAnswers] = userAnswers.remove(page)

      result mustBe Success(userAnswers)
    }

    "return what cleanup returns" in {
      val ua = UserAnswers("id", Json.obj("key" -> "value"))
      val cleaned = UserAnswers("id", Json.obj("key" -> "value"))

      val page =
        new TestPage[String](__ \ "key", cleanupResult = Success(cleaned))

      val result = ua.remove(page)

      result mustBe Success(cleaned)
    }

    "propagate Failure from cleanup" in {
      val ua = UserAnswers("id", Json.obj("key" -> "value"))
      val failure = Failure(new RuntimeException("cleanup failed"))
      val page = new TestPage[String](__ \ "key", cleanupResult = failure)

      val result = ua.remove(page)

      result mustBe failure
    }
  }
}
