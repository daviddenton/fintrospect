package experiments

import com.twitter.finagle.http.Method
import experiments.types.{Filt, RqParam}
import io.fintrospect.ContentType
import io.fintrospect.parameters.Body

object Contract {
  def apply(summary: String = "<unknown>", description: Option[String] = None) = Contract0(Terms(summary, description))
}

abstract class Contract(rps: RqParam[_]*) {
  type This <: Contract
  val terms: Terms

  def update(terms: Terms): This

  val params: Seq[RqParam[_]] = rps

  def consuming(contentTypes: ContentType*) = update(terms.consuming(contentTypes))

  def producing(contentTypes: ContentType*) = update(terms.producing(contentTypes))

  def withFilter(filter: Filt) = update(terms.withFilter(filter))
}

trait ExtendableContract {
  val terms: Terms

  type Next[T] <: Contract

  def next[NEXT](terms: Terms, next: RqParam[NEXT]): Next[NEXT]

  def taking[NEXT](nxt: RqParam[NEXT]) = next(terms, nxt)

  def body[BODY](nxt: Body[BODY]) = next(terms.body(nxt.asInstanceOf[Body[BODY]]), nxt)
}

case class Contract0(terms: Terms)
  extends Contract() with ExtendableContract {
  type This = Contract0
  type Next[T] = Contract1[T]

  override def update(terms: Terms) = this.copy(terms = terms)

  override def next[NEXT](terms: Terms, nxt: RqParam[NEXT]) = Contract1(terms, nxt)

  def at(method: Method) = PathBuilder0(method, terms, identity)
}

case class Contract1[RP1](terms: Terms, rp1: RqParam[RP1])
  extends Contract(rp1) with ExtendableContract {
  type This = Contract1[RP1]
  type Next[T] = Contract2[RP1, T]

  override def update(terms: Terms) = this.copy(terms = terms)

  override def next[NEXT](terms: Terms, nxt: RqParam[NEXT]) = Contract2(terms, rp1, nxt)

  def at(method: Method) = PathBuilder0(method, terms, req => (rp1.from(req), req))
}

case class Contract2[RP1, RP2](terms: Terms, rp1: RqParam[RP1], rp2: RqParam[RP2])
  extends Contract(rp1, rp2) with ExtendableContract {
  type This = Contract2[RP1, RP2]
  type Next[T] = Contract3[RP1, RP2, T]

  override def update(terms: Terms) = this.copy(terms = terms)

  override def next[NEXT](terms: Terms, nxt: RqParam[NEXT]) = Contract3(terms, rp1, rp2, nxt)

  def at(method: Method) = PathBuilder0(method, terms, req => (rp1.from(req), rp2.from(req), req))
}

case class Contract3[RP1, RP2, RP3](terms: Terms, rp1: RqParam[RP1], rp2: RqParam[RP2], rp3: RqParam[RP3])
  extends Contract(rp1, rp2, rp3) with ExtendableContract {
  type This = Contract3[RP1, RP2, RP3]
  type Next[T] = Contract4[RP1, RP2, RP3, T]

  override def update(terms: Terms) = this.copy(terms = terms)

  override def next[NEXT](terms: Terms, nxt: RqParam[NEXT]) = Contract4(terms, rp1, rp2, rp3, nxt)

  def at(method: Method) = PathBuilder0(method, terms, req => (rp1.from(req), rp2.from(req), rp3.from(req), req))
}

case class Contract4[RP1, RP2, RP3, RP4](terms: Terms, rp1: RqParam[RP1], rp2: RqParam[RP2], rp3: RqParam[RP3], rp4: RqParam[RP4])
  extends Contract(rp1, rp2, rp3, rp4) with ExtendableContract {
  type This = Contract4[RP1, RP2, RP3, RP4]
  type Next[T] = Contract5[RP1, RP2, RP3, RP4, T]

  override def update(terms: Terms) = this.copy(terms = terms)

  override def next[NEXT](terms: Terms, nxt: RqParam[NEXT]) = Contract5(terms, rp1, rp2, rp3, rp4, nxt)

  def at(method: Method) = PathBuilder0(method, terms, req =>
    (rp1.from(req), rp2.from(req), rp3.from(req), rp4.from(req), req))
}

case class Contract5[RP1, RP2, RP3, RP4, RP5](terms: Terms, rp1: RqParam[RP1], rp2: RqParam[RP2],
                                              rp3: RqParam[RP3], rp4: RqParam[RP4], rp5: RqParam[RP5])
  extends Contract(rp1, rp2, rp3, rp4, rp5) with ExtendableContract {
  type This = Contract5[RP1, RP2, RP3, RP4, RP5]
  type Next[T] = Contract6[RP1, RP2, RP3, RP4, RP5, T]

  override def update(terms: Terms) = this.copy(terms = terms)

  override def next[NEXT](terms: Terms, nxt: RqParam[NEXT]) = Contract6(terms, rp1, rp2, rp3, rp4, rp5, nxt)

  def at(method: Method) = PathBuilder0(method, terms, req =>
    (rp1.from(req), rp2.from(req), rp3.from(req), rp4.from(req), rp5.from(req), req))
}

case class Contract6[RP1, RP2, RP3, RP4, RP5, RP6](terms: Terms, rp1: RqParam[RP1], rp2: RqParam[RP2],
                                                   rp3: RqParam[RP3], rp4: RqParam[RP4], rp5: RqParam[RP5], rp6: RqParam[RP6])
  extends Contract(rp1, rp2, rp3, rp4, rp5, rp6) {
  type This = Contract6[RP1, RP2, RP3, RP4, RP5, RP6]

  override def update(terms: Terms) = this.copy(terms = terms)

  def at(method: Method) = PathBuilder0(method, terms, req =>
    (rp1.from(req), rp2.from(req), rp3.from(req), rp4.from(req), rp5.from(req), rp6.from(req), req))
}