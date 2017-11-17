package uk.dsxt.bb.loadgenerator.external_monitor;

import uk.dsxt.bb.blockchain.Manager;
import uk.dsxt.bb.blockchain.Message;
import uk.dsxt.bb.datamodel.blockchain.BlockchainBlock;
import uk.dsxt.bb.datamodel.blockchain.BlockchainChainInfo;
import uk.dsxt.bb.datamodel.blockchain.BlockchainPeer;

import java.io.IOException;
import java.util.List;

/**
 * Mocks all methods and only sends stats to {@link StatsDConnector}.
 */
public class MockManager implements Manager {
    private final StatsDConnector connector = new StatsDConnector();
    private final String target;

    /**
     * Mocks all methods and only sends stats to {@link StatsDConnector}.
     *
     * @param target Code to send in {@link StatsDConnector}
     */
    public MockManager(String target) {
        this.target = target;
    }

    @Override
    public String sendTransaction(String to, String from, long amount) {
        return null;
    }

    @Override
    public String sendMessage(byte[] body) {
        connector.count("epvoting.rf.generator." + target, 1L);
        return null;
    }

    @Override
    public String sendMessage(String from, String to, String message) {
        connector.count("epvoting.rf.generator." + target, 1L);
        return null;
    }

    @Override
    public List<Message> getNewMessages() {
        return null;
    }

    @Override
    public BlockchainBlock getBlock(long id) throws IOException {
        return null;
    }

    @Override
    public BlockchainPeer[] getPeers() throws IOException {
        return new BlockchainPeer[0];
    }

    @Override
    public BlockchainChainInfo getChain() throws IOException {
        return null;
    }

    @Override
    public void authorize(String user, String password) {

    }
}
