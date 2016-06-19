package io.fintrospect.util

import java.time.ZoneId.systemDefault
import java.time.{Clock, Instant, ZoneId}

object TestClocks {
  val ticking = new Clock {
    private var current = Instant.now()

    override def getZone: ZoneId = systemDefault()

    override def instant(): Instant = {
      current = current.plusSeconds(1)
      current
    }

    override def withZone(zone: ZoneId): Clock = this
  }

  val fixed = new Clock {
    private val current = Instant.now()

    override def getZone: ZoneId = systemDefault()

    override def instant(): Instant = current

    override def withZone(zone: ZoneId): Clock = this
  }

}
