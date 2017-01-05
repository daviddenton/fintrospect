package io.fintrospect.formats

import java.io.OutputStream

import com.twitter.concurrent.AsyncStream
import com.twitter.io.{Buf, Reader}
import org.jboss.netty.buffer.ChannelBuffer

/**
  * Magnet to convert content types (such as String, Buf, custom JSON types etc) to a common type that
  * ResponseBuilders can accept.
  *
  * See companion object methods for various conversions available.
  */
trait ResponseBuilderMagnet[T] extends (ResponseBuilder[T] => ResponseBuilder[T])

object ResponseBuilderMagnet {
  /**
    * Convert Buf to a ResponseBuilderMagnet for building responses
    */
  implicit def bufToMagnet[T](s: Buf): ResponseBuilderMagnet[T] = new ResponseBuilderMagnet[T] {
    override def apply(b: ResponseBuilder[T]) = b.withContent(s)
  }

  /**
    * Convert OutputStream writing function to a ResponseBuilderMagnet for building responses
    */
  implicit def outputStreamToMagnet[T](f: OutputStream => Unit): ResponseBuilderMagnet[T] = new ResponseBuilderMagnet[T] {
    override def apply(b: ResponseBuilder[T]) = b.withContent(f)
  }

  /**
    * Convert ChannelBuffer to a ResponseBuilderMagnet for building responses
    */
  implicit def channelBufferToMagnet[T](channelBuffer: ChannelBuffer): ResponseBuilderMagnet[T] = new ResponseBuilderMagnet[T] {
    override def apply(b: ResponseBuilder[T]) = b.withContent(channelBuffer)
  }

  /**
    * Convert AsyncStream to a ResponseBuilderMagnet for building responses
    */
  implicit def streamToMagnet[T](stream: AsyncStream[T]): ResponseBuilderMagnet[T] = new ResponseBuilderMagnet[T] {
    override def apply(b: ResponseBuilder[T]) = b.withContent(stream)
  }

  /**
    * Convert Reader to a ResponseBuilderMagnet for building responses
    */
  implicit def readerToMagnet[T](reader: Reader): ResponseBuilderMagnet[T] = new ResponseBuilderMagnet[T] {
    override def apply(b: ResponseBuilder[T]) = b.withContent(reader)
  }

  /**
    * Convert a custom format type (eg. JsonObject) to a ResponseBuilderMagnet for building responses
    */
  implicit def customFormatToMagnet[T](t: T): ResponseBuilderMagnet[T] = new ResponseBuilderMagnet[T] {
    override def apply(b: ResponseBuilder[T]) = b.withContent(t)
  }

  /**
    * Convert String to a ResponseBuilderMagnet for building responses
    */
  implicit def stringToMagnet[T](s: String): ResponseBuilderMagnet[T] = new ResponseBuilderMagnet[T] {
    override def apply(b: ResponseBuilder[T]) = if(b.response.status.code < 400) b.withContent(s) else b.withErrorMessage(s)
  }

  /**
    * Convert an Exception to a ResponseBuilderMagnet for building responses
    */
  implicit def exceptionToMagnet[T](error: Throwable): ResponseBuilderMagnet[T] = new ResponseBuilderMagnet[T] {
    override def apply(b: ResponseBuilder[T]) = b.withError(error)
  }
}


