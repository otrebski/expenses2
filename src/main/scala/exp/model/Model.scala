package exp.model

import io.circe.Codec
import io.circe._
import io.circe.generic.semiauto._
import io.circe.generic.extras.semiauto._
import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._

object Model {

  case class Country(name: String)

  case class Author(name: String, country: Country)

  case class Genre(name: String, description: String)

  case class Book(title: String, genre: Genre, year: Int, author: Author)

  case class Purpose(value: String)

  object Purpose {
    implicit val codec: Codec[Purpose] = deriveUnwrappedCodec
  }

  case class Note(value: String)

  object Note {
    implicit val codec: Codec[Note] = deriveUnwrappedCodec
  }

  object Expense {

    case class Id(value: Long)

    object Id {
      implicit val codec: Codec[Id] = deriveUnwrappedCodec
    }

  }

  case class Expense(
                      id: Expense.Id,
                      amount: BigDecimal,
                      purpose: Purpose,
                      note: Note,
                      date: Date
                    )

  case class ExpenseToAdd(
                           amount: BigDecimal,
                           purpose: Purpose,
                           note: Note,
                           date: Date
                         )

  case class Date(year: Int, month: Int)


  case class CalculateRequest(expression: String)

  sealed trait CalculateResult

  case class SuccessfulCalculateResult(result: BigDecimal, expression: String, success: Boolean = true) extends CalculateResult

  case class FailureCalculateResult(expression: String, error: String, success: Boolean = false) extends CalculateResult

}
