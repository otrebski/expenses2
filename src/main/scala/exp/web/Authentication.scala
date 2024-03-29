package exp.web

import cats.effect.IO
import cats.effect.Sync
import cats.syntax.all._
import sttp.tapir.auth
import sttp.tapir.endpoint
import sttp.tapir.model.UsernamePassword
import sttp.tapir.oneOfVariant
import sttp.tapir.plainBody
import sttp.tapir.stringBody

object Authentication {

  case class User(name: String)
  case class AuthenticationToken(value: String)
  case class AuthenticationError(code: Int)

  def authLogic[F[_]: Sync](
      usernamePassword: UsernamePassword
  ): F[Either[AuthenticationError, User]] = usernamePassword match {
    case UsernamePassword("a", Some("a")) =>
      User("a").asRight[AuthenticationError].pure[F]
    case _ => AuthenticationError(1001).asLeft[User].pure[F]
  }

  val secureEndpoint = endpoint
    .securityIn(auth.basic[UsernamePassword]())
    .errorOut(
      plainBody[Int]
        .map[AuthenticationError](i => AuthenticationError(i))(_.code)
    )
    .serverSecurityLogic(t => Authentication.authLogic[IO](t))

}
