package services

import org.scalatest.EitherValues
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext

class InProgressReturnsServiceSpec extends AnyWordSpec
  with ScalaFutures
  with Matchers
  with EitherValues {

  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val ex: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  trait Fixture {
  }

}
