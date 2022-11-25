package exp.service

import scala.util.Failure
import scala.util.Success
import scala.util.Try

import com.udojava.evalex.Expression
import exp.model.Model.CalculateRequest
import exp.model.Model.CalculateResult
import exp.model.Model.FailureCalculateResult
import exp.model.Model.SuccessfulCalculateResult

object CalculateService {

  def calculate(request: CalculateRequest): CalculateResult =
    Try {
      new Expression(
        request.expression.trim.replace('\n', '+').replace(',', '.')
      ).eval()
        .setScale(2, BigDecimal.RoundingMode.HALF_UP)
    } match {
      case Success(r) => SuccessfulCalculateResult(r, request.expression)
      case Failure(ex) =>
        FailureCalculateResult(request.expression, ex.getMessage)
    }

}
