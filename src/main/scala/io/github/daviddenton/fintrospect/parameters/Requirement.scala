package io.github.daviddenton.fintrospect.parameters

sealed trait Requirement {
  val required: Boolean
}

object Requirement {
  val Mandatory = new Requirement {
    val required = true
  }

  val Optional = new Requirement {
    val required = false
  }
}
