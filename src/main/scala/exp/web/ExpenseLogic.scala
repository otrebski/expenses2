package exp.web

import cats.{Applicative, Functor}
import exp.model.Model.{Date, Expense, ExpenseReport, ExpenseSummary, ExpenseToAdd, Note, Purpose}
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
    _ => idAndExpense => Functor[F].map(expenseService.edit(idAndExpense._2))(_.asRight[RequestError])
  }

  def monthSummary[F[_] : Applicative](expenseService: ExpenseService[F]): Authentication.User => Date => F[Either[RequestError, ExpenseReport]] = {
    _ =>
      date => {
        val list = List(
          Expense(Expense.Id(1), 10, Purpose("p1"), Note("x"), date),
          Expense(Expense.Id(2), 11, Purpose("p1"), Note("y"), date),
          Expense(Expense.Id(3), 20, Purpose("p2"), Note("ab"), date),
          Expense(Expense.Id(4), 40, Purpose("p2"), Note("ab"), date)
        )
        val summary = list
          .groupBy(_.purpose)
          .map {
            case (purpose, expenses) => ExpenseSummary(purpose.value, expenses.map(_.amount).sum)
          }.toList

        ExpenseReport(expenseSummary = summary, expenses = list).asRight[RequestError].pure[F]
      }
  }

}
