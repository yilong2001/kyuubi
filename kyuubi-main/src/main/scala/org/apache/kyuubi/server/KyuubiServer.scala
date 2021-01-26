/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.kyuubi.server

import scala.util.Properties

import org.apache.hadoop.security.UserGroupInformation

import org.apache.kyuubi._
import org.apache.kyuubi.config.KyuubiConf
// import org.apache.kyuubi.ha.HighAvailabilityConf._

// import org.apache.kyuubi.ha.server.EmbeddedZkServer
import org.apache.kyuubi.service.{AbstractBackendService, KinitAuxiliaryService, Serverable}
import org.apache.kyuubi.util.{KyuubiHadoopUtils, SignalRegister}

object KyuubiServer extends Logging {
  // private val zkServer = new EmbeddedZkServer()

  def startServer(conf: KyuubiConf): KyuubiServer = {
    // TODO: changed by llz 20210113, to skip zk from k8s
    /* val zkEnsemble = conf.get(HA_ZK_QUORUM)
    if (zkEnsemble == null || zkEnsemble.isEmpty) {
      zkServer.initialize(conf)
      zkServer.start()
      sys.addShutdownHook(zkServer.stop())
      conf.set(HA_ZK_QUORUM, zkServer.getConnectString)
      conf.set(HA_ZK_ACL_ENABLED, false)
    } */

    val server = new KyuubiServer()
    server.initialize(conf)
    server.start()
    sys.addShutdownHook(server.stop())
    server
  }

  def main(args: Array[String]): Unit = {
    info(
       """
         |                  Welcome to
         |  __  __                           __
         | /\ \/\ \                         /\ \      __
         | \ \ \/'/'  __  __  __  __  __  __\ \ \____/\_\
         |  \ \ , <  /\ \/\ \/\ \/\ \/\ \/\ \\ \ '__`\/\ \
         |   \ \ \\`\\ \ \_\ \ \ \_\ \ \ \_\ \\ \ \L\ \ \ \
         |    \ \_\ \_\/`____ \ \____/\ \____/ \ \_,__/\ \_\
         |     \/_/\/_/`/___/> \/___/  \/___/   \/___/  \/_/
         |                /\___/
         |                \/__/
       """.stripMargin)
    info(s"Version: $KYUUBI_VERSION, Revision: $REVISION, Branch: $BRANCH," +
      s" Java: $JAVA_COMPILE_VERSION, Scala: $SCALA_COMPILE_VERSION," +
      s" Spark: $SPARK_COMPILE_VERSION, Hadoop: $HADOOP_COMPILE_VERSION," +
      s" Hive: $HIVE_COMPILE_VERSION")
    info(s"Using Scala ${Properties.versionString}, ${Properties.javaVmName}," +
      s" ${Properties.javaVersion}")
    SignalRegister.registerLogger(logger)
    val conf = new KyuubiConf().loadFileDefaults()

    // TODO: added by llz 20210114, first use default conf, and then use conf from main args
    val arglist = args.toList
    def nextOption(map : Map[String, String], list: List[String]) : Map[String, String] = {
      list match {
        case Nil => map
        case "--conf" :: value :: tail if (value.split("=").length > 1) =>
          nextOption(map ++ Map(value.split("=")(0) -> value.split("=")(1)), tail)
        case "--conf" :: value :: tail if (value.split("=").length <= 1) =>
          info(value + " is empty!!!")
          nextOption(map ++ Map(value -> ""), tail)
        case option :: tail => info("Unknown option " + option)
          map
      }
    }
    val mainConfs = nextOption(Map(), arglist)
    mainConfs.foreach(kv => conf.set(kv._1, kv._2))
    // end

    UserGroupInformation.setConfiguration(KyuubiHadoopUtils.newHadoopConf(conf))
    startServer(conf)
  }
}

class KyuubiServer(name: String) extends Serverable(name) {

  def this() = this(classOf[KyuubiServer].getSimpleName)

  override private[kyuubi] val backendService: AbstractBackendService = new KyuubiBackendService()

  override def initialize(conf: KyuubiConf): Unit = {
    val kinit = new KinitAuxiliaryService()
    addService(kinit)
    super.initialize(conf)
  }

  override protected def stopServer(): Unit = {
    // TODO: changed by llz 20210113, to skip zk from k8s
    // KyuubiServer.zkServer.stop()
  }
}
