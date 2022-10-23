package exp.service

import cats.Applicative
import exp.model.Model.{Date, Expense, ExpenseSummary, ExpenseToAdd, Purpose, *}
import exp.model.Model.Purpose.{apply, encoder}
import cats.implicits.*

trait ExpenseService[F[_]] {

  def find(id: Expense.Id): F[Option[Expense]]

  def add(toAdd: ExpenseToAdd): F[Expense]

  def edit(expense: Expense): F[Expense]

  def delete(id: Expense.Id): F[Unit]

  def purposes(purpose: Purpose): F[List[Purpose]]

  def purposes(): F[List[Purpose]]

  def summary(since: Date, until: Date): F[List[ExpenseSummary]]

  def search(since: Date, until: Date, purpose: Option[Purpose]= None, note: Option[Note] = None): F[List[ExpenseSummary]]
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
        val l: List[Purpose] = List(Purpose("abcd"), Purpose("asdf"), Purpose("asdcv"))
        l.filter { p => p.contains(purpose) }
          //          .map(Purpose(_))
          .pure[F]
      }

      override def purposes(): F[List[Purpose]] = List(Purpose("abcd"), Purpose("asdf"), Purpose("asdcv"))
        //        .map(Purpose(_))
        .pure[F]

      override def summary(since: Date, until: Date): F[List[ExpenseSummary]] =
        data
          .values
          .filter(_.date >= since)
          .filter(_.date <= until)
          .groupBy(_.purpose)
          .view.mapValues(_.map(_.amount).sum)
          .map { case (purpose, amount) => ExpenseSummary(purpose = purpose, amount = amount) }
          .toList
          .pure[F]

      override def search(since: Date, until: Date, purpose: Option[Purpose], note: Option[Note]): F[List[ExpenseSummary]] =
        data
          .values
          .filter(_.date >= since)
          .filter(_.date <= until)
          .filter(expense => purpose.forall(p => p == expense.purpose))
          .filter(expense => note.forall(n => n == expense.note))
          .groupBy(_.purpose)
          .view.mapValues(_.map(_.amount).sum)
          .map { case (purpose, amount) => ExpenseSummary(purpose = purpose, amount = amount) }
          .toList
          .pure[F]
    }

}
