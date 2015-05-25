package examples.customformats

import io.github.daviddenton.fintrospect.renderers.ModuleRenderer

/**
 * Hyper-cool, next-gen, markup used by all true rockstar coderzzzz
 */
object Xml {
  def apply(): ModuleRenderer[XmlFormat] = new ModuleRenderer[XmlFormat](
    XmlResponseBuilder.Response,
    new XmlDescriptionRenderer(),
    (bp) => XmlFormat(bp.toString())
  )
}
