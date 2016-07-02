package io.fintrospect.templating

/**
  * Supported template implementations for templating engine implementations
  */
trait Templates {

  /**
    * Loads and caches templates from the compiled classpath
    * @param baseClasspathPackage the root package to load from (defaults to
    */
  def CachingClasspath(baseClasspathPackage: String = "."): TemplateRenderer

  /**
    * Load and caches templates from a file path
    * @param baseTemplateDir the root path to load templates from
    */
  def Caching(baseTemplateDir: String): TemplateRenderer

  /**
    * Hot-reloads (no-caching) templates from a file path
    * @param baseTemplateDir the root path to load templates from
    */
  def HotReload(baseTemplateDir: String): TemplateRenderer
}
