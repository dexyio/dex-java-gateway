package com.finotek.dexgateway

import java.util
import java.util.concurrent.{ExecutorService, Executors}

import com.ericsson.otp.erlang._

class DexPlugin {
  type FunName = String

  val executor:ExecutorService = Executors.newFixedThreadPool(5)

  private var functions:util.Map[FunName, DexFunction] = new util.HashMap[FunName, DexFunction]()

  def execute(request: DexyRequest, mailbox:OtpMbox): Unit = {
    val dexRequest = new DexRequest(new DexRequestParams(request.params), request.requestId, request.pid, mailbox)

    if(functions.containsKey(request.fun)) {
      executor.execute { () =>
        functions.get(request.fun).execute(dexRequest)
      }
    } else {
      throw new RuntimeException("No Such function exists.")
    }
  }

  def addFunction(key:FunName, func:DexFunction):Unit =
    functions.put(key, func)
}

class DexRequest(val params:DexRequestParams, val requestId:String, pid:OtpErlangPid, mailbox:OtpMbox) {
  def send(list:OtpErlangList): Unit = {
    val resultParams = List[OtpErlangObject](
      new OtpErlangBitstr(requestId.getBytes("UTF-8")),
      list
    )
    val result = new OtpErlangTuple(resultParams.toArray)

    mailbox.send(pid, result)
  }
}