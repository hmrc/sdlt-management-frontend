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

package connectors

import base.SpecBase
import connectors.JsErrorOps.*
import play.api.libs.json.{JsError, JsPath, JsonValidationError}

class JsErrorOpsSpec extends SpecBase {

  "JsErrorOps.prettyPrint" - {

    "format a single path with a single error message" in {
      val error = JsError(JsPath \ "fieldName", JsonValidationError("error.required"))
      error.prettyPrint() mustBe "/fieldName: [error.required]"
    }

    "format a single path with multiple error messages" in {
      val error = JsError(
        Seq(
          (JsPath \ "fieldName") -> Seq(
            JsonValidationError("error.required"),
            JsonValidationError("error.invalid")
          )
        )
      )
      error.prettyPrint() mustBe "/fieldName: [error.required,error.invalid]"
    }

    "format multiple paths separated by semicolons" in {
      val error = JsError(
        Seq(
          (JsPath \ "field1") -> Seq(JsonValidationError("error.required")),
          (JsPath \ "field2") -> Seq(JsonValidationError("error.invalid"))
        )
      )
      val result = error.prettyPrint()
      result must include("/field1: [error.required]")
      result must include("/field2: [error.invalid]")
      result must include("; ")
    }

    "format a nested path" in {
      val error = JsError((JsPath \ "outer" \ "inner") -> JsonValidationError("error.missing"))
      error.prettyPrint() mustBe "/outer/inner: [error.missing]"
    }

    "format an empty JsError (no errors)" in {
      val error  = JsError(Seq.empty)
      val result = error.prettyPrint()
      result mustBe ""
    }
  }
}
