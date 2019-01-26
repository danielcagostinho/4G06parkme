const DBReqestor = require("./DBRequestor")

const pollTime = 3000;

module.exports = {

    // start initial time out
    init() {
        setInterval( ()=> { DBReqestor.Update()}, pollTime);
    }
}