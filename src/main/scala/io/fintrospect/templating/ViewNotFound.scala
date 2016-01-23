package io.fintrospect.templating

class ViewNotFound(view: View) extends Exception(s"Can't find template for View ${view.template}")
