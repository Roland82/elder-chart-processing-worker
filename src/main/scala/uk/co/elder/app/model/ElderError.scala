package uk.co.elder.app.model

import scalaz.Maybe

case class ElderError(message: String, ex: Maybe[Exception])
