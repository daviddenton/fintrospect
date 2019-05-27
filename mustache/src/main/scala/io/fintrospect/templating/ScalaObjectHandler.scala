package io.fintrospect.templating

import java.io.Writer
import java.lang.reflect.{Field, Method}

import com.github.mustachejava.Iteration
import com.github.mustachejava.reflect.ReflectionObjectHandler

import scala.collection.JavaConverters
import scala.reflect.ClassTag
import scala.runtime.BoxedUnit

class ScalaObjectHandler extends ReflectionObjectHandler {

  override def checkMethod(member: Method) {}

  override def checkField(member: Field) {}

  override def coerce(value: AnyRef) = value match {
    case m: collection.Map[_, _] => JavaConverters.mapAsJavaMap(m)
    case _: BoxedUnit => null
    case Some(some: AnyRef) => coerce(some)
    case None => null
    case _ => value
  }

  override def iterate(iteration: Iteration, writer: Writer, value: AnyRef, scopes: java.util.List[AnyRef]) = value match {
    case TraversableAnyRef(t) => {
      var newWriter = writer
      t foreach (next => newWriter = iteration.next(newWriter, coerce(next), scopes))
      newWriter
    }
    case n: Number => if (n.intValue() == 0) writer else iteration.next(writer, coerce(value), scopes)
    case _ => super.iterate(iteration, writer, value, scopes)
  }

  override def falsey(iteration: Iteration, writer: Writer, value: AnyRef, scopes: java.util.List[AnyRef]) = value match {
    case TraversableAnyRef(t) => if (t.isEmpty) iteration.next(writer, value, scopes) else writer
    case n: Number => if (n.intValue() == 0) iteration.next(writer, coerce(value), scopes) else writer
    case _ => super.falsey(iteration, writer, value, scopes)
  }

  private val TraversableAnyRef = new Def[Traversable[AnyRef]]

  private class Def[C: ClassTag] {
    def unapply[X: ClassTag](x: X): Option[C] = x match {
      case c: C => Some(c)
      case _ => None
    }
  }

}
