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
package org.beangle.notify.mail

import org.beangle.notify.Notifier

import org.beangle.notify.Message
import org.beangle.commons.lang.Assert
import org.beangle.commons.lang.Strings
import org.beangle.notify.NotificationException
import org.beangle.commons.lang.Throwables
import org.beangle.commons.logging.Logging

abstract class AbstractMailNotifier[T <: MailMessage](val mailSender: MailSender,val from: String) extends Notifier[T] with Logging {

  def getType(): String = "mail"

  def deliver(msg: T): Unit = {
    beforeSend(msg)
    try {
      if (null == msg.from && null != from) {
        msg.from(from)
      }
      mailSender.send(msg)
      afterSend(msg)
    } catch {
      case e: NotificationException =>
        logger.error("Cannot send message " + msg.subject, e)
        Throwables.propagate(e)
    }
  }

  protected def buildSubject(msg: Message): String

  protected def buildText(msg: Message): String

  protected def beforeSend(msg: Message): Unit = {}

  protected def afterSend(msg: Message): Unit = {}
}
