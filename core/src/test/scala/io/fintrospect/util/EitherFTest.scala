package io.fintrospect.util

import com.twitter.util.{Await, Future}
import io.fintrospect.util.EitherF.eitherF
import org.scalatest.{FunSpec, ShouldMatchers}

class EitherFTest extends FunSpec with ShouldMatchers {

  describe("EitherF") {

    describe("map") {
      it("Init with Future value") {
        val map = eitherF(Future.value("success")).map(identity)
        Await.result(map.matchF { case a => Future.value(a.toString) }) shouldBe "Right(success)"
      }

      it("Init with Future exception") {
        val f = eitherF(Future.exception(new RuntimeException("foo")))
        val map = f.map(identity)
        intercept[RuntimeException](Await.result(map.matchF { case a => Future.value(a.toString) })).getMessage shouldBe "foo"
      }

      it("Init with simple value") {
        val map = eitherF("success").map(identity)
        Await.result(map.matchF { case a => Future.value(a.toString) }) shouldBe "Right(success)"
      }

      it("Init with either") {
        val map = eitherF(Right("success")).flatMap(Right(_))
        Await.result(map.matchF { case a => Future.value(a.toString) }) shouldBe "Right(success)"
      }
    }

    describe("flatMap") {
      describe("Init with Future value") {
        it("Right") {
          val map = eitherF(Future.value("success")).flatMap(Right(_))
          Await.result(map.matchF { case a => Future.value(a.toString) }) shouldBe "Right(success)"
        }
        it("Left") {
          val map = eitherF(Future.value("success")).flatMap(Left(_))
          Await.result(map.matchF { case a => Future.value(a.toString) }) shouldBe "Left(success)"
        }
      }

      describe("Init with Future exception") {
        it("Right") {
          val f = eitherF(Future.exception(new RuntimeException("foo")))
          val map = f.flatMap(Right(_))
          intercept[RuntimeException](Await.result(map.matchF { case a => Future.value(a.toString) })).getMessage shouldBe "foo"
        }
        it("Left") {
          val f = eitherF(Future.exception(new RuntimeException("foo")))
          val map = f.flatMap(Left(_))
          intercept[RuntimeException](Await.result(map.matchF { case a => Future.value(a.toString) })).getMessage shouldBe "foo"
        }
      }

      describe("Init with simple value") {
        it("Right") {
          val map = eitherF("success").flatMap(Right(_))
          Await.result(map.matchF { case a => Future.value(a.toString) }) shouldBe "Right(success)"
        }
        it("Left") {
          val map = eitherF("success").flatMap(Left(_))
          Await.result(map.matchF { case a => Future.value(a.toString) }) shouldBe "Left(success)"
        }
      }

      describe("Init with either") {
        it("Right") {
          val map = eitherF(Right("success")).flatMap(Right(_))
          Await.result(map.matchF { case a => Future.value(a.toString) }) shouldBe "Right(success)"
        }
        it("Left") {
          val map = eitherF(Right("success")).flatMap(Left(_))
          Await.result(map.matchF { case a => Future.value(a.toString) }) shouldBe "Left(success)"
        }
      }
    }

    describe("flatMapF") {
      describe("Init with Future value") {
        it("Right") {
          val map = eitherF(Future.value("success")).flatMapF(v => Future.value(Right(v)))
          Await.result(map.matchF { case a => Future.value(a.toString) }) shouldBe "Right(success)"
        }
        it("Left") {
          val map = eitherF(Future.value("success")).flatMapF(v => Future.value(Left(v)))
          Await.result(map.matchF { case a => Future.value(a.toString) }) shouldBe "Left(success)"
        }
      }

      describe("Init with Future exception") {
        it("Right") {
          val f = eitherF(Future.exception(new RuntimeException("foo")))
          val map = f.flatMapF(v => Future.value(Right(v)))
          intercept[RuntimeException](Await.result(map.matchF { case a => Future.value(a.toString) })).getMessage shouldBe "foo"
        }
        it("Left") {
          val f = eitherF(Future.exception(new RuntimeException("foo")))
          val map = f.flatMapF(v => Future.value(Left(v)))
          intercept[RuntimeException](Await.result(map.matchF { case a => Future.value(a.toString) })).getMessage shouldBe "foo"
        }
      }

      describe("Init with simple value") {
        it("Right") {
          val map = eitherF("success").flatMapF(v => Future.value(Right(v)))
          Await.result(map.matchF { case a => Future.value(a.toString) }) shouldBe "Right(success)"
        }
        it("Left") {
          val map = eitherF(Future.value("success")).flatMapF(v => Future.value(Left(v)))
          Await.result(map.matchF { case a => Future.value(a.toString) }) shouldBe "Left(success)"
        }
      }

      describe("Init with either") {
        it("Right") {
          val map = eitherF(Right("success")).flatMapF(v => Future.value(Right(v)))
          Await.result(map.matchF { case a => Future.value(a.toString) }) shouldBe "Right(success)"
        }
        it("Left") {
          val map = eitherF(Right("success")).flatMapF(v => Future.value(Left(v)))
          Await.result(map.matchF { case a => Future.value(a.toString) }) shouldBe "Left(success)"
        }
      }
    }
  }
}
