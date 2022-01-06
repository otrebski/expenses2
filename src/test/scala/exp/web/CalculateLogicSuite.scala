package exp.web

import exp.model.Model.{CalculateRequest, FailureCalculateResult, SuccessfulCalculateResult}
import munit.CatsEffectSuite
import cats.syntax.all._

class CalculateLogicSuite extends CatsEffectSuite {

  test("Correct expression get calculated") {
    CalculateLogic.calculate(Authentication.User("a"))(CalculateRequest("1+1"))
      .map(result => assertEquals(result, SuccessfulCalculateResult(2, "1+1").asRight))
  }

  test("Incorrect expression get error") {
    CalculateLogic.calculate(Authentication.User("a"))(CalculateRequest("1+x"))
      .map(result => assertEquals(result, FailureCalculateResult("1+x", "Unknown operator or function: x").asRight))
  }

}
