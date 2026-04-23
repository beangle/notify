/*
 * Copyright (C) 2005, The Beangle Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.beangle.notify.sms

import org.beangle.commons.lang.Consoles

/** 交互式命令行调试 `SmsSender`。由 `org.beangle.notify.Main` 调度。 */
object SmsShell {

  private var conf: CliConfig = _

  def run(args: Array[String]): Unit = {
    val vendor = chooseVendor(args.headOption)
    val sender = findSender(vendor)

    println(s"Ready. vendor=$vendor")
    println("Commands:")
    println("  send <mobile> <message>")
    println("  show")
    println("  help")
    println("  quit")

    repl(sender)
  }

  private def printResult(res: SmsResponse): Unit = {
    if (res.isOk) println(s"OK: ${res.message}")
    else System.err.println(s"Failure: ${res.message}")
  }

  private final class CliConfig(val vendor: String, var endpoint: String, var appId: String, var appSecret: String)

  private def chooseVendor(argVendor: Option[String]): String = {
    argVendor.map(_.trim.toLowerCase) match
      case Some(v) if Set("lixin", "b2m", "ecupl").contains(v) => v
      case _ =>
        val v = Consoles.prompt("vendor (lixin|b2m|ecupl): ", "lixin").trim.toLowerCase
        if Set("lixin", "b2m", "ecupl").contains(v) then v else "lixin"
  }

  private def findSender(vendor: String): SmsSender = {
    val endpoint = Consoles.prompt(s"endpoint: ")
    val appId = Consoles.prompt("appId: ")
    val appSecret = Consoles.prompt("appSecret: ")

    this.conf = new CliConfig(vendor, endpoint, appId, appSecret)
    import SmsSenderFactory.*
    createSender(Map(Vendor -> vendor, EndPoint -> endpoint, AppId -> appId, AppSecret -> appSecret))
  }

  private def repl(sender: SmsSender): Unit = {
    Consoles.shell("sms> ", Set("quit", "exit", "q")) { trimmed =>
      val lower = trimmed.toLowerCase
      if (lower == "help") {
        println("send <mobile> <message>")
        println("show")
        println("quit")
      } else if (lower == "show") {
        showConfig()
      } else if (lower.startsWith("send ")) {
        handleSend(trimmed.substring(5), sender)
      } else if (trimmed.nonEmpty) {
        println("unknown command, use help")
      }
    }
  }

  private def handleSend(raw: String, sender: SmsSender): Unit = {
    val idx = raw.indexOf(' ')
    if (idx <= 0 || idx == raw.length - 1) {
      println("usage: send <mobile> <message>")
      return
    }
    val mobile = raw.substring(0, idx).trim
    val message = raw.substring(idx + 1).trim
    val res = sender.send(Receiver(mobile, name = mobile), message)
    printResult(res)
  }

  private def showConfig(): Unit = {
    println(s"vendor=${conf.vendor}")
    println(s"endpoint=${conf.endpoint}")
    println(s"appId=${conf.appId}")
    println(s"appSecret=${maskSecret(conf.appSecret)}")
  }

  /** 仅用于 `show` 输出，保留首尾各 2 字符便于对照是否用错密钥，中间固定掩码。 */
  private def maskSecret(secret: String): String = {
    val s = secret.trim
    if s.isEmpty then "(empty)"
    else if s.length <= 4 then "****"
    else s"${s.substring(0, 2)}****${s.substring(s.length - 2)}"
  }

}
