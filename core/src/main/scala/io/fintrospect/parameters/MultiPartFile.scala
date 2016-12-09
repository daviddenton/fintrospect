package io.fintrospect.parameters

import java.io.{File, RandomAccessFile}
import java.nio.channels.FileChannel.MapMode.READ_ONLY

import com.twitter.finagle.http.FileElement
import com.twitter.finagle.http.exp.Multipart.{FileUpload, InMemoryFileUpload, OnDiskFileUpload}
import com.twitter.io.Buf
import com.twitter.io.Bufs.ownedBuf

sealed trait MultiPartFile {
  def toFileElement(name: String): FileElement

  def isEmpty(): Boolean
}

object MultiPartFile {
  def apply(fileUpload: FileUpload): MultiPartFile = fileUpload match {
    case InMemoryFileUpload(content, fileType, name, _) => InMemoryMultiPartFile(name, content, Option(fileType))
    case OnDiskFileUpload(file, fileType, name, _) => OnDiskMultiPartFile(name, file, Option(fileType))
  }
}

/**
  * This is a multipart form file element that is under the max memory limit, and thus has been kept
  */
case class InMemoryMultiPartFile(filename: String, content: Buf, contentType: Option[String] = None) extends MultiPartFile {
  override def toFileElement(name: String): FileElement = FileElement(name, content, contentType, Some(filename))

  override def isEmpty(): Boolean = content.isEmpty
}

/**
  * This is a multipart form file element that is over the max memory limit, and thus has been stored on disk temporarily
  */
case class OnDiskMultiPartFile(filename: String, content: File, contentType: Option[String] = None) extends MultiPartFile {

  override def toFileElement(name: String): FileElement = FileElement(name, toBuffer, contentType, Some(filename))

  override def isEmpty(): Boolean = content.length() == 0

  private def toBuffer = {
    val channel = new RandomAccessFile(content, "r").getChannel
    ownedBuf(channel.map(READ_ONLY, 0, channel.size()))
  }
}
