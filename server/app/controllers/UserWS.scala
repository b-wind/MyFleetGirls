package controllers

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.mvc._
import play.api.libs.iteratee.{Enumerator, Iteratee}
import play.api.libs.concurrent.Promise
import org.json4s._
import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods._
import models.db

/**
 * @author ponkotuy
 * Date: 14/04/11.
 */
class UserWS {
  implicit val formats = DefaultFormats

  def info(memberId: Long) = WebSocket.using { request =>
    val in = Iteratee.consume[String]()
    val out = Enumerator.fromCallback1 { _ =>
      Promise.timeout(infoRaise(memberId), 60.seconds)
    }
    (in, out)
  }

  def infoRaise(memberId: Long): Option[String] = {
    val kdocks = db.KDock.findAllByUserWithName(memberId).map(Extraction.decompose)
    val ndocks = db.NDock.findAllByUserWithName(memberId).map(Extraction.decompose)
    val missions = db.Mission.findByUserWithFlagship(memberId).map(Extraction.decompose)
    val json = ("kdocks" -> kdocks) ~ ("ndocks" -> ndocks) ~ ("missions" -> missions)
    Some(compact(render(json)))
  }
}
