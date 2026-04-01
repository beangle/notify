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

package org.beangle.notify.sms.vendor

import org.beangle.commons.lang.Strings
import org.beangle.commons.net.http.{HttpUtils, Request}
import org.beangle.commons.text.escape.XmlEscaper
import org.beangle.commons.xml.Document
import org.beangle.notify.NotifyLogger
import org.beangle.notify.sms.{AbstractSmsSender, Receiver, SmsResponse}

/** Lixin平台 SOAP 1.1 WebService（Axis2，WSDL 见学校文档）。
 *
 * 与文档中 Java/Axis 示例等价：对 `msg.msgHttpSoap11Endpoint` 发送 document/literal 请求，
 * `SOAPAction` 以 WSDL 为准（`urn:sendSmsWithPhoneNum`），而非文档里错误的 namespace 拼接。
 */
class LixinSmsSender(endpoint: String, appId: String, appSecret: String)
  extends AbstractSmsSender(endpoint, appId, appSecret) {

  override def send(receiver: Receiver, contents: String): SmsResponse = {
    val inner = buildPayloadXml(receiver, contents)
    val (opName, soapAction) = ("sendSmsWithPhoneNum", "urn:sendSmsWithPhoneNum")
    val envelope = buildSoapEnvelope(inner, opName)
    val req = Request
      .build(envelope, "text/xml; charset=UTF-8")
      .header("SOAPAction", "\"" + soapAction + "\"")
    val res = HttpUtils.post(soap11EndpointUrl, req)
    if !res.isOk then {
      NotifyLogger.error("sms http error: " + res.status + " " + res.getText)
      SmsResponse.fail(res.getText)
    } else {
      parseBusinessXml(res.getText)
    }
  }

  /** WSDL 中 `msgHttpSoap11Endpoint` 的完整 HTTP 地址。 */
  private def soap11EndpointUrl: String =
    s"${endpoint.stripSuffix("/")}/msg.msgHttpSoap11Endpoint/"

  private def buildPayloadXml(receiver: Receiver, contents: String): String = {

    val b = new StringBuilder
    b.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
    b.append("<message>")
    b.append("<account>").append(XmlEscaper.escapeText(appId)).append("</account>")
    b.append("<password>").append(XmlEscaper.escapeText(appSecret)).append("</password>")
    b.append("<phoneNum>").append(XmlEscaper.escapeText(receiver.mobile)).append("</phoneNum>")
    b.append("<content>").append(XmlEscaper.escapeText(contents)).append("</content>")
    b.append("</message>")
    b.toString
  }

  private def buildSoapEnvelope(innerMessageXml: String, operationLocalName: String): String = {
    s"""<?xml version="1.0" encoding="UTF-8"?>
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:ns="http://sms.duanxin.lixin.edu.cn">
  <soapenv:Body>
    <ns:$operationLocalName>
      <ns:message><![CDATA[$innerMessageXml]]></ns:message>
    </ns:$operationLocalName>
  </soapenv:Body>
</soapenv:Envelope>"""
  }

  private[sms] def parseBusinessXml(soapXml: String): SmsResponse = {
    try
      val doc = Document.parse(soapXml)
      var faultMsg = (doc \\ "soapenv:faultstring").text
      if (Strings.isBlank(faultMsg)) {
        faultMsg = (doc \\ "faultstring").text
      }
      if (Strings.isNotBlank(faultMsg)) {
        return SmsResponse.fail(faultMsg)
      }

      var inner = (doc \\ "ns:return").text.trim
      if (Strings.isBlank(inner)) {
        inner = (doc \\ "return").text.trim
      }

      if Strings.isBlank(inner) then
        SmsResponse.fail("empty return: " + soapXml)
      else
        val biz = Document.parse(XmlEscaper.unescape(inner))
        val result = biz \\ "result"
        val desc = biz \\ "desc"
        val code = if result.nonEmpty then result.text.trim else ""
        val d = if desc.nonEmpty then desc.text.trim else ""
        if code == "0" then SmsResponse.ok(if d.nonEmpty then d else "OK")
        else {
          val translated = LixinSmsSender.translateResult(code)
          val msg = if d.nonEmpty then s"$translated($d)" else translated
          SmsResponse.fail(s"result=$code $msg")
        }
    catch
      case e: Exception =>
        NotifyLogger.error("sms SOAP/response parse error", e)
        SmsResponse.fail(e.getMessage)
  }

}

object LixinSmsSender {

  private val resultMessage = Map(
    "0" -> "提交成功",
    "1" -> "账号或密码错误",
    "4" -> "存在无效的工号或学号",
    "5" -> "手机号码个数超过最大限制",
    "6" -> "短信内容超过最大限制",
    "9" -> "请求来源地址无效",
    "98" -> "系统繁忙",
    "99" -> "消息格式错误"
  )

  def translateResult(code: String): String = {
    resultMessage.getOrElse(code, "未知错误")
  }
}
