// ! protoc --js_out=import_style=commonjs,binary:./src ./proto/chain.proto ./proto/contract.proto
// ! npm install google-protobuf

const chain = require('./proto/chain_pb');
const contract = require('./proto/contract_pb');
const google_protobuf_any_pb = require('google-protobuf/google/protobuf/any_pb.js');

let transfer = new contract.TransferContract();

transfer.setOwnerAddress(Uint8Array.from(Buffer.from("414d1ef8673f916debb7e2515a8f3ecaf2611034aa", 'hex')));
transfer.setToAddress(Uint8Array.from(Buffer.from("4184292b9ee2e685591a926b82f2ed4dbcac06e3c1", 'hex')));
// 100_TRX
transfer.setAmount(100_000_000);

console.log(transfer.toObject());

let cntr = new chain.Transaction.Contract();
cntr.setType(chain.ContractType.TRANSFERCONTRACT);

let any = new google_protobuf_any_pb.Any();
any.pack(transfer.serializeBinary(), "protocol.TransferContract");

cntr.setParameter(any);

console.log(cntr.toObject());

let raw = new chain.Transaction.Raw();
raw.setContract(cntr);
raw.setFeeLimit(0); // or 1000_000_000

// expire in 60s
raw.setTimestamp(Math.floor(Date.now()));
raw.setExpiration(Math.floor(Date.now() / 1000) * 1000 + 60_000);

// tapos:
// block_hash[6..8]
raw.setRefBlockBytes(Uint8Array.from(Buffer.from("a7fa", 'hex')));
// block_hash[8..16]
raw.setRefBlockHash(Uint8Array.from(Buffer.from("d1a8441dd662f8cb", 'hex')));

let rawSerialized = raw.serializeBinary();

console.log(raw.toObject());

console.log(Buffer.from(rawSerialized).toString('hex'));

console.log("============ sha256 ==");

//TODO
console.log("============= secp256k1 ==");
//TODO