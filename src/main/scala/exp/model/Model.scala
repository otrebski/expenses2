package exp.model

import io.circe.Codec
import io.circe.Json.JString
import io.circe._
import io.circe.generic.semiauto._
//import io.circe.generic.extras.semiauto._
import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._
import io.circe.{Decoder, Encoder}

object Model {

  case class Country(name: String)

  case class Author(name: String, country: Country)

  case class Genre(name: String, description: String)

  case class Book(title: String, genre: Genre, year: Int, author: Author)

  case class Purpose(value: String)

  object Purpose {
    implicit val decoder: Decoder[Purpose] = Decoder.decodeString.map(s => Purpose(s))
    implicit val encoder: Encoder[Purpose] = Encoder.instance(p => Json.fromString(p.value))
  }

  case class Note(value: String)

  object Note {
    implicit val decoder: Decoder[Note] = Decoder.decodeString.map(s => Note(s))
    implicit val encoder: Encoder[Note] = Encoder.instance(p => Json.fromString(p.value))
  }

  object Expense {

    case class Id(value: Long)

    object Id {
      implicit val decoder: Decoder[Id] = Decoder.decodeLong.map(s => Id(s))
      implicit val encoder: Encoder[Id] = Encoder.instance(p => Json.fromLong(p.value))
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

  object Date {
    implicit class DateOps(date: Date) {
      def >(otherDate: Date): Boolean = (date.year * 100 + date.month) > (otherDate.year * 100 + otherDate.month)

      def >=(otherDate: Date): Boolean = (date.year * 100 + date.month) >= (otherDate.year * 100 + otherDate.month)

      def <(otherDate: Date): Boolean = (date.year * 100 + date.month) < (otherDate.year * 100 + otherDate.month)

      def <=(otherDate: Date): Boolean = (date.year * 100 + date.month) <= (otherDate.year * 100 + otherDate.month)
    }
  }

  case class CalculateRequest(expression: String)

  sealed trait CalculateResult

  case class SuccessfulCalculateResult(result: BigDecimal, expression: String, success: Boolean = true) extends CalculateResult

  case class FailureCalculateResult(expression: String, error: String, success: Boolean = false) extends CalculateResult

  case class ExpenseSummary(purpose: String, amount: BigDecimal)

  object ExpenseSummary {
    implicit val coded: Codec[ExpenseSummary] = deriveCodec
  }

  case class ExpenseReport(expenseSummary: List[ExpenseSummary], expenses: List[Expense])

  case class NotesSuggestionRequest(purpose: String, note: String)

  case class NotesSuggestionResponse(suggestions: List[Suggestion])

  case class Suggestion(suggestion: String, count: Int)

}
