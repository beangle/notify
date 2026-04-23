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

package org.beangle.notify

import org.beangle.commons.lang.Consoles
import org.beangle.notify.mail.MailShell
import org.beangle.notify.sms.SmsShell

/** 库默认入口：按参数或交互选择进入 `SmsShell` / `MailShell`。 */
object Main {

  def main(args: Array[String]): Unit = {
    val app = chooseApp(args.headOption)
    app match
      case "sms" =>
        SmsShell.run(args.drop(1))
      case "mail" =>
        MailShell.run(args.drop(1))
  }

  private def chooseApp(arg: Option[String]): String = {
    arg.map(_.trim.toLowerCase) match
      case Some(v) if Set("sms", "mail").contains(v) => v
      case _ =>
        val v = Consoles.prompt("app (sms|mail): ", "sms").trim.toLowerCase
        if Set("sms", "mail").contains(v) then v else "sms"
  }
}
