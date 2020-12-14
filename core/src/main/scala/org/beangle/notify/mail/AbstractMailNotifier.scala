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

import org.beangle.commons.lang.Throwables
import org.beangle.commons.logging.Logging
import org.beangle.notify.{Message, NotificationException, Notifier}

abstract class AbstractMailNotifier(val mailSender: MailSender, val from: String) extends Notifier with Logging {

  override def getType: String = "mail"

  override def deliver(message: Message): Unit = {
    message match {
      case msg: MailMessage =>
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
      case _ => logger.warn("Mail Notifier should deliver mail message only")
    }
  }

  protected def buildSubject(msg: Message): String

  protected def buildText(msg: Message): String

  protected def beforeSend(msg: Message): Unit = {}

  protected def afterSend(msg: Message): Unit = {}
}
