package com.finotek.dexgateway

trait DexFunction {
  def execute(dexRequest:DexRequest):Unit
}