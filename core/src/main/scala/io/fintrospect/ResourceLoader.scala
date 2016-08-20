package io.fintrospect

import java.io.File
import java.net.URL

trait ResourceLoader {
  def load(path: String): URL
}

object ResourceLoader {
  /**
    * Load static resources from the classpath path. Useful when you're just loading from a JAR.
    * @param basePath
    */
  def Classpath(basePath: String = "/") = new ResourceLoader {
    val withStarting = if (basePath.startsWith("/")) basePath else "/" + basePath
    val finalBasePath = if (withStarting.endsWith("/")) withStarting else withStarting + "/"

    override def load(path: String): URL = getClass.getResource(finalBasePath + path)
  }

  /**
    * Load static resources from a directory path. Using this version allows for hot-reload from the file system.
    * @param baseDir
    */
  def Directory(baseDir: String) = new ResourceLoader {
    val finalBaseDir = if (baseDir.endsWith("/")) baseDir else baseDir + "/"
    override def load(path: String): URL = {
      val f = new File(finalBaseDir, path)
      if(f.exists()) f.toURI.toURL else null
    }
  }
}
