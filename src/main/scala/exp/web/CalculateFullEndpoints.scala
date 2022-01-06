package exp.web

import cats.effect.IO
import exp.web.CalculatePartialEndpoints.CalculationError
import exp.model.Model.{CalculateRequest, CalculateResult}
import sttp.tapir.model.UsernamePassword
import sttp.tapir.server.ServerEndpoint.Full

object CalculateFullEndpoints {
  val calculate: Full[UsernamePassword, Authentication.User, CalculateRequest, CalculationError, CalculateResult, Any, IO] = CalculatePartialEndpoints.calculate
    .serverLogic(CalculateLogic.calculate)

}
