package com.ponkotuy.config

import java.io.File
import scala.util.Try
import com.typesafe.config.ConfigFactory
import com.ponkotuy.data.MyFleetAuth

/**
 *
 * @author ponkotuy
 * Date: 14/02/23
 */
object ClientConfig {
  lazy val config = {
    val file = new File("application.conf")
    if(file.exists()) {
      ConfigFactory.parseFile(file)
    } else {
      ConfigFactory.parseFile(new File("application.conf.sample"))
    }
  }

  lazy val post = config.getString("url.post")
  def postUrl(ver: Int = 1) = post + s"/post/v${ver}"
  def getUrl(ver: Int = 1) = config.getString("url.post") + s"/rest/v${ver}"
  def proxyPort = config.getInt("proxy.port")

  @deprecated("Move to Auth.master", "0.13.0")
  def master: Boolean = Auth.master
  def auth(memberId: Long): Option[MyFleetAuth] = Auth.password.map(p => MyFleetAuth(memberId, p))

  object Auth {
    val conf = Try { config.getConfig("auth") }.toOption
    val master: Boolean = Try { conf.get.getBoolean("master") }.getOrElse(false)
    val password: Option[String] = Try { conf.get.getString("pass") }.toOption
  }
}
