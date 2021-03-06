package models.db

import scalikejdbc._
import com.ponkotuy.data
import util.scalikejdbc.BulkInsert._
import scala.collection.breakOut
import scala.concurrent.duration._

case class ShipHistory(
  shipId: Int,
  memberId: Long,
  lv: Short,
  exp: Int,
  created: Long) {

  def save()(implicit session: DBSession = ShipHistory.autoSession): ShipHistory = ShipHistory.save(this)(session)

  def destroy()(implicit session: DBSession = ShipHistory.autoSession): Unit = ShipHistory.destroy(this)(session)

  def diff(x: data.Ship): Double = {
    import com.ponkotuy.tool.DiffCalc._
    Seq(
      neq(lv, x.lv),
      diffRatio(10000.0)(exp, x.exp),
      diffRatio(7.day.toMillis)(created, System.currentTimeMillis())
    ).max
  }

}


object ShipHistory extends SQLSyntaxSupport[ShipHistory] {

  override val tableName = "ship_history"

  override val columns = Seq("ship_id", "member_id", "lv", "exp", "created")

  def apply(sh: SyntaxProvider[ShipHistory])(rs: WrappedResultSet): ShipHistory = autoConstruct(rs, sh)
  def apply(sh: ResultName[ShipHistory])(rs: WrappedResultSet): ShipHistory = autoConstruct(rs, sh)

  val sh = ShipHistory.syntax("sh")
  val sh2 = ShipHistory.syntax("sha") // Subquery用

  override val autoSession = AutoSession

  def find(memberId: Long, shipId: Int, created: Long)(implicit session: DBSession = autoSession): Option[ShipHistory] = {
    withSQL {
      select.from(ShipHistory as sh)
          .where.eq(sh.memberId, memberId)
            .and.eq(sh.shipId, shipId)
            .and.eq(sh.created, created)
    }.map(ShipHistory(sh.resultName)).single().apply()
  }

  def findAll()(implicit session: DBSession = autoSession): List[ShipHistory] = {
    withSQL(select.from(ShipHistory as sh)).map(ShipHistory(sh.resultName)).list().apply()
  }

  def countAll()(implicit session: DBSession = autoSession): Long = {
    withSQL(select(sqls.count).from(ShipHistory as sh)).map(rs => rs.long(1)).single().apply().get
  }

  def findBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Option[ShipHistory] = {
    withSQL {
      select.from(ShipHistory as sh).where.append(where)
    }.map(ShipHistory(sh.resultName)).single().apply()
  }

  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[ShipHistory] = {
    withSQL {
      select.from(ShipHistory as sh).where.append(where)
    }.map(ShipHistory(sh.resultName)).list().apply()
  }

  def findAllLastest(where: SQLSyntax)(implicit session: DBSession = autoSession): List[ShipHistory] = {
    withSQL {
      select.from(ShipHistory as sh)
          .where.eq(
            sh.created,
            sqls"""(
              ${
                select(sqls.max(sh2.created)).from(ShipHistory as sh2)
                  .where.eq(sh.memberId, sh2.memberId).and.eq(sh.shipId, sh2.shipId).and.append(where).sql
              }
            )"""
          ).and.append(where)
    }.map(ShipHistory(sh)).list().apply()
  }

  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
    withSQL {
      select(sqls.count).from(ShipHistory as sh).where.append(where)
    }.map(_.long(1)).single().apply().get
  }

  def create(
      shipId: Int,
      memberId: Long,
      lv: Short,
      exp: Int,
      created: Long)(implicit session: DBSession = autoSession): Unit = applyUpdate {
    insert.into(ShipHistory).columns(
      column.shipId,
      column.memberId,
      column.lv,
      column.exp,
      column.created
    ).values(
      shipId,
      memberId,
      lv,
      exp,
      created
    )
  }

  def bulkInsert(ships: Seq[data.Ship], memberId: Long)(implicit session: DBSession = autoSession): Unit = {
    val lastsWhere = sqls.eq(sh.memberId, memberId)
        .and.in(sh.shipId, ships.map(_.id))
        .and.gt(sh.created, System.currentTimeMillis() - 12.hours.toMillis) // 8.4時間より前のデータはあっても必ず更新されるので、速度の為除外
    val lasts: Map[Int, ShipHistory] = findAllLastest(lastsWhere)
        .map(s => s.shipId -> s)(breakOut)
    val inserts = ships.filter(s => 0.05 <= lasts.get(s.id).map(_.diff(s)).getOrElse(Double.MaxValue))
    if(inserts.isEmpty) return
    val current = System.currentTimeMillis()
    val size = inserts.size
    applyUpdate {
      insert.into(ShipHistory).columns(
        column.shipId,
        column.memberId,
        column.lv,
        column.exp,
        column.created
      ).multiValues(
        inserts.map(_.id),
        Seq.fill(size)(memberId),
        inserts.map(_.lv),
        inserts.map(_.exp),
        Seq.fill(size)(current)
      )
    }
  }

  def save(entity: ShipHistory)(implicit session: DBSession = autoSession): ShipHistory = {
    applyUpdate {
      update(ShipHistory).set(
        column.shipId -> entity.shipId,
        column.memberId -> entity.memberId,
        column.lv -> entity.lv,
        column.exp -> entity.exp,
        column.created -> entity.created
      ).where.eq(column.memberId, entity.memberId)
        .and.eq(column.shipId, entity.shipId)
        .and.eq(column.created, entity.created)
    }
    entity
  }

  def destroy(entity: ShipHistory)(implicit session: DBSession = autoSession): Unit =
    applyUpdate {
      delete.from(ShipHistory)
          .where.eq(column.memberId, entity.memberId)
            .and.eq(column.shipId, entity.shipId)
            .and.eq(column.created, entity.created)
    }

}
