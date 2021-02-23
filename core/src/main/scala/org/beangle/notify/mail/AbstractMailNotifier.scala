/*
 * Copyright (C) 2005, The Beangle Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful.
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.beangle.notify.mail

import org.beangle.commons.logging.Logging
import org.beangle.notify.{ Message, Notifier, SendingObserver }

abstract class AbstractMailNotifier(val mailSender: MailSender, val from: String) extends Notifier with Logging:

  override def getType: String = "mail"

  override def deliver(message: Message, observer: SendingObserver): Unit =
    deliver(List(message), observer)

  override def deliver(messages: Iterable[Message], observer: SendingObserver): Unit =
    messages foreach { message =>
      message match
        case msg: MailMessage =>
          if (null == msg.from && null != from)
            msg.from(from)
          mailSender.send(msg, observer)
        case _ => logger.warn("Mail Notifier should deliver mail message only")
    }

  protected def buildSubject(msg: Message): String

  protected def buildText(msg: Message): String
