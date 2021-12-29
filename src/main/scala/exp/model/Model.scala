package exp.model

import io.circe.Codec
import io.circe._
import io.circe.generic.semiauto._
import io.circe.generic.extras.semiauto._

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

}
