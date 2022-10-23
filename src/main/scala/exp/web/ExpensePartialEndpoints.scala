package exp.web

import cats.effect.IO
import exp.model.Model
import exp.model.Model.{Date, ExpenseReport, NotesSuggestionRequest, NotesSuggestionResponse, Purpose}
import exp.model.Model.Purpose.encoder
import exp.model.Model.Purpose.decoder
import exp.model.Model.Note.decoder
import exp.model.Model.Note.decoder
import exp.model.Model.Expense.codec
import exp.model.Model.ExpenseToAdd.codec
import exp.web.Authentication.AuthenticationError
import sttp.tapir._
//import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._
import sttp.tapir.model.UsernamePassword
import sttp.tapir.server.PartialServerEndpoint
import sttp.tapir.CodecFormat.TextPlain
import TapirCodecs._

object ExpensePartialEndpoints {

  trait RequestError

  case class RequestAuthenticationError(wrapped: AuthenticationError) extends RequestError

  case class Other(msg: String) extends RequestError


  private val secureEndpoint = Authentication.secureEndpoint
    .mapErrorOut(RequestAuthenticationError.apply)(_.wrapped)
    .errorOutVariant[RequestError](oneOfVariant(stringBody.mapTo[Other]))


  private val apiEndpoint = secureEndpoint.in("api")

  private val baseExpenseEndpoint = apiEndpoint.in("expenses")

  val get: PartialServerEndpoint[UsernamePassword, Authentication.User, Long, RequestError, Model.Expense, Any, IO] = baseExpenseEndpoint
    .get
    .in("id" / path[Long]("id"))
    .out(jsonBody[Model.Expense])

  val add: PartialServerEndpoint[UsernamePassword, Authentication.User, Model.ExpenseToAdd, RequestError, Model.Expense, Any, IO] = baseExpenseEndpoint
    .put
    .in(jsonBody[Model.ExpenseToAdd])
    .out(jsonBody[Model.Expense])

  val addList: PartialServerEndpoint[UsernamePassword, Authentication.User, List[Model.ExpenseToAdd], RequestError, List[Model.Expense], Any, IO] = baseExpenseEndpoint
    .put
    .in(jsonBody[List[Model.ExpenseToAdd]])
    .out(jsonBody[List[Model.Expense]])


  val edit: PartialServerEndpoint[UsernamePassword, Authentication.User, (Long, Model.Expense), RequestError, Model.Expense, Any, IO] = baseExpenseEndpoint
    .post
    .in("id" / path[Long]("id"))
    .in(jsonBody[Model.Expense])
    .out(jsonBody[Model.Expense])

  val delete: PartialServerEndpoint[UsernamePassword, Authentication.User, Long, RequestError, Unit, Any, IO] = baseExpenseEndpoint
    .delete
    .in("id" / path[Long]("id"))

  val singleExpenseEndpoints = List(get, add, addList, edit, delete)

  val listInterval: PartialServerEndpoint[UsernamePassword, Authentication.User, Date, RequestError, ExpenseReport, Any, IO] = baseExpenseEndpoint
    .in("date")
    .in(path[Date]("date"))
    .out(jsonBody[ExpenseReport])


  val notes: PartialServerEndpoint[UsernamePassword, Authentication.User, NotesSuggestionRequest, RequestError, NotesSuggestionResponse, Any, IO] = apiEndpoint
    .post
    .in("notes")
    .in(jsonBody[NotesSuggestionRequest])
    .out(jsonBody[NotesSuggestionResponse])

  val allPurposes: PartialServerEndpoint[UsernamePassword, Authentication.User, Unit, RequestError, List[Purpose], Any, IO] = apiEndpoint
    .in("purposes")
    .out(jsonBody[List[Purpose]])

  val purposes: PartialServerEndpoint[UsernamePassword, Authentication.User, Purpose, RequestError, List[Purpose], Any, IO] = apiEndpoint
    .in("purposes")
    .in(path[Purpose]("purpose"))
    .out(jsonBody[List[Purpose]])


  val endpoints: List[AnyEndpoint] = List(listInterval, notes, allPurposes, purposes, get, add, addList, edit, delete).map(_.endpoint)


}
