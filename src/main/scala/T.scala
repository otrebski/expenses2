import cats.Semigroup
import cats.syntax.all._

object T extends App {

  case class X(a: String)

  implicit val xSemigroup = new Semigroup[X] {
    override def combine(x: X, y: X): X = X(x.a |+| y.a)
  }

  val f1: String => Int = (v1: String) => v1.toList.count(c => "aeiou".indexOf(c) != -1)
  val f2: String => Int = (v1: String) => v1.toList.count(c => "AEIOU".indexOf(c) != -1)
  val fX1: String => X = (v1: String) => X(v1.toLowerCase.trim)
  val fX2: String => X = (v1: String) => X(v1.toUpperCase.trim)
  val f3: String => Int = f1 |+| f2

  val f4 = fX1 |+| fX2

  val s = " Ala ma kota "
  println(s"f1: ${f1(s)}")
  println(s"f2: ${f2(s)}")
  println(s"f3: ${f3(s)}")
  println(s"f4: ${f4(s)}")

}
