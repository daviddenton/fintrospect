package io.fintrospect.util

import com.twitter.util.{Await, Future}
import io.fintrospect.util.EitherF.eitherF
import org.scalatest.{FunSpec, ShouldMatchers}

class EitherFTest extends FunSpec with ShouldMatchers {

  describe("EitherF") {

    describe("map") {
      describe("Init with Future value") {
        it("Right") {
          val map = eitherF(Future.value("success")).map(Right(_))
          Await.result(map.end(_.toString)) shouldBe "Right(success)"
        }
        it("Left") {
          val map = eitherF(Future.value("success")).map(Left(_))
          Await.result(map.end(_.toString)) shouldBe "Left(success)"
        }
      }

      describe("Init with Future exception") {
        it("Right") {
          val f = eitherF(Future.exception(new RuntimeException("foo")))
          val map = f.map(Right(_))
          intercept[RuntimeException](Await.result(map.end(_.toString))).getMessage shouldBe "foo"
        }
        it("Left") {
          val f = eitherF(Future.exception(new RuntimeException("foo")))
          val map = f.map(Left(_))
          intercept[RuntimeException](Await.result(map.end(_.toString))).getMessage shouldBe "foo"
        }
      }

      describe("Init with simple value") {
        it("Right") {
          val map = eitherF("success").map(Right(_))
          Await.result(map.end(_.toString)) shouldBe "Right(success)"
        }
        it("Left") {
          val map = eitherF(Future.value("success")).map(Left(_))
          Await.result(map.end(_.toString)) shouldBe "Left(success)"
        }
      }

      describe("Init with either") {
        it("Right") {
          val map = eitherF(Right("success")).map(Right(_))
          Await.result(map.end(_.toString)) shouldBe "Right(success)"
        }
        it("Left") {
          val map = eitherF(Right("success")).map(Left(_))
          Await.result(map.end(_.toString)) shouldBe "Left(success)"
        }
      }
    }

    describe("flatmap") {
      describe("Init with Future value") {
        it("Right") {
          val map = eitherF(Future.value("success")).flatMap(v => Future.value(Right(v)))
          Await.result(map.end(_.toString)) shouldBe "Right(success)"
        }
        it("Left") {
          val map = eitherF(Future.value("success")).flatMap(v => Future.value(Left(v)))
          Await.result(map.end(_.toString)) shouldBe "Left(success)"
        }
      }

      describe("Init with Future exception") {
        it("Right") {
          val f = eitherF(Future.exception(new RuntimeException("foo")))
          val map = f.flatMap(v => Future.value(Right(v)))
          intercept[RuntimeException](Await.result(map.end(_.toString))).getMessage shouldBe "foo"
        }
        it("Left") {
          val f = eitherF(Future.exception(new RuntimeException("foo")))
          val map = f.flatMap(v => Future.value(Left(v)))
          intercept[RuntimeException](Await.result(map.end(_.toString))).getMessage shouldBe "foo"
        }
      }

      describe("Init with simple value") {
        it("Right") {
          val map = eitherF("success").flatMap(v => Future.value(Right(v)))
          Await.result(map.end(_.toString)) shouldBe "Right(success)"
        }
        it("Left") {
          val map = eitherF(Future.value("success")).flatMap(v => Future.value(Left(v)))
          Await.result(map.end(_.toString)) shouldBe "Left(success)"
        }
      }

      describe("Init with either") {
        it("Right") {
          val map = eitherF(Right("success")).flatMap(v => Future.value(Right(v)))
          Await.result(map.end(_.toString)) shouldBe "Right(success)"
        }
        it("Left") {
          val map = eitherF(Right("success")).flatMap(v => Future.value(Left(v)))
          Await.result(map.end(_.toString)) shouldBe "Left(success)"
        }
      }
    }
  }
}
