package uk.co.elder.app.services.dataproviders

import com.github.tomakehurst.wiremock.WireMockServer
import uk.co.elder.app.service.dataproviders.YahooStockQuoteProvider
import org.scalatest.{FunSpec, Matchers}
import com.github.tomakehurst.wiremock.client.WireMock._

import scalaz.Maybe._
import scalaz.{-\/, \/-}
import uk.co.elder.app.model._
import uk.co.elder.app.model.{ElderError, Ticker}

class YahooStockQuoteProviderSpec extends FunSpec with Matchers {
  val wiremock = new WireMockServer(8080)
  val symbol = Ticker("SGP.L")
  wiremock.start()

  describe("Yahoo stock quote provider") {
    it("should return an error when the http call fails for any reason") {
      val task = YahooStockQuoteProvider.request(symbol, "localhost:9876")
      val result = task.unsafePerformSync
      result.isLeft shouldBe true
      result.swap.map {
        case ElderError(s, e) => {
          e.isEmpty shouldBe false
          s shouldEqual "Something went wrong with the HTTP request."
        }
      }
    }

    it("should return an error when the service returns a non 200 code") {
      stubFor(get(urlEqualTo(s"/table.csv?s=$symbol&ignore=.csv"))
        .willReturn(aResponse()
          .withStatus(404)
          .withHeader("Content-Type", "text/csv")))

      val task = YahooStockQuoteProvider.request(symbol, "localhost:8080")
      val result = task.unsafePerformSync
      result shouldBe -\/(ElderError("Call to yahoo failed. Response code = 404", Empty()))
    }

    it("should return an error when parsing the CSV fails") {
      stubFor(get(urlMatching(s"/table(.*)"))
        .willReturn(aResponse()
          .withStatus(200)
          .withHeader("Content-Type", "text/csv")
          .withBodyFile("test-quotes.csv")
        ))

      val task = YahooStockQuoteProvider.request(symbol, "localhost:8080")
      val result = task.unsafePerformSync
      result match {
        case -\/(e) => e shouldBe "Parsing the CSV returned from server failed. List(1, 2)"
        case \/-(_) => fail()
      }
    }

    it("should return an error when the service takes too long to respond") {
      stubFor(get(urlMatching(s"/table(.*)"))
        .willReturn(aResponse()
          .withStatus(200)
          .withHeader("Content-Type", "text/csv")
          .withBodyFile("test-quotes.csv").withFixedDelay(5000)
        ))

      val task = YahooStockQuoteProvider.request(symbol, "localhost:8080")
      val result = task.unsafePerformSync
      result match {
        case -\/(e) => e shouldBe "Parsing the CSV returned from server failed. List(1, 2)"
        case \/-(_) => fail()
      }
    }

    it("should return quotes when parsing the CSV is successful") {
      pending
    }
  }
}
