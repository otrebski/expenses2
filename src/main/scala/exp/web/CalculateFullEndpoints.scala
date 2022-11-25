package exp.web

import cats.effect.IO
import exp.model.Model.CalculateRequest
import exp.model.Model.CalculateResult
import exp.web.CalculatePartialEndpoints.CalculationError
import sttp.tapir.model.UsernamePassword
import sttp.tapir.server.ServerEndpoint.Full

object CalculateFullEndpoints {
  val calculate: Full[
    UsernamePassword,
    Authentication.User,
    CalculateRequest,
    CalculationError,
    CalculateResult,
    Any,
    IO
  ] = CalculatePartialEndpoints.calculate
    .serverLogic(CalculateLogic.calculate)

}
