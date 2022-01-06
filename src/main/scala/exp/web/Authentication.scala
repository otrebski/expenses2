package exp.web

import cats.effect.Sync
import cats.syntax.all._
import sttp.tapir.model.UsernamePassword

object Authentication {

  case class User(name: String)
  case class AuthenticationToken(value: String)
  case class AuthenticationError(code: Int)


  def authLogic[F[_]: Sync](usernamePassword: UsernamePassword): F[Either[AuthenticationError, User]] = usernamePassword match {
    case UsernamePassword("a",Some("a")) => User("a").asRight[AuthenticationError].pure[F]
    case _ => AuthenticationError(1001).asLeft[User].pure[F]
  }

}
