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
package org.beangle.notify.service

import org.beangle.commons.logging.Logging
import org.beangle.notify.{MessageQueue, NotificationException, NotificationTask, Notifier}

class DefaultNotificationTask(val notifier: Notifier, val queue: MessageQueue = new DefaultMessageQueue)
  extends NotificationTask with Logging {

  var observer: SendingObserver = _

  private var taskInterval: Long = 0

  def send(): Unit = {
    var msg = queue.poll()
    while (null != msg) {
      try {
        if (null != observer) observer.onStart(msg)
        notifier.deliver(msg)
        if (taskInterval > 0) Thread.sleep(taskInterval)
      } catch {
        case e: NotificationException => logger.error("send error", e)
        case e: InterruptedException => logger.error("send error", e)
      }
      if (null != observer) observer.onFinish(msg)
      msg = queue.poll()
    }
  }

}
