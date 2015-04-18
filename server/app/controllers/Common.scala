package controllers

import models.join.{Activity, User}
import models.db
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits._
import org.json4s._
import org.json4s.native.{ JsonMethods => J }
import org.json4s.native.Serialization.write
import scala.concurrent.Future
import scalikejdbc._
import com.ponkotuy.data.{MyFleetAuth, Auth}
import tool.Authentication

/**
 *
 * @author ponkotuy
 * Date: 14/02/25
 */
object Common extends Controller {
  type Req = Map[String, Seq[String]]
  implicit val formats = DefaultFormats

  def authAndParse[T](f: (db.Admiral, T) => Result)(implicit mf: Manifest[T]): Action[Req] = {
    Action.async(parse.urlFormEncoded) { request =>
      authentication(request.body) { auth =>
        withData[T](request.body) { data =>
          f(auth, data)
        }
      }
    }
  }

  /** 実際はCheckしてないです */
  def checkPonkotuAndParse[T](f: (T) => Result)(implicit mf: Manifest[T]): Action[Req] = {
    Action.async(parse.urlFormEncoded(1024*1024*2)) { request =>
      checkPonkotu(request.body) {
        withData[T](request.body) { data =>
          f(data)
        }
      }
    }
  }

  /**
   * 1. 旧ログイン系は必ず必要（さもないとデータが不足する）
   * 2. 新ログイン系は任意だが、一度でも認証させたら通さないと駄目
   */
  def authentication(request: Req)(f: (db.Admiral) => Result): Future[Result] = {
    Future {
      reqHeadParse[Auth](request)("auth") match {
        case Some(oldAuth) =>
          Authentication.oldAuth(oldAuth) match {
            case Some(ad) =>
              reqHeadParse[MyFleetAuth](request)("auth2") match {
                case Some(auth) =>
                  if(auth.id == oldAuth.memberId && Authentication.myfleetAuthOrCreate(auth)) f(ad)
                  else Unauthorized("Password Authentication Failed")
                case None =>
                  if(db.MyFleetAuth.find(oldAuth.memberId).isEmpty) f(ad)
                  else Unauthorized("Password Required")
              }
            case None => Unauthorized("Old Protocol Authentication Failed")
          }
        case None => Unauthorized("Authentication Data Required")
      }
    }
  }

  /** Checkしなくなりました */
  def checkPonkotu(request: Req)(f: => Result): Future[Result] = {
    Future {
      f
      Ok("Success")
    }
  }

  def withData[T](request: Req)(f: T => Result)(implicit mf: Manifest[T]): Result = {
    val result = for {
      json <- reqHead(request)("data")
      data <- J.parse(json).extractOpt[T]
    } yield { f(data) }
    result.getOrElse(BadRequest("Request Error(JSON Parse Error? Header?)"))
  }

  def userView(memberId: Long)(f: User => Result): Action[AnyContent] = actionAsync { request =>
    getUser(memberId, uuidCheck(memberId, request.session.get("key"))) match {
      case Some(user) => f(user)
      case _ => NotFound("ユーザが見つかりませんでした")
    }
  }

  def reqHead(request: Req)(key: String): Option[String] = {
    for {
      results <- request.get(key)
      one <- results.headOption
    } yield one
  }

  def reqHeadParse[T](request: Req)(key: String)(implicit m: Manifest[T]): Option[T] = {
    for {
      head <- reqHead(request)(key)
      result <- J.parse(head).extractOpt[T]
    } yield result
  }

  def getUser(memberId: Long, logined: Boolean): Option[User] = {
    for {
      auth <- db.Admiral.find(memberId)
      basic <- db.Basic.findByUser(memberId)
    } yield {
      val notClear = db.MapInfo.findAllBy(sqls"member_id = ${memberId} and cleared = false")
      val nextMapView = if(notClear.isEmpty) {
        if(db.MapInfo.find(54, memberId).isDefined) "全海域クリア" else "海域進捗未登録"
      } else {
        notClear.map(_.abbr).mkString("", ", ", "海域の攻略中")
      }
      val settings = db.UserSettings.find(memberId).getOrElse(db.UserSettings.empty(memberId))
      User(auth, basic, nextMapView, settings, if(logined) Some(memberId) else None)
    }
  }

  def uuidCheck(memberId: Long, key: Option[String]): Boolean = {
    val result = for {
      k <- key
      session <- db.Session.findByUser(memberId)
    } yield session.uuid.toString == k
    result.getOrElse(false)
  }

  def returnJson[A <: AnyRef](f: => A): Action[AnyContent] = returnJsonReq(_ => f)

  def returnJsonReq[A <: AnyRef](f: Request[AnyContent] => A): Action[AnyContent] = Action.async { req =>
    Future {
      try {
        Ok(write(f(req))).as("application/json")
      } catch {
        case e: IllegalArgumentException => BadRequest(e.getMessage)
      }
    }
  }

  def returnString[A](f: => A) = actionAsync { Ok(f.toString) }

  def actionAsync(f: => Result) = Action.async { request =>
    Future {
      try {
        f
      } catch {
        case e: IllegalArgumentException => BadRequest(e.getMessage)
      }
    }
  }

  def actionAsync(f: (Request[AnyContent]) => Result) = Action.async { request =>
    Future {
      try {
        f(request)
      } catch {
        case e: IllegalArgumentException => BadRequest(e.getMessage)
      }
    }
  }

  def formAsync(f: Request[Map[String, Seq[String]]] => Result) = Action.async(parse.urlFormEncoded) { request =>
    Future {
      try {
        f(request)
      } catch {
        case e: IllegalArgumentException => BadRequest(e.getMessage)
      }
    }
  }

  val NGStage = Set((1, 1), (2, 2), (2, 3))
  def readActivities(from: Long, limit: Int, offset: Int, memberId: Long = 0L): List[Activity] = {
    def f = whereMember(_: SQLSyntax, memberId)
    val started = db.MapRoute.findWithUserBy(sqls"mr.created > ${from} ${f(sqls"mr.member_id")}", limit*8, offset)
      .filter(_.start.exists(_.start))
      .filterNot { it => NGStage.contains((it.areaId, it.infoNo)) }
    val rares = db.MasterShipOther.findAllBy(sqls"mso.backs >= 5").map(_.id).toSet
    val drops = db.BattleResult.findWithUserBy(sqls"br.created > ${from} and br.get_ship_id is not null ${f(sqls"br.member_id")}", limit*8, offset)
      .filter(_.getShipId.exists(rares.contains))
    val rareItems = db.MasterSlotItem.findAllBy(sqls"msi.rare >= 1").map(_.id).toSet
    val createItems = db.CreateItem.findWithUserBy(sqls"ci.created > ${from} ${f(sqls"ci.member_id")}", limit, offset)
      .filter { it => rareItems.contains(it.itemId) }
    val createShips = db.CreateShip.findWithUserBy(sqls"cs.created > ${from} ${f(sqls"cs.member_id")}", limit, offset)
      .filter { it => rares.contains(it.shipId) }
    (started ++ drops ++ createItems ++ createShips).sortBy(-_.created).take(limit)
  }

  private def whereMember(col: SQLSyntax, memberId: Long) = if(memberId == 0L) sqls"" else sqls"and ${col} = ${memberId}"


}
