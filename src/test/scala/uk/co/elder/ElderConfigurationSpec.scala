package uk.co.elder

import com.typesafe.config.ConfigFactory
import org.scalatest.{FunSpec, Matchers}

import scalaz.Maybe
import scalaz.Maybe.Just
import scalaz.syntax.validation._

class ElderConfigurationSpec extends FunSpec with Matchers {
  describe("Yahoo configuration") {
    it("should validate and return the port if the host is localhost") {
      val config = ConfigFactory.parseString("{app { yahooFinance { host = \"localhost\", port = 0 } }}")
      val result = ElderConfiguration.loadYahooConfig(config)
      result.isFailure shouldBe true
    }

    it("should return a success if the port is set and the host is localhost") {
      val config = ConfigFactory.parseString("{app { yahooFinance { host = \"localhost\", port = 1234 } }}")
      val result = ElderConfiguration.loadYahooConfig(config)
      result.isSuccess shouldBe true
      result.toOption match {
        case Some(r) => {
          r.host shouldEqual "localhost"
          r.port shouldEqual Just(1234)
        }
      }
    }

    it("should not validate the port if the host is not localhost") {
      val config = ConfigFactory.parseString("{app { yahooFinance { host = \"www.yahoo.com\", port = 0 } }}")
      val result = ElderConfiguration.loadYahooConfig(config)
      result.isSuccess shouldBe true
    }

    it("should handle port not being set if host is not localhost") {
      val config = ConfigFactory.parseString("{app { yahooFinance { host = \"www.yahoo.com\" } }}")
      val result = ElderConfiguration.loadYahooConfig(config)
      result.isSuccess shouldBe true
    }
  }
  describe("ElderConfiguration.validateProperty") {
    it("returns the exception message if running the function to get the property from the config fails") {
      val result = ElderConfiguration.validateProperty[String, String](throw new Exception("Oh Dear"))(Maybe.empty)
      result.fold(
        e => e.head shouldBe "Oh Dear",
        r => fail()
      )
    }

    it("Doesn't do any validation checks if the property cant be parsed") {
      val result = ElderConfiguration.validateProperty[Int, Int](throw new Exception("Exception Message"))(
        Just((e:Int) => if (e == 1) "This should happen".failureNel else e.successNel)
      )

      result.fold(
        e => {
          e.size shouldBe 1
          e.head shouldBe "Exception Message"
        },
        r => fail()
      )
    }

    it("should output the property completely unchanged if validation succeeds") {
      val result = ElderConfiguration.validateProperty(2)(
        Just((e: Int) => if (e == 1) "This should happen".failureNel else e.successNel)
      )

      result.fold(
        e => fail(),
        _ shouldBe 2
      )
    }
  }
}
