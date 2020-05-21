package com.example;

import com.example.SECP256K1;
import com.google.protobuf.ByteString;
import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.tuweni.bytes.Bytes32;
import org.bouncycastle.util.encoders.Hex;
import org.tron.api.GrpcAPI.Return;
import org.tron.api.GrpcAPI.TransactionExtention;
import org.tron.api.WalletGrpc;
import org.tron.protos.Protocol.Transaction;
import org.tron.protos.contract.BalanceContract.TransferContract;

public class TronClient {
  private static final Logger logger = Logger.getLogger(TronClient.class.getName());

  private final WalletGrpc.WalletBlockingStub blockingStub;
  private final SECP256K1.KeyPair keyPair;

  public TronClient(Channel channel, String privateKey) {
    blockingStub = WalletGrpc.newBlockingStub(channel);
    keyPair = SECP256K1.KeyPair.create(SECP256K1.PrivateKey.create(Bytes32.fromHexString(privateKey)));
  }

  public void transfer(String from, String to, long amount) throws Exception {
    logger.info("Transfer from: " + from);
    logger.info("Transfer to: " + from);

    byte[] rawFrom = Base58Check.base58ToBytes(from);
    byte[] rawTo = Base58Check.base58ToBytes(to);

    TransferContract req = TransferContract.newBuilder()
                               .setOwnerAddress(ByteString.copyFrom(rawFrom))
                               .setToAddress(ByteString.copyFrom(rawTo))
                               .setAmount(amount)
                               .build();
    logger.info("transfer => ", req.toString());

    TransactionExtention txnExt = blockingStub.createTransaction2(req);
    logger.info("txn id => " + Hex.toHexString(txnExt.getTxid().toByteArray()));

    SECP256K1.Signature sig = SECP256K1.sign(Bytes32.wrap(txnExt.getTxid().toByteArray()), keyPair);
    logger.info("signature => " + Hex.toHexString(sig.encodedBytes().toArray()));
    Transaction signedTxn =
        txnExt.getTransaction().toBuilder().addSignature(ByteString.copyFrom(sig.encodedBytes().toArray())).build();

    logger.info(signedTxn.toString());
    // Return ret = blockingStub.broadcastTransaction(signedTxn);
    // logger.info("======== Result ========\n" + ret.toString());
  }
}
