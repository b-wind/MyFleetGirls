package ranking

import controllers.routes
import models.join.ShipWithName
import org.json4s.JValue
import ranking.common.{RankingData, RankingElement, Ranking}
import ranking.data.ShipMini
import scalikejdbc._
import models.db._
import scala.concurrent.duration._

/**
 *
 * @author ponkotuy
 * Date: 14/10/09.
 */
case object FirstShipRanking extends Ranking {
  import Ranking._

  def a = Admiral.a

  // 以下はDBをinitializeしないといけないのでlazyにしておく(さもないとtestでこける)
  lazy val s = Ship.syntax("s")
  lazy val ms = MasterShipBase.syntax("ms")
  lazy val mst = MasterStype.syntax("mst")
  lazy val mss = MasterShipSpecs.syntax("mss")

  override val id = 2
  override val title: String = "初期艦Lv"
  override val divClass: String = collg3
  override val comment: List[String] = List(comment7days)

  override def rankingQuery(limit: Int): List[RankingElement] = {
    findAllByOrderByExp(sqls"s.id = 1", limit, agoMillis(7.days)).map { case (admiral, ship) =>
      val url = routes.UserView.top(admiral.id).toString
      RankingElement(admiral.id, admiral.nickname, ShipMini.toData(ship), url, ship.exp)
    }
  }

  private def findAllByOrderByExp(where: SQLSyntax, limit: Int = 10, from: Long = 0L)(
    implicit session: DBSession = Ship.autoSession): List[(Admiral, ShipWithName)] = {
    withSQL {
      select.from(Ship as s)
        .innerJoin(Admiral as a).on(s.memberId, a.id)
        .innerJoin(MasterShipBase as ms).on(s.shipId, ms.id)
        .innerJoin(MasterStype as mst).on(ms.stype, mst.id)
        .innerJoin(MasterShipSpecs as mss).on(s.shipId, mss.id)
        .where.gt(s.created, from).and.append(where)
        .orderBy(s.exp).desc
        .limit(limit)
    }.map { rs =>
      Admiral(a)(rs) -> ShipWithName(Ship(s, Nil)(rs), MasterShipBase(ms)(rs), MasterStype(mst)(rs), MasterShipSpecs(mss)(rs))
    }.list().apply()
  }

  // JSONになったRankingDataをdeserializeする
  override def decodeData(v: JValue): Option[RankingData] = ShipMini.decode(v)
}
