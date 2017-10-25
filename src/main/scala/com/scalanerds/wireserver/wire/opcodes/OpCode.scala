package com.scalanerds.wireserver.wire.opcodes

/**
  * Mongo wire OpCodes
  */
sealed abstract class OpCode {
  def value: Int

  override def toString: String = value.toString
}

object OpCode {
  def apply(value:Int): Option[OpCode] = value match  {
    case 1 => Some(OpReplyCode)
    case 1000 => Some(OpMsgCode)
    case 2001 => Some(OpUpdateCode)
    case 2002 => Some(OpInsertCode)
    case 2004 => Some(OpQueryCode)
    case 2005 => Some(OpGetMoreCode)
    case 2006 => Some(OpDeleteCode)
    case 2007 => Some(OpKillCursorsCode)
    case 2010 => Some(OpCommandCode)
    case 2011 => Some(OpCommandReplyCode)
    case _ => None
  }
}

/** Mongo client request */
trait ClientCode

/** Mongo server reply */
trait ServerCode

case object OpReplyCode extends OpCode with ServerCode {
  val value = 1
}

case object OpMsgCode extends OpCode with ServerCode {
  val value = 1000
}

case object OpUpdateCode extends OpCode with ClientCode {
  val value = 2001
}

case object OpInsertCode extends OpCode with ClientCode {
  val value = 2002
}

case object OpQueryCode extends OpCode with ClientCode {
  val value = 2004
}

case object OpGetMoreCode extends OpCode with ClientCode {
  val value = 2005
}

case object OpDeleteCode extends OpCode with ClientCode {
  val value = 2006
}

case object OpKillCursorsCode extends OpCode with ClientCode {
  val value = 2007
}

case object OpCommandCode extends OpCode with ClientCode {
  val value = 2010
}

case object OpCommandReplyCode extends OpCode with ServerCode {
  val value = 2011
}