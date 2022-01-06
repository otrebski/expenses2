package exp.web

import cats.Functor
import exp.model.Model.{Expense, ExpenseToAdd}
import exp.service.ExpenseService
import exp.web.ExpensePartialEndpoints.{Other, RequestError}
import cats.syntax.all._

object ExpenseLogic {

  def get[F[_] : Functor](expenseService: ExpenseService[F]): Authentication.User => Long => F[Either[RequestError, Expense]] = {
    _ => id => Functor[F].map(expenseService.find(Expense.Id(id)))(_.toRight(Other("Not found")))
  }
  def add[F[_] : Functor](expenseService: ExpenseService[F]): Authentication.User => ExpenseToAdd => F[Either[RequestError, Expense]] = {
    _ => expense => Functor[F].map(expenseService.add(expense))(_.asRight[RequestError])
  }

  def delete[F[_] : Functor](expenseService: ExpenseService[F]): Authentication.User => Long => F[Either[RequestError, Unit]] = {
    _ => id => Functor[F].map(expenseService.delete(Expense.Id(id)))(_.asRight[RequestError])
  }

  def edit[F[_] : Functor](expenseService: ExpenseService[F]): Authentication.User => ((Long, Expense)) => F[Either[RequestError, Expense]] = {
    _ => (idAndExpense) => Functor[F].map(expenseService.edit(idAndExpense._2))(_.asRight[RequestError])
  }

}
