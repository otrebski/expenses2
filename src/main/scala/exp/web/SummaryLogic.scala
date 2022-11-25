package exp.web

import cats.Functor
import cats.implicits._
import exp.model.Model.Date
import exp.model.Model.ExpenseSummary
import exp.service.ExpenseService
import exp.web.SummaryPartialEndpoints.Other
import exp.web.SummaryPartialEndpoints.RequestError

object SummaryLogic {

  def summary[F[_]: Functor](
      expenseService: ExpenseService[F]
  ): Authentication.User => (Date, Date) => F[
    Either[RequestError, List[ExpenseSummary]]
  ] = { user =>
    { case (since, until) =>
      Functor[F].map(expenseService.summary(since, until))(list =>
        Right[RequestError, List[ExpenseSummary]](list)
      )

    }
  }
}
