package honor

import models.db.Ship
import ranking.EvolutionBase
import scala.collection.breakOut

object Fetishism extends HonorCategory {
  override def category: Int = 18

  override def comment: String = "対象艦娘の合計Lvを一定以上"

  val groups = Vector(
    FetiGroup("メガネフェチ", Set(85, 143, 442, 69, 183, 31, 154, 128, 134), 450)
  )

  override def approved(memberId: Long): List[String] = {
    val ships = Ship.findAllByUserWithName(memberId)
    val lvs = ships.map { ship =>
      EvolutionBase(ship.shipId) -> ship.lv
    }
    groups.filter { gr =>
      val userSumLv = lvs.filter { case (sid, _) => gr.ships.contains(sid) }
        .map { case (_, lv) => lv.toInt }.sum
      userSumLv >= gr.sumLv
    }.map(_.name)(breakOut)
  }
}

case class FetiGroup(name: String, ships: Set[Int], sumLv: Int)
