package models.db

import models.join.{ShipWithName, DeckShipWithName}
import scalikejdbc._
import util.scalikejdbc.BulkInsert._

/**
 *
 * @param deckId : 艦隊No. 1-indexed
 * @param num : 順番（旗艦0） // 0-indexed
 */
case class DeckShip(deckId: Int, num: Int, memberId: Long, shipId: Int)

object DeckShip extends SQLSyntaxSupport[DeckShip] {
  def apply(x: SyntaxProvider[DeckShip])(rs: WrappedResultSet): DeckShip = apply(x.resultName)(rs)
  def apply(x: ResultName[DeckShip])(rs: WrappedResultSet): DeckShip = autoConstruct(rs, x)

  lazy val ds = DeckShip.syntax("ds")
  lazy val s = Ship.syntax("s")
  lazy val ms = MasterShipBase.syntax("ms")
  lazy val mst = MasterStype.syntax("mst")
  lazy val mss = MasterShipSpecs.syntax("mss")

  def find(memberId: Long, deckId: Int, num: Int)(implicit session: DBSession = autoSession): Option[DeckShip] =
    withSQL {
      select.from(DeckShip as ds)
        .where.eq(ds.memberId, memberId).and.eq(ds.deckId, deckId).and.eq(ds.num, num)
    }.map(DeckShip(ds)).single().apply()

  def findByShipId(memberId: Long, shipId: Int)(implicit session: DBSession = autoSession): Option[DeckShip] = {
    withSQL {
      select.from(DeckShip as ds)
        .where.eq(ds.memberId, memberId).and.eq(ds.shipId, shipId)
    }.map(DeckShip(ds)).single().apply()
  }

  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[DeckShip] = withSQL {
    select.from(DeckShip as ds).where(where)
  }.map(DeckShip(ds)).list().apply()

  def findAllByUserWithName(memberId: Long)(implicit session: DBSession = autoSession): List[DeckShipWithName] = {
    withSQL {
      select(ds.deckId, ds.num, ds.memberId, ds.shipId, s.lv, s.cond, ms.name)
        .from(DeckShip as ds)
        .innerJoin(Ship as s).on(sqls"${ds.shipId} = ${s.id} and ${ds.memberId} = ${s.memberId}")
        .innerJoin(MasterShipBase as ms).on(s.shipId, ms.id)
        .where.eq(ds.memberId, memberId).orderBy(ds.deckId, ds.num)
    }.map(DeckShipWithName(ds, s, ms)).list().apply()
  }

  def findAllByDeck(memberId: Long, deckId: Int)(implicit session: DBSession = autoSession): List[ShipWithName] = {
    val slots = ShipSlotItem.findAllBy(sqls"member_id = ${memberId}")
    withSQL {
      select.from(DeckShip as ds)
        .innerJoin(Ship as s).on(sqls"${ds.shipId} = ${s.id} and ${ds.memberId} = ${s.memberId}")
        .innerJoin(MasterShipBase as ms).on(s.shipId, ms.id)
        .innerJoin(MasterStype as mst).on(ms.stype, mst.id)
        .innerJoin(MasterShipSpecs as mss).on(s.shipId, mss.id)
        .where.eq(ds.memberId, memberId).and.eq(ds.deckId, deckId)
        .orderBy(ds.num)
    }.map { rs =>
      val id = rs.int(s.resultName.id)
      val slot = slots.filter(_.shipId == id).map(_.slotitemId)
      ShipWithName(Ship(s, slot)(rs), MasterShipBase(ms)(rs), MasterStype(mst)(rs), MasterShipSpecs(mss)(rs))
    }.list().apply()
  }

  def findFlagshipByUserWishShipName(memberId: Long)(implicit session: DBSession = autoSession): Option[ShipWithName] = {
    val slots = ShipSlotItem.findAllBy(sqls"member_id = ${memberId}")
    withSQL {
      select.from(DeckShip as ds)
        .innerJoin(Ship as s).on(ds.shipId, s.id)
        .innerJoin(MasterShipBase as ms).on(s.shipId, ms.id)
        .innerJoin(MasterStype as mst).on(ms.stype, mst.id)
        .innerJoin(MasterShipSpecs as mss).on(s.shipId, mss.id)
        .where.eq(ds.memberId, memberId).and.eq(s.memberId, memberId).and.eq(ds.deckId, 1).and.eq(ds.num, 0)
    }.map { rs =>
      val id = rs.int(s.resultName.id)
      val slot = slots.filter(_.shipId == id).map(_.slotitemId)
      ShipWithName(Ship(s, slot)(rs), MasterShipBase(ms)(rs), MasterStype(mst)(rs), MasterShipSpecs(mss)(rs))
    }.first().apply()
  }

  def create(deckId: Int, num: Int, memberId: Long, shipId: Int)(
    implicit session: DBSession = DeckShip.autoSession): DeckShip = {
    applyUpdate {
      insert.into(DeckShip).namedValues(
        column.deckId -> deckId, column.num -> num, column.memberId -> memberId, column.shipId -> shipId
      )
    }
    DeckShip(deckId, num, memberId, shipId)
  }

  def bulkInsert(deckId: Int, memberId: Long, ships: List[Int])(
    implicit session: DBSession = DeckShip.autoSession): List[DeckShip] = {
    if(ships.isEmpty) Nil
    else {
      deleteByDeck(deckId, memberId)
      applyUpdate {
        insert.into(DeckShip)
          .columns(column.deckId, column.num, column.memberId, column.shipId)
          .multiValues(Seq.fill(ships.size)(deckId), Seq.range(0, ships.size), Seq.fill(ships.size)(memberId), ships)
      }
      ships.zip(Stream.from(0)).map { case (ship, num) =>
        DeckShip(deckId, num, memberId, ship)
      }
    }
  }

  def deleteByDeck(deckId: Int, memberId: Long)(
    implicit session: DBSession = DeckShip.autoSession): Unit = applyUpdate {
    delete.from(DeckShip)
      .where.eq(DeckShip.column.memberId, memberId).and.eq(DeckShip.column.deckId, deckId)
  }

  def deleteByUser(memberId: Long)(
    implicit session: DBSession = DeckShip.autoSession): Unit = applyUpdate {
    delete.from(DeckShip)
      .where.eq(DeckShip.column.memberId, memberId)
  }
}
