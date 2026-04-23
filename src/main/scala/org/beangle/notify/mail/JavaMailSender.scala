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

import jakarta.mail.internet.{MimeBodyPart, MimeMessage, MimeMultipart, MimeUtility}
import jakarta.mail.util.ByteArrayDataSource
import jakarta.mail.{MessagingException, Part, Session, Transport}
import org.beangle.commons.bean.Initializing
import org.beangle.commons.lang.{Strings, Throwables}
import org.beangle.notify.{NotifyException, SendingObserver}
import org.eclipse.angus.mail.util.MailSSLSocketFactory

import java.io.{ByteArrayOutputStream, UnsupportedEncodingException}
import java.util as ju
import java.util.Properties
import scala.collection.mutable.ArrayBuffer

/** Jakarta Mail 实现的 SMTP 发送器。需先配置 `properties`/`host` 等并调用 `init()` 再发信；也可用工厂方法 `smtp` 一次性完成。 */
object JavaMailSender:

  private val HEADER_MESSAGE_ID = "Message-ID"

  /** 按常见 SMTP+STARTTLS+信任全部主机 预设构造发送器，并在返回前调用 `init()`。 */
  def smtp(host: String, username: String, password: String, port: Int = 25): JavaMailSender =
    assert(username != null && username.contains("@"), "username should confirm xxx@hostname format")
    val sender = new JavaMailSender
    sender.protocol = "smtp"
    sender.host = host
    sender.port = port
    sender.username = username
    sender.password = password

    sender.properties.put("mail.smtp.auth", "true")
    sender.properties.put("mail.smtp.port", Integer.valueOf(port))
    sender.properties.put("mail.smtp.starttls.enable", "true")
    sender.properties.put("mail.smtp.timeout", 10000);
    sender.properties.put("mail.smtp.connectiontimeout", 10000);
    sender.properties.put("mail.smtp.writetimeout", 10000);
    val sf = new MailSSLSocketFactory()
    sf.setTrustAllHosts(true)
    sender.properties.put("mail.smtp.socketFactory", sf)
    sender.init()
    sender

import org.beangle.notify.mail.JavaMailSender.*

/** 基于 `jakarta.mail.Session` 的邮件发送实现；同一实例可复用字段 `session` 多次发送。
  *
  * 示例：
  * {{{
  * import org.beangle.notify.SendingObserver
  *
  * val sender = JavaMailSender.smtp("smtp.example.com", "user@example.com", "secret", 587)
  * val msg = new MailMessage("主题", "正文", "recv@example.com").from("user@example.com")
  * sender.send(msg, SendingObserver.Log)
  * }}}
  */
class JavaMailSender extends MailSender, Initializing {

  /** 传入 `Session.getInstance` 所用的 Jakarta Mail 配置。修改后须再次 `init()` 才会生效。 */
  var properties: Properties = new Properties()

  var protocol: String = "smtp"

  var host: String = _

  var port: Int = -1

  var username: String = _

  var password: String = _

  var defaultEncoding: String = _

  var sendInterval: Long = 0

  private var session: Session = _

  /** 根据当前 `properties` 创建邮件 `Session`，发信前必须调用一次（`smtp` 工厂已代为调用）。 */
  override def init(): Unit = {
    this.session = Session.getInstance(this.properties)
  }

  /** 发送单封邮件。 */
  override def send(msg: MailMessage, observer: SendingObserver): Unit = {
    send(List(msg), observer)
  }

  /** 将多封 `MailMessage` 转为 `MimeMessage` 后，共用一条 `Transport` 连接顺序发送。 */
  override def send(messages: Iterable[MailMessage], observer: SendingObserver): Unit = {
    val mimeMsgs = new ArrayBuffer[(MailMessage, MimeMessage)]
    for (m <- messages)
      try
        mimeMsgs.addOne((m, createMimeMessage(m)))
      catch
        case e: MessagingException => observer.onFail(new NotifyException("Cannot mapping message" + m.subject, e))
    doSend(mimeMsgs, observer)
  }

  /** 将业务层 `MailMessage` 映射为 Jakarta `MimeMessage`（含正文与附件 multipart）。子类可覆盖以调整 MIME 结构。 */
  protected def createMimeMessage(mailMsg: MailMessage): MimeMessage = {
    val mimeMsg = new MimeMessage(session)

    mimeMsg.setSentDate(if (null == mailMsg.sentAt) new ju.Date() else ju.Date.from(mailMsg.sentAt))
    if (null != mailMsg.from) mimeMsg.setFrom(mailMsg.from)
    addRecipient(mimeMsg, mailMsg)

    val encoding = Strings.substringAfter(mailMsg.contentType, "charset=")
    try
      mimeMsg.setSubject(MimeUtility.encodeText(mailMsg.subject, encoding, "B"))
    catch
      case e: UnsupportedEncodingException => Throwables.propagate(e)
    if (mailMsg.hasAttachments) {
      val multipart = new MimeMultipart("mixed")
      multipart.addBodyPart(buildTextBody(mailMsg.text, mailMsg.contentType, encoding))
      mailMsg.attachments.foreach(a => multipart.addBodyPart(buildAttachmentBody(a, encoding)))
      mimeMsg.setContent(multipart)
    } else {
      val text = mailMsg.text
      if (Strings.contains(mailMsg.contentType, "html"))
        mimeMsg.setContent(text, if (Strings.isEmpty(encoding)) "text/html" else "text/html;charset=" + encoding)
      else
        mimeMsg.setText(text, if (Strings.isEmpty(encoding)) null else encoding)
    }
    mimeMsg
  }

