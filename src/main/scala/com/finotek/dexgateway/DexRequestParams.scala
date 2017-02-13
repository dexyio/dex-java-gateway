package com.finotek.dexgateway

import com.ericsson.otp.erlang.{OtpErlangBinary, OtpErlangList}

import scala.util.{Failure, Success, Try}

class DexRequestParams(params: OtpErlangList) {
  def getString(i:Int):java.lang.String = {
    val bin = params.elementAt(i).asInstanceOf[OtpErlangBinary]
    new String(bin.binaryValue())
  }

  def getString(i:Int, charset:String): java.lang.String = {
    val bin = params.elementAt(i).asInstanceOf[OtpErlangBinary]
    Try(new String(bin.binaryValue(), charset)) match {
      case Success(v) =>
        v
      case Failure(e) =>
        e.printStackTrace()
        null
    }
  }
}
