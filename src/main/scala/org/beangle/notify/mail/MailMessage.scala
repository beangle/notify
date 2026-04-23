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

import jakarta.mail.internet.InternetAddress
import org.beangle.commons.lang.{Assert, Strings}
import org.beangle.notify.{AbstractMessage, Message}

import java.io.{ByteArrayInputStream, File, InputStream}
import java.nio.file.Files
import java.time.Instant
import scala.collection.mutable

/** 邮件附件：显示名、内容流（发送时会被读取并关闭）、可选 MIME 类型。 */
final case class MailAttachment(name: String, content: InputStream, contentType: Option[String] = None)

/** 邮件消息模型：收发件人、主题正文、可选附件；与 `JavaMailSender` 配合使用。 */
class MailMessage extends AbstractMessage {
  var from: InternetAddress = _
  var to: List[InternetAddress] = List.empty
  var cc: List[InternetAddress] = List.empty
  var bcc: List[InternetAddress] = List.empty
  var sentAt: Instant = _
  var attachments: List[MailAttachment] = List.empty

  /** 从 `contentType` 中解析出的 charset 片段（用于地址编码等）。 */
  def encoding: String =
    Strings.substringAfter(contentType, "charset=")

  def hasAttachments: Boolean = attachments.nonEmpty

  /** 所有 to/cc/bcc 的字符串形式列表。 */
  override def recipients: List[String] = {
    val recipients = new mutable.ArrayBuffer[String]
    this.to.foreach(a => recipients += a.toString)
    this.cc.foreach(a => recipients += a.toString)
    this.bcc.foreach(a => recipients += a.toString)
    recipients.toList
  }

  /** 构造一封仅含主送、主题、正文的邮件；若以 `<!DOCTYPE html>` 开头则设为 HTML。 */
  def this(subject: String, text: String, sendTo: String) = {
    this()
    this.to = MimeUtils.parseAddress(sendTo, encoding)
    this.subject = subject
    this.text = text
    if (text.startsWith("<!DOCTYPE html>")) this.contentType = Message.HTML
  }

  /** 同上，并解析抄送、密送地址。 */
  def this(subject: String, text: String, sendTo: String, sendCc: String, sendBcc: String) = {
    this(subject, text, sendTo)
    this.cc = MimeUtils.parseAddress(sendCc, encoding)
    this.bcc = MimeUtils.parseAddress(sendBcc, encoding)
  }

  /** 设置发件人（取解析后第一个地址）；链式返回 `this`。 */
  def from(from: String): MailMessage = {
    val froms = MimeUtils.parseAddress(from, encoding)
    if (froms.nonEmpty) this.from = froms.head
    this
  }

  /** 追加主送地址（可含多个，逗号分隔）。 */
  def addTo(sendTo: String): Unit = {
    Assert.notNull(sendTo)
    this.to ++= MimeUtils.parseAddress(sendTo, encoding)
  }

  /** 追加抄送。 */
  def addCc(sendCc: String): Unit = {
    Assert.notNull(sendCc)
    this.cc ++= MimeUtils.parseAddress(sendCc, encoding)
  }

  /** 追加密送。 */
  def addBcc(sendBcc: String): Unit = {
    Assert.notNull(sendBcc)
    this.bcc ++= MimeUtils.parseAddress(sendBcc, encoding)
  }

  /** 追加附件（字节）；可选 `contentType`，缺省则由发送端按 `application/octet-stream` 处理。 */
  def addAttachment(name: String, bytes: Array[Byte]): MailMessage = {
    addAttachment(name, bytes, null)
  }

  /** 字节附件重载，可显式指定 MIME 类型。 */
  def addAttachment(name: String, bytes: Array[Byte], contentType: String): MailMessage = {
    Assert.notNull(name)
    Assert.notNull(bytes)
    this.attachments = this.attachments :+ MailAttachment(name, new ByteArrayInputStream(bytes), Option(contentType))
    this
  }

  /** 追加附件（本地文件），文件名为附件显示名。 */
  def addAttachment(file: File): MailMessage = {
    addAttachment(file, null)
  }

  /** 文件附件重载，可显式指定 MIME 类型。 */
  def addAttachment(file: File, contentType: String): MailMessage = {
    Assert.notNull(file)
    Assert.isTrue(file.exists(), s"file ${file.getAbsolutePath} does not exists")
    Assert.isTrue(file.isFile, s"${file.getAbsolutePath} is not file")
    this.attachments = this.attachments :+ MailAttachment(file.getName, Files.newInputStream(file.toPath), Option(contentType))
    this
  }

  /** 追加附件（流）；发送后流会被关闭，同一流不可重复发送。 */
  def addAttachment(name: String, stream: InputStream): MailMessage = {
    addAttachment(name, stream, null)
  }

  /** 流附件重载，可显式指定 MIME 类型。 */
  def addAttachment(name: String, stream: InputStream, contentType: String): MailMessage = {
    Assert.notNull(name)
    Assert.notNull(stream)
    this.attachments = this.attachments :+ MailAttachment(name, stream, Option(contentType))
    this
  }
}
