const TrustRecord = artifacts.require("TrustRecord");

module.exports = function (deployer) {
  deployer.deploy(TrustRecord);
};
