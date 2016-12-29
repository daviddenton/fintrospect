package io.fintrospect.formats

import java.io.OutputStream

import com.twitter.concurrent.AsyncStream
import com.twitter.io.{Buf, Reader}
import org.jboss.netty.buffer.ChannelBuffer


trait ResponseMagnet[T] extends (ResponseBuilder[T] => ResponseBuilder[T])

object ResponseMagnet {
  implicit def stringToMag[T](s: String): ResponseMagnet[T] = new ResponseMagnet[T] {
    override def apply(b: ResponseBuilder[T]) = b.withContent(s)
  }

  implicit def bufToMag[T](s: Buf): ResponseMagnet[T] = new ResponseMagnet[T] {
    override def apply(b: ResponseBuilder[T]) = b.withContent(s)
  }

  implicit def nothingToMag[T](unit: Unit): ResponseMagnet[T] = new ResponseMagnet[T] {
    override def apply(b: ResponseBuilder[T]) = b
  }

  implicit def outputStreamToMag[T](f: OutputStream => Unit): ResponseMagnet[T] = new ResponseMagnet[T] {
    override def apply(b: ResponseBuilder[T]) = b.withContent(f)
  }

  implicit def channelBufferToMag[T](channelBuffer: ChannelBuffer): ResponseMagnet[T] = new ResponseMagnet[T] {
    override def apply(b: ResponseBuilder[T]) = b.withContent(channelBuffer)
  }

  implicit def streamToMag[T](stream: AsyncStream[T]): ResponseMagnet[T] = new ResponseMagnet[T] {
    override def apply(b: ResponseBuilder[T]) = b.withContent(stream)
  }

  implicit def readerToMag[T](reader: Reader): ResponseMagnet[T] = new ResponseMagnet[T] {
    override def apply(b: ResponseBuilder[T]) = b.withContent(reader)
  }

  implicit def tToMag[T](t: T): ResponseMagnet[T] = new ResponseMagnet[T] {
    override def apply(b: ResponseBuilder[T]) = b.withContent(t)
  }

  implicit def errorToMag[T](error: Throwable): ResponseMagnet[T] = new ResponseMagnet[T] {
    override def apply(b: ResponseBuilder[T]) = b.withError(error)
  }
}


