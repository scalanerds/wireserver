package com.scalanerds.wire

object OpCodes {
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
}
