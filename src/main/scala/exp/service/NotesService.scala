package exp.service

import cats.Applicative
import exp.model.Model.{Note, Purpose, Suggestion}
import cats.syntax.all._

trait NotesService[F[_]] {
  def notesSuggestions(purpose: Purpose, note: Note): F[List[Suggestion]]
}

object NotesService {
  def mockInstance[F[_] : Applicative]() = new NotesService[F]() {
    override def notesSuggestions(purpose: Purpose, note: Note): F[List[Suggestion]] = {
      List(Suggestion(note, 5)).pure[F]
    }
  }
}