  /** multipart 中的正文部分（纯文本或 HTML）。 */
  private def buildTextBody(text: String, contentType: String, encoding: String): MimeBodyPart = {
    val part = new MimeBodyPart()
    if (Strings.contains(contentType, "html")) {
      part.setContent(text, if (Strings.isEmpty(encoding)) "text/html" else s"text/html;charset=$encoding")
    } else {
      if (Strings.isEmpty(encoding)) part.setText(text)
      else part.setText(text, encoding)
    }
    part
  }

  /** 将附件流读入内存后封装为 MIME 部件；会关闭 `MailAttachment.content`。 */
  private def buildAttachmentBody(attachment: MailAttachment, encoding: String): MimeBodyPart = {
    val part = new MimeBodyPart()
    val charset = if (Strings.isEmpty(encoding)) "UTF-8" else encoding
    val is = attachment.content
    val dataSource =
      try {
        val bytes = readAllBytes(is)
        new ByteArrayDataSource(bytes, attachment.contentType.getOrElse("application/octet-stream"))
      } finally {
        is.close()
      }

    part.setDataHandler(new jakarta.activation.DataHandler(dataSource))
    part.setDisposition(Part.ATTACHMENT)
    part.setFileName(MimeUtility.encodeText(attachment.name, charset, "B"))
    part
  }

  /** 缓冲读取输入流直至 EOF。 */
  private def readAllBytes(is: java.io.InputStream): Array[Byte] = {
    val baos = new ByteArrayOutputStream()
    val buffer = new Array[Byte](8192)
    var len = is.read(buffer)
    while (len != -1) {
      baos.write(buffer, 0, len)
      len = is.read(buffer)
    }
    baos.toByteArray
  }

  /** 按 `protocol`（或 session 默认协议）从当前 `session` 获取传输层。 */
  protected def getTransport(): Transport = {
    var pro = this.protocol
    if (pro == null) pro = session.getProperty("mail.transport.protocol")
    session.getTransport(pro)
  }

  /** 连接 SMTP、逐封 `sendMessage`，最后在循环外关闭 `Transport`（适合群发）。 */
  protected def doSend(msgs: Iterable[(MailMessage, MimeMessage)], observer: SendingObserver): Unit = {
    var transport: Transport = null
    try {
      transport = getTransport()
      transport.connect(host, port, username, password)
    } catch {
      case ex: Exception => observer.onFail(ex)
    }
    if (null != transport) {
      for (msg <- msgs) {
        val mimeMessage = msg._2
        try {
          if (mimeMessage.getSentDate == null) mimeMessage.setSentDate(new ju.Date())
          val messageId = mimeMessage.getMessageID
          mimeMessage.saveChanges()
          // Preserve explicitly specified message id...
          if (messageId != null) mimeMessage.setHeader(HEADER_MESSAGE_ID, messageId)
          observer.onStart(msg._1)
          transport.sendMessage(mimeMessage, mimeMessage.getAllRecipients)
          observer.onFinish(msg._1)
          if (sendInterval > 0) Thread.sleep(sendInterval)
        } catch {
          case ex: MessagingException => observer.onFail(ex)
        }
      }
      try
        transport.close()
      catch
        case _: Throwable =>
    }
  }

  /** 将 `MailMessage` 的 to/cc/bcc 写入 `MimeMessage`，返回收件人总数。 */
  private def addRecipient(mimeMsg: MimeMessage, mailMsg: MailMessage): Int = {
    var recipients = 0
    try
      for (to <- mailMsg.to)
        mimeMsg.addRecipient(jakarta.mail.Message.RecipientType.TO, to)
        recipients += 1
      for (cc <- mailMsg.cc)
        mimeMsg.addRecipient(jakarta.mail.Message.RecipientType.CC, cc)
        recipients += 1
      for (bcc <- mailMsg.bcc)
        mimeMsg.addRecipient(jakarta.mail.Message.RecipientType.BCC, bcc)
        recipients += 1
    catch
      case e: MessagingException => Throwables.propagate(e)
    recipients
  }
}
