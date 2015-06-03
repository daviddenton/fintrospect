package io.fintrospect

import com.twitter.finagle.http.path.Root
import com.twitter.util.Future
import io.fintrospect.parameters.RequestParameter
import org.jboss.netty.handler.codec.http.{HttpMethod, HttpResponse}

object ClientRoute {
  def apply(name: String): ClientRoute0 = ClientRoute0()
}

trait ClientRoute[T] {
  self: T =>
  def at(method: HttpMethod): ClientPath[T] = ClientPath(this, Root)
}

case class ClientRoute0() extends ClientRoute[ClientRoute0] {
  def apply(): Future[HttpResponse] = ???

  def taking[T](rq: RequestParameter[T]) = ClientRoute1(rq)
}

case class ClientRoute1[A](rp0: RequestParameter[A])
  extends ClientRoute[ClientRoute1[A]] {
  def apply(a: A): Future[HttpResponse] = ???

  def taking[T](rp1: RequestParameter[T]) = ClientRoute2(rp0, rp1)
}

case class ClientRoute2[A, B](rp0: RequestParameter[A], rp1: RequestParameter[B])
  extends ClientRoute[ClientRoute2[A, B]] {
  def apply(a: A, b: B): Future[HttpResponse] = ???

  def taking[T](rp2: RequestParameter[T]) = ClientRoute3(rp0, rp1, rp2)
}

case class ClientRoute3[A, B, C](rp0: RequestParameter[A], rp1: RequestParameter[B], rp2: RequestParameter[C])
  extends ClientRoute[ClientRoute3[A, B, C]] {
  def apply(a: A, b: B, c: C): Future[HttpResponse] = ???

  def taking[T](rp3: RequestParameter[T]) = ClientRoute4(rp0, rp1, rp2, rp3)
}

case class ClientRoute4[A, B, C, D](rp0: RequestParameter[A], rp1: RequestParameter[B], rp2: RequestParameter[C], rp3: RequestParameter[D])
  extends ClientRoute[ClientRoute4[A, B, C, D]] {
  def apply(a: A, b: B, c: C, d: D): Future[HttpResponse] = ???
}
