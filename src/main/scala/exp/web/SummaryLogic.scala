package exp.web

import cats.Functor
import exp.model.Model.{Date, ExpenseSummary}
import exp.service.ExpenseService
import exp.web.SummaryPartialEndpoints.{Other, RequestError}

import cats.implicits._

object SummaryLogic {

  def summary[F[_] : Functor](expenseService: ExpenseService[F]):
  Authentication.User => (Date, Date) => F[Either[RequestError, List[ExpenseSummary]]] = {
    user => {
      case (since, until) =>
        Functor[F].map(expenseService.summary(since, until))(list => Right[RequestError, List[ExpenseSummary]](list))

    }
  }
}
