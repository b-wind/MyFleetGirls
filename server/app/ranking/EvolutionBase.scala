package ranking

import models.db.MasterShipAfter
import util.PeriodicalCache

import scala.concurrent.duration._

/**
 * Thread Safe
 *
 * @author ponkotuy
 * Date: 15/01/10.
 */
object EvolutionBase {
  def apply(shipId: Int): Int = {
    Afters.get(shipId) match {
      case Some(afterId) => apply(afterId)
      case None => shipId
    }
  }

  def isBase(shipId: Int): Boolean = Afters.get(shipId).isEmpty

  // 大鯨→龍鳳、U-511→呂500など名称変更が入る艦娘のid mapping
  val Aliases = Map(431 -> 436, 184 -> 185, 35 -> 147, 441 -> 446)

  private[this] def createCache() =
    MasterShipAfter.findAll()
        .filterNot(_.aftershipid == 0)
        .map { ship => ship.aftershipid -> ship.id }.toMap

  val Afters = new PeriodicalCache(60.minutes, createCache)
}
