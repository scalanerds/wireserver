package com.scalanerds.wireserver.utils

import org.slf4j.LoggerFactory

/** slf4j Logger */
trait Logger {
  lazy val logger = LoggerFactory.getLogger(getClass)

  /** log error and call a function that should cleans resources and stop the process */
  def fail(fn: => Unit): Unit = {
    val stack = Thread.currentThread.getStackTrace()(3)
    logger.error(
      s"Error in [[${stack.getMethodName}]] at line ${stack.getLineNumber} in file [[${stack.getFileName}]]")
  }
}
