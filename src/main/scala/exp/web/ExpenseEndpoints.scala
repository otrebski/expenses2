package exp.web

import exp.model.Model
import exp.web.Authentication.AuthenticationError
import io.circe.generic.auto._
import sttp.tapir._
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._

object ExpenseEndpoints {

  trait RequestError
  case class RequestAuthenticationError(wrapped: AuthenticationError) extends RequestError
  case class Other(msg: String) extends RequestError

  private val secureEndpoint = Authentication.secureEndpoint
    .mapErrorOut(RequestAuthenticationError)(_.wrapped)
    .errorOutVariant[RequestError](oneOfVariant(stringBody.mapTo[Other]))


  //api/expenses/id/12886
  private val baseExpenseEndpoint = secureEndpoint
    .in("api" / "expenses")

  val get = baseExpenseEndpoint
    .get
    .in("id" / path[Long]("id"))
    .out(jsonBody[Model.Expense])

  val add = baseExpenseEndpoint
    .put
    .in(jsonBody[Model.ExpenseToAdd])
    .out(jsonBody[Model.Expense])

  val edit = baseExpenseEndpoint
    .post
    .in("id" / path[Long]("id"))
    .in(jsonBody[Model.Expense])
    .out(jsonBody[Model.Expense])

  val delete = baseExpenseEndpoint
    .delete
    .in("id" / path[Long]("id"))

  val all = get ::
    add ::
    edit ::
    delete ::
    Nil

}
