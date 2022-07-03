package exp.service

import cats.Applicative
import exp.model.Model.{Date, Expense, ExpenseSummary, ExpenseToAdd, Purpose}
import cats.implicits.*

trait ExpenseService[F[_]] {

  def find(id: Expense.Id): F[Option[Expense]]

  def add(toAdd: ExpenseToAdd): F[Expense]

  def edit(expense: Expense): F[Expense]

  def delete(id: Expense.Id): F[Unit]

  def purposes(purpose: Purpose): F[List[Purpose]]

  def purposes(): F[List[Purpose]]

  def summary(since: Date, until: Date): F[List[ExpenseSummary]]
}

object ExpenseService {

  def mockInstance[F[_] : Applicative]() =
    new ExpenseService[F] {
      var data = Map.empty[Expense.Id, Expense]

      override def find(id: Expense.Id): F[Option[Expense]] = data.get(id).pure[F]

      override def add(toAdd: ExpenseToAdd): F[Expense] = {
        val id = Expense.Id(data.size)
        val added = Expense(id, toAdd.amount, toAdd.purpose, toAdd.note, toAdd.date)
        data = data.updated(id, added)
        added.pure[F]
      }

      override def edit(expense: Expense): F[Expense] = {
        data = data.updated(expense.id, expense)
        expense.pure[F]
      }

      override def delete(id: Expense.Id): F[Unit] = {
        data = data.removed(id)
        ().pure[F]
      }

      override def purposes(purpose: Purpose): F[List[Purpose]] = {
        List("abcd", "asdf", "asdcv")
          .filter { s =>
            println(s"Filtering $s (${s.toLowerCase.contains(purpose.value.toLowerCase)})")
            s.toLowerCase.contains(purpose.value.toLowerCase)
          }
          .map(Purpose(_))
          .pure[F]
      }

      override def purposes(): F[List[Purpose]] = List("abcd", "asdf", "asdcv")
        .map(Purpose(_))
        .pure[F]

      override def summary(since: Date, until: Date): F[List[ExpenseSummary]] =
        data
          .values
          .filter(_.date >= since)
          .filter(_.date <= until)
          .groupBy(_.purpose)
          .view.mapValues(_.map(_.amount).sum)
          .map { case (purpose, amount) => ExpenseSummary(purpose = purpose.value, amount = amount) }
          .toList
          .pure[F]
    }

}
