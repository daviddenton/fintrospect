package io.fintrospect.util

import com.twitter.util.{Await, Future}
import io.fintrospect.util.EitherF.eitherF
import org.scalatest.{FunSpec, Matchers}

class EitherFTest extends FunSpec with Matchers {

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


    describe("mapF") {
      describe("Init with Future value") {
        it("Future value") {
          val map = eitherF(Future.value("success")).mapF(Future.value)
          Await.result(map.matchF { case a => Future.value(a.toString) }) shouldBe "Right(success)"
        }
        it("Future exception") {
          val map = eitherF(Future.value("success")).mapF(_ => Future.exception(new RuntimeException("foo")))
          intercept[RuntimeException](Await.result(map.matchF { case a => Future.value(a.toString) })).getMessage shouldBe "foo"
        }
      }

      describe("Init with Future exception") {
        it("Future value") {
          val f = eitherF(Future.exception(new RuntimeException("foo")))
          val map = f.mapF(Future.value)
          intercept[RuntimeException](Await.result(map.matchF { case a => Future.value(a.toString) })).getMessage shouldBe "foo"
        }
        it("Future exception") {
          val f = eitherF(Future.exception(new RuntimeException("foo")))
          val map = f.mapF(_ => Future.exception(new RuntimeException("foo")))
          intercept[RuntimeException](Await.result(map.matchF { case a => Future.value(a.toString) })).getMessage shouldBe "foo"
        }
      }

      describe("Init with simple value") {
        it("Future value") {
          val map = eitherF("success").mapF(Future.value)
          Await.result(map.matchF { case a => Future.value(a.toString) }) shouldBe "Right(success)"
        }
        it("Future exception") {
          val map = eitherF("success").mapF(_ => Future.exception(new RuntimeException("foo")))
          intercept[RuntimeException](Await.result(map.matchF { case a => Future.value(a.toString) })).getMessage shouldBe "foo"
        }
      }

      describe("Init with either") {
        it("Future value") {
          val map = eitherF(Right("success")).mapF(Future.value)
          Await.result(map.matchF { case a => Future.value(a.toString) }) shouldBe "Right(success)"
        }
        it("Future exception") {
          val map = eitherF(Right("success")).mapF(_ => Future.exception(new RuntimeException("foo")))
          intercept[RuntimeException](Await.result(map.matchF { case a => Future.value(a.toString) })).getMessage shouldBe "foo"
        }
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
