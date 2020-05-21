package com.example;

import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import org.bouncycastle.jcajce.provider.digest.Keccak;
import org.bouncycastle.util.encoders.Hex;

/**
 * Hello world!
 *
 */

public class Application {
  public static void generateAddress() {
    // generate random address
    SECP256K1.KeyPair kp = SECP256K1.KeyPair.generate();

    SECP256K1.PublicKey pubKey = kp.getPublicKey();
    Keccak.Digest256 digest = new Keccak.Digest256();
    digest.update(pubKey.getEncoded(), 0, 64);
    byte[] raw = digest.digest();
    byte[] rawAddr = new byte[21];
    rawAddr[0] = 0x41;
    System.arraycopy(raw, 12, rawAddr, 1, 20);

    System.out.println("Base58Check: " + Base58Check.bytesToBase58(rawAddr));
    System.out.println("Hex Address: " + Hex.toHexString(rawAddr));
    System.out.println("Public Key:  " + Hex.toHexString(pubKey.getEncoded()));
    System.out.println("Private Key: " + Hex.toHexString(kp.getPrivateKey().getEncoded()));
  }
  public static void main(String[] args) throws Exception {
    generateAddress();

    System.out.println("======================================================================");

    String target = "grpc.shasta.trongrid.io:50051";

    ManagedChannel channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
    TronClient client = new TronClient(channel, "3333333333333333333333333333333333333333333333333333333333333333");

    try {
      client.transfer("TJRabPrwbZy45sbavfcjinPJC18kjpRTv8", "TDZtzCp2m6vyyrNVGj77QPZQKtFd3Q5AUZ", 1_1000);
    } finally {
      channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
    }

    System.out.println("Done!");
  }
}
