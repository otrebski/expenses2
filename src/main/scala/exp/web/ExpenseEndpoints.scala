package exp.web

import cats.effect.IO
import cats.effect.Sync
import cats.syntax.all._
import exp.model.Model
import io.circe.generic.auto._
import sttp.tapir._
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._
import sttp.tapir.model.UsernamePassword

object ExpenseEndpoints {


  case class User(name: String)
  case class AuthenticationToken(value: String)
  case class AuthenticationError(code: Int)
  trait RequestError
  case class RequestAuthenticationError(wrapped: AuthenticationError) extends RequestError
  case class Other(msg: String) extends RequestError

  private def authLogic[F[_]: Sync](usernamePassword: UsernamePassword): F[Either[AuthenticationError, User]] = usernamePassword match {
    case UsernamePassword("a",Some("a")) => User("a").asRight[AuthenticationError].pure[F]
    case _ => AuthenticationError(1001).asLeft[User].pure[F]
  }


  private val secureEndpoint = endpoint
    .securityIn(auth.basic[UsernamePassword]())
    .errorOut(plainBody[Int].map[AuthenticationError](i => AuthenticationError(i))(_.code))
    .serverSecurityLogic(t => authLogic[IO](t))
    .mapErrorOut(RequestAuthenticationError)(_.wrapped)
    .errorOutVariant[RequestError](oneOfVariant(stringBody.mapTo[Other]))


  //api/expenses/id/12886
  private val baseExpenseEndpoint = secureEndpoint
    .in("api" / "expenses")

  val getExpense = baseExpenseEndpoint
    .get
    .in("id" / path[Long]("id"))
    .out(jsonBody[Model.Expense])

  val addExpense = baseExpenseEndpoint
    .put
    .in(jsonBody[Model.ExpenseToAdd])
    .out(jsonBody[Model.Expense])

  val editExpense = baseExpenseEndpoint
    .post
    .in("id" / path[Long]("id"))
    .in(jsonBody[Model.Expense])
    .out(jsonBody[Model.Expense])

  val deleteExpense = baseExpenseEndpoint
    .delete
    .in("id" / path[Long]("id"))

  val all = getExpense ::
    addExpense ::
    editExpense ::
    deleteExpense ::
    Nil

}
