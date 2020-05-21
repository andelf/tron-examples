package com.example;

import com.example.SECP256K1;
import com.google.protobuf.ByteString;
import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import java.util.concurrent.TimeUnit;
import org.apache.tuweni.bytes.Bytes32;
import org.bouncycastle.util.encoders.Hex;
import org.tron.api.GrpcAPI.Return;
import org.tron.api.GrpcAPI.TransactionExtention;
import org.tron.api.WalletGrpc;
import org.tron.protos.Protocol.Transaction;
import org.tron.protos.contract.BalanceContract.TransferContract;

public class TronClient {
  private final WalletGrpc.WalletBlockingStub blockingStub;
  private final SECP256K1.KeyPair keyPair;

  public TronClient(Channel channel, String privateKey) {
    blockingStub = WalletGrpc.newBlockingStub(channel);
    keyPair = SECP256K1.KeyPair.create(SECP256K1.PrivateKey.create(Bytes32.fromHexString(privateKey)));
  }

  public void transfer(String from, String to, long amount) throws Exception {
    System.out.println("Transfer from: " + from);
    System.out.println("Transfer to: " + from);

    byte[] rawFrom = Base58Check.base58ToBytes(from);
    byte[] rawTo = Base58Check.base58ToBytes(to);

    TransferContract req = TransferContract.newBuilder()
                               .setOwnerAddress(ByteString.copyFrom(rawFrom))
                               .setToAddress(ByteString.copyFrom(rawTo))
                               .setAmount(amount)
                               .build();
    System.out.println("transfer => " + req.toString());

    TransactionExtention txnExt = blockingStub.createTransaction2(req);
    System.out.println("txn id => " + Hex.toHexString(txnExt.getTxid().toByteArray()));

    SECP256K1.Signature sig = SECP256K1.sign(Bytes32.wrap(txnExt.getTxid().toByteArray()), keyPair);
    System.out.println("signature => " + Hex.toHexString(sig.encodedBytes().toArray()));
    Transaction signedTxn =
        txnExt.getTransaction().toBuilder().addSignature(ByteString.copyFrom(sig.encodedBytes().toArray())).build();

    System.out.println(signedTxn.toString());
    Return ret = blockingStub.broadcastTransaction(signedTxn);
    System.out.println("======== Result ========\n" + ret.toString());
  }
}
