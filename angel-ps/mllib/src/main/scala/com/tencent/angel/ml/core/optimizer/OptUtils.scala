/*
 * Tencent is pleased to support the open source community by making Angel available.
 *
 * Copyright (C) 2017-2018 THL A29 Limited, a Tencent company. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in 
 * compliance with the License. You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/Apache-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 */


package com.tencent.angel.ml.core.optimizer

import com.tencent.angel.RunningMode
import com.tencent.angel.conf.AngelConf
import com.tencent.angel.ml.core.conf.{MLConf, SharedConf}
import com.tencent.angel.ml.core.network.graph.Graph

object OptUtils {
  def getSlotNum(optimizer: Optimizer): Int = optimizer.getNumSlot

  def getSlotNum(optimizer: String): Int = {
    optimizer.toLowerCase match {
      case "sgd" => 1
      case "momentum" => 2
      case "adam" => 3
      case "ftrl" => 3
      case "adagrad" => 2
      case "adadelta" => 3
      case _ => 1
    }
  }

  def getOptimizer(name: String): Optimizer = {
    val conf: SharedConf = SharedConf.get()
    val lr0: Double = conf.getDouble(MLConf.ML_LEARN_RATE, 1.0)

    name.toLowerCase().trim() match {
      case "momentum" =>
        val momentum: Double = conf.getDouble(MLConf.ML_OPT_MOMENTUM_MOMENTUM, 0.9)
        new Momentum(lr0, momentum)
      case "adam" =>
        val gamma: Double = conf.getDouble(MLConf.ML_OPT_ADAM_GAMMA, 0.99)
        val beta: Double = conf.getDouble(MLConf.ML_OPT_ADAM_BETA, 0.9)
        new Adam(lr0, gamma, beta)
      case "ftrl" =>
        val alpha: Double = conf.getDouble(MLConf.ML_OPT_FTRL_ALPHA, 0.1)
        val beta: Double = conf.getDouble(MLConf.ML_OPT_FTRL_BETA, 1.0)
        new FTRL(lr0, alpha, beta)
      case "adagrad" =>
        val beta: Double = conf.getDouble(MLConf.ML_OPT_ADAGRAD_BETA, 0.9)
        new AdaGrad(lr0, beta)
      case "adadelta" =>
        val beta: Double = conf.getDouble(MLConf.ML_OPT_ADADELTA_BETA, 0.9)
        new AdaDelta(lr0, beta)
      case _ =>
        new SGD(lr0)
    }
  }

  def getNormal(mode: RunningMode, graph: Graph): Double = {
    mode match {
      case RunningMode.ANGEL_PS => 1.0
      case RunningMode.ANGEL_PS_WORKER => graph.placeHolder.getBatchSize * graph.taskNum
      case RunningMode.ANGEL_LOCAL => graph.placeHolder.getBatchSize
    }
  }
}
