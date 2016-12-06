package io.fintrospect.parameters

import java.io.{File, RandomAccessFile}
import java.nio.channels.FileChannel

import com.twitter.finagle.http.exp.Multipart.{FileUpload, InMemoryFileUpload, OnDiskFileUpload}
import com.twitter.finagle.http.{FileElement, FormElement}
import com.twitter.io.{Buf, Bufs}

sealed trait MultiPartFile {
  def toFormElement(name: String): FormElement
}

object MultiPartFile {
  def apply(fileUpload: FileUpload): MultiPartFile = fileUpload match {
    case InMemoryFileUpload(content, fileType, name, _) => InMemoryMultiPartFile(content, Option(fileType), Option(name))
    case OnDiskFileUpload(file, fileType, name, _) => OnDiskMultiPartFile(file, Option(fileType), Option(name))
  }
}

/**
  * This is a multipart form file element that is under the max memory limit, and thus has been kept
  */
case class InMemoryMultiPartFile(content: Buf, contentType: Option[String] = None, filename: Option[String] = None) extends MultiPartFile {
  def toFormElement(name: String): FormElement = FileElement(name, content, contentType, filename)
}

/**
  * This is a multipart form file element that is over the max memory limit, and thus has been stored on disk temporarily
  */
case class OnDiskMultiPartFile(content: File, contentType: Option[String] = None, filename: Option[String] = None) extends MultiPartFile {

  def toFormElement(name: String): FormElement = FileElement(name, toBuffer(), contentType, filename)

  private def toBuffer() = {
    val channel = new RandomAccessFile(content, "r").getChannel
    Bufs.ownedBuf(channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size()))
  }
}
