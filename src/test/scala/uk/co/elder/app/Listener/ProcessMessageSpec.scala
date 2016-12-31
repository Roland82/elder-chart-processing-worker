package uk.co.elder.app.Listener

import uk.co.elder.listener.ProcessMessage
import org.scalatest.{FunSpec, Matchers}

import scalaz.{-\/, \/, \/-}

class ProcessMessageSpec  extends FunSpec with Matchers {
  describe("Test") {
    val task = ProcessMessage.handleMessage("{ \"name\": \"refresh\",  \"symbol\": \"UTW.L\"}")
    val result = task.unsafePerformSync
    result match {
      case -\/(e) => println(e);
      case \/-(e) => fail("wfwefawe");
    }
  }
}
