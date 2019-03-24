const DBReqestor = require("./DBRequestor")

const pollTime = 1000;

module.exports = {

    // start initial time out
    init() {
        setInterval( ()=> { DBReqestor.Update()}, pollTime);
    }
}