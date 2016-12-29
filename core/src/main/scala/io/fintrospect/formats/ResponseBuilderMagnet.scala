package io.fintrospect.formats

import java.io.OutputStream

import com.twitter.concurrent.AsyncStream
import com.twitter.io.{Buf, Reader}
import org.jboss.netty.buffer.ChannelBuffer


trait ResponseBuilderMagnet[T] extends (ResponseBuilder[T] => ResponseBuilder[T])

object ResponseBuilderMagnet {
  implicit def bufToMagnet[T](s: Buf): ResponseBuilderMagnet[T] = new ResponseBuilderMagnet[T] {
    override def apply(b: ResponseBuilder[T]) = b.withContent(s)
  }

  implicit def outputStreamToMagnet[T](f: OutputStream => Unit): ResponseBuilderMagnet[T] = new ResponseBuilderMagnet[T] {
    override def apply(b: ResponseBuilder[T]) = b.withContent(f)
  }

  implicit def channelBufferToMagnet[T](channelBuffer: ChannelBuffer): ResponseBuilderMagnet[T] = new ResponseBuilderMagnet[T] {
    override def apply(b: ResponseBuilder[T]) = b.withContent(channelBuffer)
  }

  implicit def streamToMagnet[T](stream: AsyncStream[T]): ResponseBuilderMagnet[T] = new ResponseBuilderMagnet[T] {
    override def apply(b: ResponseBuilder[T]) = b.withContent(stream)
  }

  implicit def readerToMagnet[T](reader: Reader): ResponseBuilderMagnet[T] = new ResponseBuilderMagnet[T] {
    override def apply(b: ResponseBuilder[T]) = b.withContent(reader)
  }

  implicit def tToMagnet[T](t: T): ResponseBuilderMagnet[T] = new ResponseBuilderMagnet[T] {
    override def apply(b: ResponseBuilder[T]) = b.withContent(t)
  }

  implicit def stringToMagnet[T](s: String): ResponseBuilderMagnet[T] = new ResponseBuilderMagnet[T] {
    override def apply(b: ResponseBuilder[T]) = if(b.response.status.code < 400) b.withContent(s) else b.withErrorMessage(s)
  }

  implicit def errorToMagnet[T](error: Throwable): ResponseBuilderMagnet[T] = new ResponseBuilderMagnet[T] {
    override def apply(b: ResponseBuilder[T]) = b.withError(error)
  }
}


