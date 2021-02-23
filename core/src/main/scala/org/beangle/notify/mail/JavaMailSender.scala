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

import java.io.UnsupportedEncodingException
import java.util.Properties
import java.{ util => ju }

import com.sun.mail.util.MailSSLSocketFactory
import javax.mail.internet.{ MimeMessage, MimeUtility }
import javax.mail.{ MessagingException, NoSuchProviderException, Session, Transport }
import org.beangle.commons.lang.{ Strings, Throwables }
import org.beangle.commons.logging.Logging
import org.beangle.notify.{ NotifyException, SendingObserver }

import scala.collection.mutable.ArrayBuffer

object JavaMailSender:

  private val HEADER_MESSAGE_ID = "Message-ID"

  def smtp(host: String, username: String, password: String, port: Int = 25): JavaMailSender =
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
    sender.properties.put("mail.smtp.timeout", 10000);
    sender.properties.put("mail.smtp.connectiontimeout", 10000);
    sender.properties.put("mail.smtp.writetimeout", 10000);
    val sf = new MailSSLSocketFactory()
    sf.setTrustAllHosts(true)
    sender.properties.put("mail.smtp.socketFactory", sf)
    sender

import org.beangle.notify.mail.JavaMailSender._

class JavaMailSender extends MailSender with Logging:

  var properties: Properties = new Properties()

  var protocol: String = "smtp"

  var host: String = _

  var port: Int = -1

  var username: String = _

  var password: String = _

  var defaultEncoding: String = _

  var sendInterval: Long = 0

  private var session: Session = _

  override def send(msg: MailMessage, observer: SendingObserver): Unit =
    send(List(msg), observer)

  override def send(messages: Iterable[MailMessage], observer: SendingObserver): Unit =
    val mimeMsgs = new ArrayBuffer[(MailMessage, MimeMessage)]
    for (m <- messages)
      try
        mimeMsgs.addOne((m, createMimeMessage(m)))
      catch
        case e: MessagingException => observer.onFail(new NotifyException("Cannot mapping message" + m.subject, e))
    doSend(mimeMsgs, observer)

  protected def createMimeMessage(mailMsg: MailMessage): MimeMessage =
    val mimeMsg = new MimeMessage(getSession())

    mimeMsg.setSentDate(if (null == mailMsg.sentAt) new ju.Date() else ju.Date.from(mailMsg.sentAt))
    if (null != mailMsg.from) mimeMsg.setFrom(mailMsg.from)
    addRecipient(mimeMsg, mailMsg)

    val encoding = Strings.substringAfter(mailMsg.contentType, "charset=")
    try
      mimeMsg.setSubject(MimeUtility.encodeText(mailMsg.subject, encoding, "B"))
    catch
      case e: UnsupportedEncodingException => Throwables.propagate(e)
    val text = mailMsg.text
    if (Strings.contains(mailMsg.contentType, "html"))
      mimeMsg.setContent(text, if (Strings.isEmpty(encoding)) "text/html" else "text/html;charset=" + encoding)
    else
      mimeMsg.setText(text, if (Strings.isEmpty(encoding)) null else encoding)
    mimeMsg

  protected def getSession(): Session =
    this.synchronized {
      if (this.session == null) this.session = Session.getInstance(this.properties)
      this.session
    }

  protected def getTransport(session: Session): Transport =
    var pro = this.protocol
    if (pro == null) pro = session.getProperty("mail.transport.protocol")
    try
      session.getTransport(pro)
    catch
      case e: NoSuchProviderException => throw e

  protected def doSend(msgs: Iterable[(MailMessage, MimeMessage)], observer: SendingObserver): Unit =
    var transport: Transport = null
    try
      transport = getTransport(getSession())
      transport.connect(host, port, username, password)
    catch
      case ex: Exception => observer.onFail(ex)
    if (null != transport)
      try
        for (msg <- msgs)
          val mimeMessage = msg._2
          try
            if (mimeMessage.getSentDate() == null) mimeMessage.setSentDate(new ju.Date())
            val messageId = mimeMessage.getMessageID()
            mimeMessage.saveChanges()
            // Preserve explicitly specified message id...
            if (messageId != null) mimeMessage.setHeader(HEADER_MESSAGE_ID, messageId)
            observer.onStart(msg._1)
            transport.sendMessage(mimeMessage, mimeMessage.getAllRecipients())
            observer.onFinish(msg._1)
            if (sendInterval > 0) Thread.sleep(sendInterval)
          catch
            case ex: MessagingException => observer.onFail(ex)
      finally
        try
          transport.close()
        catch
          case _: Throwable =>

  private def addRecipient(mimeMsg: MimeMessage, mailMsg: MailMessage): Int =
    var recipients = 0
    try
      for (to <- mailMsg.to)
        mimeMsg.addRecipient(javax.mail.Message.RecipientType.TO, to)
        recipients += 1
      for (cc <- mailMsg.cc)
        mimeMsg.addRecipient(javax.mail.Message.RecipientType.CC, cc)
        recipients += 1
      for (bcc <- mailMsg.bcc)
        mimeMsg.addRecipient(javax.mail.Message.RecipientType.BCC, bcc)
        recipients += 1
    catch
      case e: MessagingException => Throwables.propagate(e)
    recipients
