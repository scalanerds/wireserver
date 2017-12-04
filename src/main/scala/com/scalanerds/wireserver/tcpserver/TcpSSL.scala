package com.scalanerds.wireserver.tcpserver

import java.security.{KeyStore, SecureRandom}
import javax.net.ssl.{KeyManagerFactory, SSLContext, TrustManagerFactory}

/**
  *  for Tcp connections through SSL
  */
trait TcpSSL {
  def sslContext(keystore: String, truststore: String): SSLContext = {
    // password should be read from file and converted to char array
    val password = "123456".toCharArray

    //TODO: dynamically red keyStore and trustStore from string argument
    val keyStore = KeyStore.getInstance(KeyStore.getDefaultType)
    keyStore.load(getClass.getResourceAsStream(keystore), password)

    val trustStore = KeyStore.getInstance(KeyStore.getDefaultType)
    trustStore.load(getClass.getResourceAsStream(truststore), password)

    val keyManagerFactory =
      KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm)
    keyManagerFactory.init(keyStore, password)

    val trustManagerFactory =
      TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm)
    trustManagerFactory.init(trustStore)

    val context = SSLContext.getInstance("TLS")
    context.init(keyManagerFactory.getKeyManagers,
                 trustManagerFactory.getTrustManagers,
                 new SecureRandom)
    context
  }
}
