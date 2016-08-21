package io.fintrospect

import java.io.File
import java.net.URL

trait ResourceLoader {
  def load(path: String): URL
}

object ResourceLoader {
  /**
    * Load static resources from the classpath path. Useful when you're just loading from a JAR. Note that due to security concerns
    * this should be with a package which does NOT contain class files.
    * @param basePackagePath
    */
  def Classpath(basePackagePath: String = "/") = new ResourceLoader {
    val withStarting = if (basePackagePath.startsWith("/")) basePackagePath else "/" + basePackagePath
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
      if(f.exists() && f.isFile) f.toURI.toURL else null
    }
  }
}
