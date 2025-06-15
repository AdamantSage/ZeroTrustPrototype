pragma solidity ^0.8.0;

contract TrustRecord {
    struct Event {
        address device;
        uint256 timestamp;
        uint256 trustScore;
    }

    Event[] public events;

    function record(address device, uint256 trustScore) public {
        events.push(Event(device, block.timestamp, trustScore));
    }

    function getEventCount() public view returns (uint256) {
        return events.length;
    }
}
