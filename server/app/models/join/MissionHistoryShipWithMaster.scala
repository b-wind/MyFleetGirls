package models.join

import models.db._
import org.json4s._
import org.json4s.JsonDSL._

/**
 * @author ponkotuy
 * Date: 15/01/17.
 */
case class MissionHistoryShipWithMaster(ship: MissionHistoryShip, master: MasterShipBase, stype: MasterStype) {
  def name = master.name
  def lv = ship.lv
  def missionId = ship.missionId

  def toJson: JObject = {
    ("id" -> ship.id) ~
      ("shipId" -> ship.shipId) ~
      ("name" -> name) ~
      ("lv" -> lv.toInt) ~
      ("stype" -> stype.name)
  }
}
