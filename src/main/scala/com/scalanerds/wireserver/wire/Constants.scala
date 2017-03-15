package com.scalanerds.wireserver.wire

import scala.language.implicitConversions

package object conversions {
  implicit def byte2bool(b: Byte): Boolean = b.toInt != 0
  implicit def bool2byte(b: Boolean): Byte = (if (b) 1 else 0).toByte
}

object OPCODES {
  val opReply         = 1
  val opMsg           = 1000
  val opUpdate        = 2001
  val opInsert        = 2002
  val opQuery         = 2004
  val opGetMore       = 2005
  val opDelete        = 2006
  val opKillCursor    = 2007
  val opCommand       = 2010
  val opCommandReply  = 2011

  val clientCodes = Vector(opUpdate, opInsert, opQuery, opGetMore, opDelete, opKillCursor, opCommand)
  val serverCodes = Vector(opReply, opMsg, opCommandReply)
}

object OPNAMES {
  val forward  : Vector[String] = Vector(
    "find", "insert", "update", "delete", "count")
  val intercept: Vector[String] = Vector(
    "buildinfo", "buildInfo", "getLog", "isMaster", "replSetGetStatus", "whatsmyuri", // Basic
    "currentOp", "fsync", "fsyncUnlock", "getlasterror", "getpreverror", "killOp", "listCommands",
    "reseterror", // Advanced
    "auth", "logout", // Authentication
    "create") // DB administration
  val forbidden: Vector[String] = Vector(
    "addUser", "createUser",  "dropUser", "removeUser", "updateUser", "usersInfo", // Users management
    "grantPrivilegesToRole", "grantRolesToRole", "grantRolesToUser", "revokePrivilegesFromRole", "revokeRolesFromRole",
    "revokeRolesFromUser", "rolesInfo", "updateRole", // Roles management
    "collStats", "dbStats", "forceError", "getCmdLineOpts", "getParameter", "hostInfo", "profile", "serverStatus",
    "setParameter", "shutdownServer", // Server administration
    "dbEval", "eval", "$eval") // Javascript stuff
}