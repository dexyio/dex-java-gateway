package com.finotek.dexgateway

import java.util

import com.ericsson.otp.erlang._

import scala.util.Try

case class DexyRequest(pid:OtpErlangPid, requestId:String,
                      app:String, fun:String, params:OtpErlangList, options:OtpErlangMap = null)

class DexGateway(nodeName:String, messageBox:String, cookie:String) {
  type AppName = String
  type Recv    = OtpErlangTuple

  private var isTerminated = false
  private var plugins:java.util.Map[AppName, DexPlugin] = new util.HashMap[AppName, DexPlugin]

  val node:OtpNode = new OtpNode(nodeName)
  node.setCookie(cookie)

  val mailbox:OtpMbox = node.createMbox(messageBox)

  /**
  * If you call this method on a Main Thread, whole application will wait.
  * */
  def start():Unit = {
    println("dex-gateway is running.")

    while (!isTerminated) {
      mailbox.receive() match {
        case recv:OtpErlangTuple =>
          val requestTry = Try(bindToRequest(recv))
          requestTry.fold(
            errors => {
              errors.printStackTrace()
            },
            request => {
              if(plugins.containsKey(request.app)) {
                val plugin = plugins.get(request.app)
                plugin.execute(request, mailbox)
              } else {
                throw new RuntimeException("No Such plugin error.")
              }
            }
          )
        case _ =>
          println("Received message is not tuple.")
      }
    }

    println("dex-gateway stopped.")
  }

  def terminate():Unit =
    isTerminated = true

  def addPlugin(appName:AppName, dexPlugin:DexPlugin): Unit = plugins.put(appName, dexPlugin)

  def removePlugin(appName:AppName): Unit = plugins.remove(appName)

  private def bindToRequest(recv: Recv): DexyRequest = {
    println(recv.toString)

    val pid        = recv.elementAt(0).asInstanceOf[OtpErlangPid]
    val requestId  = binToString(recv.elementAt(1).asInstanceOf[OtpErlangBinary])
    val plugin     = binToString(recv.elementAt(2).asInstanceOf[OtpErlangBinary])
    val fun        = binToString(recv.elementAt(3).asInstanceOf[OtpErlangBinary])
    val params     = recv.elementAt(4).asInstanceOf[OtpErlangList]

    DexyRequest(pid, requestId, plugin, fun, params, null)
  }

  private def binToString(bin:OtpErlangBinary):String = new String(bin.binaryValue(), "UTF-8")
}