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

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class LixinSmsSenderTest extends AnyFunSpec with Matchers:

  private val sender = new LixinSmsSender("http://duanxin.lixin.edu.cn/dxjk/services", "demo", "secret")

  describe("LixinSmsSender.parseBusinessXml") {
    it("should parse success response") {
      val soap =
        """<?xml version="1.0" encoding="UTF-8"?>
          |<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
          |  <soapenv:Body>
          |    <sendSmsWithPhoneNumResponse>
          |      <return><![CDATA[<?xml version="1.0" encoding="UTF-8"?><response><result>0</result><desc>提交成功</desc></response>]]></return>
          |    </sendSmsWithPhoneNumResponse>
          |  </soapenv:Body>
          |</soapenv:Envelope>
          |""".stripMargin

      val resp = sender.parseBusinessXml(soap)
      resp.code should be("OK")
      resp.message should be("提交成功")
    }

    it("should translate known failure code") {
      val soap =
        """<?xml version="1.0" encoding="UTF-8"?>
          |<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
          |  <soapenv:Body>
          |    <sendSmsWithPhoneNumResponse>
          |      <return><![CDATA[<?xml version="1.0" encoding="UTF-8"?><response><result>98</result><desc>系统繁忙</desc></response>]]></return>
          |    </sendSmsWithPhoneNumResponse>
          |  </soapenv:Body>
          |</soapenv:Envelope>
          |""".stripMargin

      val resp = sender.parseBusinessXml(soap)
      resp.code should be("Failure")
      resp.message should include("result=98")
      resp.message should include("系统繁忙")
    }

    it("should parse soap fault") {
      val soapFault =
        """<?xml version="1.0" encoding="UTF-8"?>
          |<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
          |  <soapenv:Body>
          |    <soapenv:Fault>
          |      <faultcode>soapenv:Server</faultcode>
          |      <faultstring>internal error</faultstring>
          |    </soapenv:Fault>
          |  </soapenv:Body>
          |</soapenv:Envelope>
          |""".stripMargin

      val resp = sender.parseBusinessXml(soapFault)
      resp.code should be("Failure")
      resp.message should be("internal error")
    }
  }
