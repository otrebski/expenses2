package exp.web

import cats.effect.IO
import cats.effect.Sync
import exp.model.Model
import exp.model.Model.Genre
import sttp.model.headers.WWWAuthenticateChallenge
import sttp.tapir.model.UsernamePassword
import cats.syntax.all._
import sttp.tapir.server.PartialServerEndpoint

object Endpoints {
  import io.circe.generic.auto._
  import sttp.tapir._
  import sttp.tapir.json.circe._
  import sttp.tapir.generic.auto._

  case class User(name: String)
  case class AuthenticationToken(value: String)
  case class AuthenticationError(code: Int)
  trait RequestError
  case class RequestAuthenticationError(wrapped: AuthenticationError) extends RequestError
  case class Other(msg: String) extends RequestError

  private def authLogic[F[_]: Sync](token: AuthenticationToken): F[Either[AuthenticationError, User]] =
    if (token.value == "berries") User("Papa Smurf").asRight[AuthenticationError].pure[F]
    else if (token.value == "smurf") User("Gargamel").asRight[AuthenticationError].pure[F]
    else AuthenticationError(1001).asLeft[User].pure[F]

  private val secureEndpoint = endpoint
    .securityIn(auth.bearer[String]().mapTo[AuthenticationToken])
//    .errorOut(plainBody[Int].mapTo[RequestError])
    .errorOut(plainBody[Int].map[AuthenticationError](i => AuthenticationError(i))(_.code))
//    .errorOut(plainBody([Int].m))
    .serverSecurityLogic(t => authLogic[IO](t))
    .mapErrorOut(RequestAuthenticationError)(_.wrapped)
    .errorOutVariant[RequestError](oneOfVariant(stringBody.mapTo[Other]))

  val book: Model.Book =
    Model.Book(title = "T", genre = Genre("genre", "genre desc"), year = 2021, author = Model.Author("John", Model.Country("PL")))

  val get = secureEndpoint
    .in("books" / "get")
    .out(jsonBody[Model.Book].example(book))

  val list = secureEndpoint
    .in("books" / "list")
    .out(jsonBody[List[Model.Book]].example(List(book)))

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
    list ::
    get ::
    Nil

}
