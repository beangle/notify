/*
 * Beangle, Agile Development Scaffold and Toolkits.
 *
 * Copyright © 2005, The Beangle Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.notify

import org.beangle.commons.logging.Logging

object SendingObserver {

  object Log extends SendingObserver with Logging {

    def onStart(msg: Message): Unit = {
      logger.info(s"开始发送${msg.subject}到${msg.recipients.head}...")
    }

    def onFinish(msg: Message): Unit = {
      logger.info(s"结束发送${msg.subject}到${msg.recipients.head}")
    }

    def onFail(e: Exception): Unit = {
      logger.error("发送发生错误", e)
    }
  }

}

trait SendingObserver {

  def onStart(msg: Message): Unit

  def onFinish(msg: Message): Unit

  def onFail(e: Exception): Unit
}
