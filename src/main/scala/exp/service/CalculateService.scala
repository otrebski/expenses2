package exp.service

import com.udojava.evalex.Expression
import exp.model.Model.{CalculateRequest, CalculateResult, FailureCalculateResult, SuccessfulCalculateResult}

import scala.util.{Failure, Success, Try}

object CalculateService {

  def calculate(request:CalculateRequest):CalculateResult = {
    Try {
      new Expression(request.expression.trim.replace('\n', '+').replace(',', '.')).eval()
        .setScale(2, BigDecimal.RoundingMode.HALF_UP)
    } match {
      case Success(r) => SuccessfulCalculateResult(r, request.expression)
      case Failure(ex) => FailureCalculateResult(request.expression, ex.getMessage)
    }

  }
}
