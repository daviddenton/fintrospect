package examples.full.test.env

import com.twitter.finagle.http.Status

case class ResponseStatusAndContent(status: Status, contentType: String, content: String)
