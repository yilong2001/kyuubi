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

import org.apache.kyuubi.config.KyuubiConf
import org.apache.kyuubi.service.AbstractBackendService
import org.apache.kyuubi.session.{K8sServiceSessionManager, KyuubiSessionManager, SessionManager}

class KyuubiBackendService(name: String) extends AbstractBackendService(name) {

  def this() = this(classOf[KyuubiBackendService].getSimpleName)

  // override val sessionManager: SessionManager = new KyuubiSessionManager()
  override val sessionManager: SessionManager = new K8sServiceSessionManager()

  override def initialize(conf: KyuubiConf): Unit = {
    super.initialize(conf)
  }
}
