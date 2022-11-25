package exp.web

import cats.effect.IO
import cats.syntax.all._
import exp.model.Model.CalculateRequest
import exp.model.Model.CalculateResult
import exp.service.CalculateService
import exp.web.CalculatePartialEndpoints.CalculationError
import sttp.tapir.model.UsernamePassword
import sttp.tapir.server.ServerEndpoint.Full

object CalculateLogic {
  val calculate: Authentication.User => CalculateRequest => IO[
    Either[CalculationError, CalculateResult]
  ] = _ =>
    request =>
      CalculateService.calculate(request).asRight[CalculationError].pure[IO]

}
