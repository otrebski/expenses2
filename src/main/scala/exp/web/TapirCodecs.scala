package exp.web

import exp.model.Model.Date
import exp.model.Model.Note
import exp.model.Model.Note.*
import exp.model.Model.Purpose
import exp.model.Model.Purpose.*
import sttp.tapir.Codec
import sttp.tapir.CodecFormat.TextPlain

object TapirCodecs {

  implicit val dateCodec: Codec[String, Date, TextPlain] = Codec.string
    .map(s => Date(s.split("-")(0).toInt, s.split("-")(1).toInt))(d =>
      s"${d.year}-${d.month}"
    )

  implicit val purposeCodec: Codec[String, Purpose, TextPlain] =
    Codec.string.map(Purpose(_))(_.value())
  implicit val noteCodec: Codec[String, Note, TextPlain] =
    Codec.string.map(Note(_))(_.v())

}
