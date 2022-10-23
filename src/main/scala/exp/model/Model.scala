package exp.model

import exp.model.Model.Note
import io.circe.Codec
import io.circe.Json.JString
import io.circe.*
import io.circe.generic.semiauto.*
import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._
import io.circe.{Decoder, Encoder}

object Model {

  opaque type Purpose = String

  object Purpose {
    def apply(purpose: String): Purpose = purpose

    implicit val encoder: Encoder[Purpose] = Encoder.encodeString.contramap(p => p.value())
    implicit val decoder: Decoder[Purpose] = Decoder.decodeString.map(Purpose.apply)
    implicit val schema: sttp.tapir.Schema[Purpose] = sttp.tapir.Schema.schemaForString
  }

  extension (p: Purpose) {
    def contains(purpose: Purpose) = p.toLowerCase.contains(purpose.toLowerCase)
    def value(): String = p

  }


  //  object Purpose {
  //    implicit val decoder: Decoder[Purpose] = Decoder.decodeString.map(s => Purpose(s))
  //    implicit val encoder: Encoder[Purpose] = Encoder.instance(p => Json.fromString(p.value))
  //  }

  //  case class Note(value: String)

  opaque type Note = String

  object Note {
    def apply(note: String): Note = note

    implicit val encoder: Encoder[Note] = Encoder.encodeString.contramap(p => p.value())
    implicit val decoder: Decoder[Note] = Decoder.decodeString.map(Note.apply)
  }

  extension (note: Note) {
    def v(): String = note
  }

  //  object Note {
  //    implicit val decoder: Decoder[Note] = Decoder.decodeString.map(s => Note(s))
  //    implicit val encoder: Encoder[Note] = Encoder.instance(p => Json.fromString(p.value))
  //  }

  object Expense {

    case class Id(value: Long)

    object Id {
      implicit val decoder: Decoder[Id] = Decoder.decodeLong.map(s => Id(s))
      implicit val encoder: Encoder[Id] = Encoder.instance(p => Json.fromLong(p.value))
      implicit val schema: sttp.tapir.Schema[Id] = sttp.tapir.Schema.derived
    }

    implicit val codec: Codec[Expense] = deriveCodec
    implicit val schema: sttp.tapir.Schema[Expense] = sttp.tapir.Schema.derived

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

  object ExpenseToAdd {
    implicit val codec: Codec[ExpenseToAdd] = deriveCodec
    implicit val schema: sttp.tapir.Schema[ExpenseToAdd] = sttp.tapir.Schema.derived
  }

  case class Date(year: Int, month: Int)

  object Date {
    implicit class DateOps(date: Date) {
      def >(otherDate: Date): Boolean = (date.year * 100 + date.month) > (otherDate.year * 100 + otherDate.month)

      def >=(otherDate: Date): Boolean = (date.year * 100 + date.month) >= (otherDate.year * 100 + otherDate.month)

      def <(otherDate: Date): Boolean = (date.year * 100 + date.month) < (otherDate.year * 100 + otherDate.month)

      def <=(otherDate: Date): Boolean = (date.year * 100 + date.month) <= (otherDate.year * 100 + otherDate.month)
    }

    implicit val schema: sttp.tapir.Schema[Date] = sttp.tapir.Schema.derived
  }

  case class CalculateRequest(expression: String)

  sealed trait CalculateResult

  case class SuccessfulCalculateResult(result: BigDecimal, expression: String, success: Boolean = true) extends CalculateResult

  case class FailureCalculateResult(expression: String, error: String, success: Boolean = false) extends CalculateResult

  case class ExpenseSummary(purpose: Purpose, amount: BigDecimal)

  object ExpenseSummary {
    implicit val coded: Codec[ExpenseSummary] = deriveCodec
    implicit val schema: sttp.tapir.Schema[ExpenseSummary] = sttp.tapir.Schema.derived
  }

  case class ExpenseReport(expenseSummary: List[ExpenseSummary], expenses: List[Expense])

  object ExpenseReport {
    implicit val codec: Codec[ExpenseReport] = deriveCodec
    implicit val schema: sttp.tapir.Schema[ExpenseReport] = sttp.tapir.Schema.derived
  }

  case class NotesSuggestionRequest(purpose: String, note: String)

  object NotesSuggestionRequest {
    implicit val codec: Codec[NotesSuggestionRequest] = deriveCodec
    implicit val schema: sttp.tapir.Schema[NotesSuggestionRequest] = sttp.tapir.Schema.derived
  }

  case class NotesSuggestionResponse(suggestions: List[Suggestion])

  object NotesSuggestionResponse {
    implicit val codec: Codec[NotesSuggestionResponse] = deriveCodec
    implicit val schema: sttp.tapir.Schema[NotesSuggestionResponse] = sttp.tapir.Schema.derived
  }

  case class Suggestion(suggestion: Note, count: Int)

  object Suggestion {
    implicit val schema: sttp.tapir.Schema[Suggestion] = sttp.tapir.Schema.derived
  }

}
