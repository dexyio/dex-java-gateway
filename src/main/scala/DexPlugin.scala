import java.util

import com.ericsson.otp.erlang._

class DexPlugin {
  type FunName = String

  private var functions:util.Map[FunName, DexFunction] = new util.HashMap[FunName, DexFunction]()

  def execute(request: DexyRequest, mailbox:OtpMbox): Unit = {
    val dexRequest = new DexRequest(request.params, request.requestId, request.pid, mailbox)

    if(functions.containsKey(request.fun)) {
      functions.get(request.fun).execute(dexRequest)
    } else {
      throw new RuntimeException("No Such function exists.")
    }
  }

  def addFunction(key:FunName, func:DexFunction):Unit =
    functions.put(key, func)
}

class DexRequest(val params:OtpErlangList, val requestId:String, pid:OtpErlangPid, mailbox:OtpMbox) {
  def send(list:OtpErlangList): Unit = {
    val resultParams = List[OtpErlangObject](
      new OtpErlangBitstr(requestId.getBytes("UTF-8")),
      list
    )
    val result = new OtpErlangTuple(resultParams.toArray)

    mailbox.send(pid, result)
  }
}