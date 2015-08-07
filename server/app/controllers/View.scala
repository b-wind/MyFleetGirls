package controllers

import build.BuildInfo
import play.api.mvc._
import models.db

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future
import scala.io.Source

/**
 *
 * @author ponkotuy
 * Date 14/02/24
 */
object View extends Controller {
  import Common._

  def index = Action.async {
    Future {
      val newest = db.Admiral.findNewestWithLv(limit = 20)
      val activeIds = db.Ship.findAllActiveUser(limit = 20)
      val actives = db.Admiral.findAllInWithLv(activeIds)
      val baseCounts = db.UserSettings.countAllByBase()
      Ok(views.html.index(BuildInfo.version, newest, actives, baseCounts))
    }
  }

  def login(init: String, back: String) = actionAsync { request =>
    request.session.get("key").map { key =>
      Redirect(back).withNewSession
    }.getOrElse {
      val baseCounts = db.UserSettings.countAllByBase()
      Ok(views.html.login(init, back, baseCounts))
    }
  }

  def modalMap(areaId: Int, infoNo: Int, cell: Int) = actionAsync(Ok(views.html.modal_map(areaId, infoNo, cell)))
  def modalMapLine(areaId: Int, infoNo: Int, dep: Int, dest: Int) = actionAsync(Ok(views.html.modal_map_line(areaId, infoNo, dep, dest)))
}
