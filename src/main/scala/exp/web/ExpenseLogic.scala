package exp.web

import cats.Applicative
import cats.Functor
import cats.implicits._
import exp.model.Model.Date
import exp.model.Model.Expense
import exp.model.Model.ExpenseReport
import exp.model.Model.ExpenseSummary
import exp.model.Model.ExpenseToAdd
import exp.model.Model.Note
import exp.model.Model.NotesSuggestionRequest
import exp.model.Model.NotesSuggestionResponse
import exp.model.Model.Purpose
import exp.service.ExpenseService
import exp.service.NotesService
import exp.web.ExpensePartialEndpoints.Other
import exp.web.ExpensePartialEndpoints.RequestError

object ExpenseLogic {

  def get[F[_]: Functor](
      expenseService: ExpenseService[F]
  ): Authentication.User => Long => F[Either[RequestError, Expense]] = {
    _ => id =>
      Functor[F].map(expenseService.find(Expense.Id(id)))(
        _.toRight(Other("Not found"))
      )
  }

  def add[F[_]: Functor](
      expenseService: ExpenseService[F]
  ): Authentication.User => ExpenseToAdd => F[Either[RequestError, Expense]] = {
    _ => expense => expenseService.add(expense).map(_.asRight[RequestError])

  }

  def delete[F[_]: Functor](
      expenseService: ExpenseService[F]
  ): Authentication.User => Long => F[Either[RequestError, Unit]] = { _ => id =>
    Functor[F].map(expenseService.delete(Expense.Id(id)))(
      _.asRight[RequestError]
    )
  }

  def edit[F[_]: Functor](
      expenseService: ExpenseService[F]
  ): Authentication.User => ((Long, Expense)) => F[
    Either[RequestError, Expense]
  ] = { _ => idAndExpense =>
    Functor[F].map(expenseService.edit(idAndExpense._2))(
      _.asRight[RequestError]
    )
  }

  def monthSummary[F[_]: Applicative](
      expenseService: ExpenseService[F]
  ): Authentication.User => Date => F[Either[RequestError, ExpenseReport]] = {
    _ => date =>
      {
        val list = List(
          Expense(Expense.Id(1), 10, Purpose("p1"), Note("x"), date),
          Expense(Expense.Id(2), 11, Purpose("p1"), Note("y"), date),
          Expense(Expense.Id(3), 20, Purpose("p2"), Note("ab"), date),
          Expense(Expense.Id(4), 40, Purpose("p2"), Note("ab"), date)
        )
        val summary = list
          .groupBy(_.purpose)
          .map { case (purpose, expenses) =>
            ExpenseSummary(purpose, expenses.map(_.amount).sum)
          }
          .toList

        ExpenseReport(expenseSummary = summary, expenses = list)
          .asRight[RequestError]
          .pure[F]
      }
  }

  def notes[F[_]: Applicative](
      notesService: NotesService[F]
  ): Authentication.User => NotesSuggestionRequest => F[
    Either[RequestError, NotesSuggestionResponse]
  ] = { _ => request =>
    notesService
      .notesSuggestions(Purpose(request.purpose), Note(request.note))
      .map(list => NotesSuggestionResponse(list).asRight[RequestError])
  }

  def purposes[F[_]: Functor](
      expenseService: ExpenseService[F]
  ): Authentication.User => Purpose => F[Either[RequestError, List[Purpose]]] =
    _ =>
      purpose => expenseService.purposes(purpose).map(_.asRight[RequestError])

  def allPurposes[F[_]: Functor](
      expenseService: ExpenseService[F]
  ): Authentication.User => Unit => F[Either[RequestError, List[Purpose]]] =
    _ => _ => expenseService.purposes().map(_.asRight[RequestError])

}
