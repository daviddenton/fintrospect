package io.fintrospect.util

import java.io.File
import java.lang.management.ManagementFactory.getPlatformMXBeans
import java.time.Clock
import java.time.ZonedDateTime.now
import java.time.format.DateTimeFormatter.ISO_DATE_TIME

import com.sun.management.HotSpotDiagnosticMXBean
import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request, Response}
import com.twitter.io.Readers
import com.twitter.util.Future
import io.fintrospect.ContentType
import io.fintrospect.formats.ResponseBuilder.HttpResponse

/**
  * Service to create and download a heap-dump of the current process. Will only work with Oracle JDK
  * installations. Since this is obviously an expensive operation (it creates a temp file on disc which is deleted
  * when the request completes) don't bind this service to a publicly available endpoint.
  */
class HeapDump(processIdentifier: String = "", clock: Clock = Clock.systemUTC()) extends Service[Request, Response] {
  override def apply(request: Request): Future[Response] = {
    val dumpFileName = s"heapdump-$processIdentifier-${now(clock).format(ISO_DATE_TIME)}"
    val dumpFile = File.createTempFile(dumpFileName, ".hprof")
    dumpFile.delete()

    getPlatformMXBeans(classOf[HotSpotDiagnosticMXBean]).get(0).dumpHeap(dumpFile.getAbsolutePath, true)

    val response = HttpResponse(ContentType("application/x-heap-dump"))
      .withHeaders("Content-disposition" -> ("inline; filename=\"" + dumpFileName + ".hprof\""))
      .withContent(Readers.newFileReader(dumpFile)).build()

    Future(response).ensure(dumpFile.delete())
  }
}
