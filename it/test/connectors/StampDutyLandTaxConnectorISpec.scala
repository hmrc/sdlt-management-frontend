package connectors

import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, equalTo, get, post, stubFor, urlPathEqualTo}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers.should
import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec
import play.api.http.Status.*
import play.api.libs.json.{JsBoolean, Json}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.HttpClientV2

import scala.concurrent.ExecutionContext
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers
import scala.concurrent.Future

class StampDutyLandTaxConnectorISpec extends AsyncFlatSpec {

  private val storn = "STN001"
  
  val http: HttpClientV2
  val config: ServicesConfig
  implicit val hc: HeaderCarrier

  val connector = new StampDutyLandTaxConnector(http, config)

  // Example async test
  "StampConnector" should "successfully fetch returns from backend" in {
    connector.getReturns(storn).map { result =>
      result.map(_.returnSummaryCount) shouldBe BigDecimal(1)
      result.map(_.returnSummaryCount) should contain()
      
    }
  }

  it should "fail to fetch returns for invalid ID" in {
    recoverToSucceededIf[RuntimeException] {
      connector.getReturns("")
    }
  }
}
