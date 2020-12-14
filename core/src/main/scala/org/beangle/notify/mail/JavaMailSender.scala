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
package org.beangle.notify.mail

import java.io.UnsupportedEncodingException
import java.util.Properties
import java.{util => ju}

import com.sun.mail.util.MailSSLSocketFactory
import javax.mail.internet.{MimeMessage, MimeUtility}
import javax.mail.{AuthenticationFailedException, MessagingException, NoSuchProviderException, Session, Transport}
import org.beangle.commons.lang.{Strings, Throwables}
import org.beangle.commons.logging.Logging
import org.beangle.notify.{NotificationException, NotificationSendException}

import scala.collection.mutable

object JavaMailSender {

  private val HEADER_MESSAGE_ID = "Message-ID"

  def smtp(host: String, username: String, password: String, port: Int = 25): JavaMailSender = {
    assert(username != null && username.contains("@"), "username should confirm xxx@hostname format")
    val sender = new JavaMailSender
    sender.protocol = "smtp"
    sender.host = host
    sender.port = port
    sender.username = username
    sender.password = password

    sender.properties.put("mail.smtp.auth", "true")
    sender.properties.put("mail.smtp.timeout", "465")
    sender.properties.put("mail.smtp.port", Integer.valueOf(port))
    sender.properties.put("mail.smtp.starttls.enable", "true")
    val sf = new MailSSLSocketFactory()
    sf.setTrustAllHosts(true)
    sender.properties.put("mail.smtp.socketFactory", sf)
    sender
  }
}

import org.beangle.notify.mail.JavaMailSender._

class JavaMailSender extends MailSender with Logging {

  var properties: Properties = new Properties()

  var protocol: String = "smtp"

  var host: String = _

  var port: Int = -1

  var username: String = _

  var password: String = _

  var defaultEncoding: String = _

  private var session: Session = _

  def send(messages: MailMessage*): Unit = {
    var mimeMsgs = new java.util.ArrayList[MimeMessage]()
    for (m <- messages) {
      try {
        mimeMsgs.add(createMimeMessage(m))
      } catch {
        case e: MessagingException => logger.error("Cannot mapping message" + m.subject, e)
      }
    }
    doSend(mimeMsgs.toArray(new Array[MimeMessage](mimeMsgs.size)))
  }

  protected def createMimeMessage(mailMsg: MailMessage): MimeMessage = {
    var mimeMsg = new MimeMessage(getSession())

    mimeMsg.setSentDate(if (null == mailMsg.sentAt) new ju.Date() else ju.Date.from(mailMsg.sentAt))
    if (null != mailMsg.from) mimeMsg.setFrom(mailMsg.from)
    addRecipient(mimeMsg, mailMsg)

    var encoding = Strings.substringAfter(mailMsg.contentType, "charset=")
    try {
      mimeMsg.setSubject(MimeUtility.encodeText(mailMsg.subject, encoding, "B"))
    } catch {
      case e: UnsupportedEncodingException => Throwables.propagate(e)
    }
    val text = mailMsg.text
    if (Strings.contains(mailMsg.contentType, "html")) {
      mimeMsg.setContent(text, if (Strings.isEmpty(encoding)) "text/html" else "text/html;charset=" + encoding)
    } else {
      mimeMsg.setText(text, if (Strings.isEmpty(encoding)) null else encoding)
    }
    mimeMsg
  }

  protected def getSession(): Session = {
    this.synchronized {
      if (this.session == null) this.session = Session.getInstance(this.properties)
      this.session
    }
  }

  protected def getTransport(session: Session): Transport = {
    var pro = this.protocol
    if (pro == null) pro = session.getProperty("mail.transport.protocol")
    try {
      session.getTransport(pro)
    } catch {
      case e: NoSuchProviderException => throw e
    }
  }

  protected def doSend(mimeMessages: Array[MimeMessage]): Unit = {
    var failedMessages = new mutable.LinkedHashMap[Object, Exception]
    var transport: Transport = null
    try {
      transport = getTransport(getSession())
      transport.connect(host, port, username, password)
    } catch {
      case ex: AuthenticationFailedException => throw new NotificationException(ex.getMessage(), ex)
      case ex: MessagingException =>
        // Effectively, all messages failed...
        mimeMessages.foreach(original => failedMessages.put(original, ex))
        throw new NotificationException("Mail server connection failed", ex)
    }
    try {
      for (mimeMessage <- mimeMessages) {
        try {
          if (mimeMessage.getSentDate() == null) mimeMessage.setSentDate(new ju.Date())
          var messageId = mimeMessage.getMessageID()
          mimeMessage.saveChanges()
          // Preserve explicitly specified message id...
          if (messageId != null) mimeMessage.setHeader(HEADER_MESSAGE_ID, messageId)
          transport.sendMessage(mimeMessage, mimeMessage.getAllRecipients())
        } catch {
          case ex: MessagingException => failedMessages.put(mimeMessage, ex)
        }
      }
    } finally {
      try {
        transport.close()
      } catch {
        case ex: MessagingException =>
          throw if (failedMessages.nonEmpty) new NotificationSendException("Failed to close server connection after message failures", ex,
            failedMessages.toMap)
          else throw new NotificationException("Failed to close server connection after message sending", ex)
      }
    }

    if (failedMessages.nonEmpty) {
      throw new NotificationSendException(failedMessages.toMap)
    }
  }

  private def addRecipient(mimeMsg: MimeMessage, mailMsg: MailMessage): Int = {
    var recipients = 0
    try {
      for (to <- mailMsg.to) {
        mimeMsg.addRecipient(javax.mail.Message.RecipientType.TO, to)
        recipients += 1
      }
      for (cc <- mailMsg.cc) {
        mimeMsg.addRecipient(javax.mail.Message.RecipientType.CC, cc)
        recipients += 1
      }
      for (bcc <- mailMsg.bcc) {
        mimeMsg.addRecipient(javax.mail.Message.RecipientType.BCC, bcc)
        recipients += 1
      }
    } catch {
      case e: MessagingException => Throwables.propagate(e)
    }
    return recipients
  }

}
