package exp.web

import exp.model.Model
import exp.model.Model.Genre
import sttp.model.headers.WWWAuthenticateChallenge
import sttp.tapir.model.UsernamePassword

object Endpoints {
  import io.circe.generic.auto._
  import sttp.tapir._
  import sttp.tapir.json.circe._
  import sttp.tapir.generic.auto._

  private val secureEndpoint = endpoint
    .securityIn(auth.basic[UsernamePassword](WWWAuthenticateChallenge.basic("example")))
    .mapErrorOut { e =>
      println(s"auth error $e");
      "Auth error"
    }(e => println(s"ups $e"))

  val book: Model.Book =
    Model.Book(title = "T", genre = Genre("genre", "genre desc"), year = 2021, author = Model.Author("John", Model.Country("PL")))

  val get: Endpoint[UsernamePassword, Unit, String, Model.Book, Any] = secureEndpoint
    .securityIn("books" / "get")
    .out(jsonBody[Model.Book].example(book))

  val list: Endpoint[UsernamePassword, Unit, String, List[Model.Book], Any] = secureEndpoint
    .securityIn("books" / "list")
    .out(jsonBody[List[Model.Book]].example(List(book)))

}
