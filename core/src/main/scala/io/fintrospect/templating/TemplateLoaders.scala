package io.fintrospect.templating

/**
  * Supported template loaders for templating engine implementations
  */
trait TemplateLoaders[T] {

  /**
    * Loads and caches templates from the compiled classpath
    * @param baseClasspathPackage the root package to load from (defaults to
    */
  def CachingClasspath(baseClasspathPackage: String): TemplateLoader[T]

  /**
    * Load and caches templates from a file path
    * @param baseTemplateDir the root path to load templates from
    */
  def Caching(baseTemplateDir: String): TemplateLoader[T]

  /**
    * Hot-reloads (no-caching) templates from a file path
    * @param baseTemplateDir the root path to load templates from
    */
  def HotReload(baseTemplateDir: String): TemplateLoader[T]
}
