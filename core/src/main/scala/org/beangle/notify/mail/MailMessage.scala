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

package org.beangle.notify.mail

import java.time.Instant
import org.beangle.commons.lang.Assert
import org.beangle.commons.lang.Strings
import org.beangle.commons.collection.Collections
import org.beangle.notify.AbstractMessage
import org.beangle.notify.Message
import javax.mail.internet.InternetAddress
import scala.collection.mutable

class MailMessage extends AbstractMessage:
  var from: InternetAddress = null
  var to: List[InternetAddress] = List.empty
  var cc: List[InternetAddress] = List.empty
  var bcc: List[InternetAddress] = List.empty
  var sentAt: Instant = _

  def encoding: String =
    Strings.substringAfter(contentType, "charset=")

  override def recipients: List[String] =
    var recipients = new mutable.ArrayBuffer[String]
    this.to.foreach(a => recipients += a.toString)
    this.cc.foreach(a => recipients += a.toString)
    this.bcc.foreach(a => recipients += a.toString)
    recipients.toList

  def this(subject: String, text: String, sendTo: String) =
    this()
    this.to = MimeUtils.parseAddress(sendTo, encoding)
    this.subject = subject
    this.text = text
    if (text.startsWith("<!DOCTYPE html>"))
      this.contentType = Message.HTML

  def this(subject: String, text: String, sendTo: String, sendCc: String, sendBcc: String) =
    this(subject, text, sendTo)
    this.cc = MimeUtils.parseAddress(sendCc, encoding)
    this.bcc = MimeUtils.parseAddress(sendBcc, encoding)

  def from(from: String): MailMessage =
    var froms = MimeUtils.parseAddress(from, encoding)
    if (froms.nonEmpty) this.from = froms.head
    this

  def addTo(sendTo: String): Unit =
    Assert.notNull(sendTo)
    this.to ++= MimeUtils.parseAddress(sendTo, encoding)

  def addCc(sendCc: String): Unit =
    Assert.notNull(sendCc)
    this.cc ++= MimeUtils.parseAddress(sendCc, encoding)

  def addBcc(sendBcc: String): Unit =
    Assert.notNull(sendBcc)
    this.bcc ++= MimeUtils.parseAddress(sendBcc, encoding)
