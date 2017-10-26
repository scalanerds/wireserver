package com.scalanerds.wireserver.wire.opcodes

/**
  * Mongo wire OpCodes
  */
sealed abstract class OpCode {
  def value: Int
  override def toString: String = value.toString
  override def hashCode(): Int = value
}

object OpCode {
  def apply(value:Int): Option[OpCode] = Option(value) collect  {
    case 1 => OpReplyCode
    case 1000 => OpMsgCode
    case 2001 => OpUpdateCode
    case 2002 => OpInsertCode
    case 2004 => OpQueryCode
    case 2005 => OpGetMoreCode
    case 2006 => OpDeleteCode
    case 2007 => OpKillCursorsCode
    case 2010 => OpCommandCode
    case 2011 => OpCommandReplyCode
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