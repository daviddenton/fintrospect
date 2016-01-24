package io.fintrospect.templating

class ViewNotFound(view: View) extends Exception(s"Template ${view.template} not found")
