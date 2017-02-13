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

  val node:OtpNode = new OtpNode(nodeName + "@127.0.0.1")
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
          println("Received a message(tuple).")

          val requestTry = Try(bindToRequest(recv))
          requestTry.fold(
            errors => {
              errors.printStackTrace()
            },
            request => {
              if(plugins.containsKey(request.app)) {
                println("found plugin.")
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
    val requestId  = recv.elementAt(1).asInstanceOf[OtpErlangAtom].atomValue()
    val plugin     = recv.elementAt(2).asInstanceOf[OtpErlangAtom].atomValue()
    val fun        = recv.elementAt(3).asInstanceOf[OtpErlangAtom].atomValue()
    val params     = recv.elementAt(4).asInstanceOf[OtpErlangList]

    DexyRequest(pid, requestId, plugin, fun, params, null)
  }

  private def binToString(bin:OtpErlangBinary):String = new String(bin.binaryValue(), "UTF-8")
}