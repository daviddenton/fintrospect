package io.fintrospect.formats

import java.io.OutputStream

import com.twitter.concurrent.AsyncStream
import com.twitter.io.{Buf, Reader}
import io.netty.buffer.ByteBuf

/**
  * Magnet to convert content types (such as String, Buf, custom JSON types etc) to a common type that
  * ResponseBuilders can accept.
  *
  * See companion object methods for various conversions available.
  */
trait ResponseContentMagnet[T] extends (ResponseBuilder[T] => ResponseBuilder[T])

object ResponseContentMagnet {
  /**
    * Convert Buf to a ResponseBuilderMagnet for building responses
    */
  implicit def bufToMagnet[T](s: Buf): ResponseContentMagnet[T] = new ResponseContentMagnet[T] {
    override def apply(b: ResponseBuilder[T]) = b.withContent(s)
  }

  /**
    * Convert OutputStream writing function to a ResponseBuilderMagnet for building responses
    */
  implicit def outputStreamToMagnet[T](f: OutputStream => Unit): ResponseContentMagnet[T] = new ResponseContentMagnet[T] {
    override def apply(b: ResponseBuilder[T]) = b.withContent(f)
  }

  /**
    * Convert ChannelBuffer to a ResponseBuilderMagnet for building responses
    */
  implicit def channelBufferToMagnet[T](bytebuf: ByteBuf): ResponseContentMagnet[T] = new ResponseContentMagnet[T] {
    override def apply(b: ResponseBuilder[T]) = b.withContent(bytebuf)
  }

  /**
    * Convert AsyncStream to a ResponseBuilderMagnet for building responses
    */
  implicit def streamToMagnet[T](stream: AsyncStream[T]): ResponseContentMagnet[T] = new ResponseContentMagnet[T] {
    override def apply(b: ResponseBuilder[T]) = b.withContent(stream)
  }

  /**
    * Convert Reader to a ResponseBuilderMagnet for building responses
    */
  implicit def readerToMagnet[T](reader: Reader[Buf]): ResponseContentMagnet[T] = new ResponseContentMagnet[T] {
    override def apply(b: ResponseBuilder[T]) = b.withContent(reader)
  }

  /**
    * Convert a custom format type (eg. JsonObject) to a ResponseBuilderMagnet for building responses
    */
  implicit def customFormatToMagnet[T](t: T): ResponseContentMagnet[T] = new ResponseContentMagnet[T] {
    override def apply(b: ResponseBuilder[T]) = b.withContent(t)
  }

  /**
    * Convert String to a ResponseBuilderMagnet for building responses
    */
  implicit def stringToMagnet[T](s: String): ResponseContentMagnet[T] = new ResponseContentMagnet[T] {
    override def apply(b: ResponseBuilder[T]) = if(b.response.status.code < 400) b.withContent(s) else b.withErrorMessage(s)
  }

  /**
    * Convert an Exception to a ResponseBuilderMagnet for building responses
    */
  implicit def exceptionToMagnet[T](error: Throwable): ResponseContentMagnet[T] = new ResponseContentMagnet[T] {
    override def apply(b: ResponseBuilder[T]) = b.withError(error)
  }
}


