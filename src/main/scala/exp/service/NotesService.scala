package exp.service

import cats.Applicative
import cats.syntax.all._
import exp.model.Model.Note
import exp.model.Model.Purpose
import exp.model.Model.Suggestion

trait NotesService[F[_]] {
  def notesSuggestions(purpose: Purpose, note: Note): F[List[Suggestion]]
}

object NotesService {
  def mockInstance[F[_]: Applicative]() = new NotesService[F]() {
    override def notesSuggestions(
        purpose: Purpose,
        note: Note
    ): F[List[Suggestion]] =
      List(Suggestion(note, 5)).pure[F]
  }
}
