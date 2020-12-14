/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2014, Beangle Software.
 *
 * Beangle is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Beangle is distributed in the hope that it will be useful.
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Beangle.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.notify.service

import org.beangle.commons.logging.Logging
import org.beangle.notify.NotificationTask
import org.beangle.notify.Message
import org.beangle.notify.MessageQueue
import org.beangle.notify.Notifier
import org.beangle.notify.NotificationException

class DefaultNotificationTask[T <: Message](val notifier: Notifier[T], val queue: MessageQueue[T] = new DefaultMessageQueue[T])
  extends NotificationTask[T] with Logging {

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
