package uk.dsxt.bb.loadgenerator.external_monitor;

import lombok.extern.log4j.Log4j2;
import uk.dsxt.bb.blockchain.Manager;
import uk.dsxt.bb.blockchain.Message;
import uk.dsxt.bb.datamodel.blockchain.BlockchainBlock;
import uk.dsxt.bb.datamodel.blockchain.BlockchainChainInfo;
import uk.dsxt.bb.datamodel.blockchain.BlockchainPeer;

import java.io.IOException;
import java.util.List;

/**
 * Delegates all methods to given {@link Manager}, but also sends stats to {@link StatsDConnector}.
 */
@Log4j2
public class SpyManager implements Manager {
    private final StatsDConnector connector = new StatsDConnector();
    private final Manager manager;
    private final String target;

    /**
     * Delegates all methods to given {@link Manager}, but also sends stats to {@link StatsDConnector}.
     *
     * @param manager Manager to delegate calls
     * @param target  Code to send in {@link StatsDConnector}
     */
    public SpyManager(Manager manager, String target) {
        this.manager = manager;
        this.target = target;
    }

    @Override
    public String sendTransaction(String to, String from, long amount) {
        return manager.sendTransaction(to, from, amount);
    }

    @Override
    public String sendMessage(byte[] body) {
        connector.count("epvoting.phd.fabric.generator." + target, 1L);
        return manager.sendMessage(body);
    }

    @Override
    public String sendMessage(String from, String to, String message) {
        connector.count("epvoting.phd.fabric.generator." + target, 1L);
        return manager.sendMessage(from, to, message);
    }

    @Override
    public List<Message> getNewMessages() {
        return manager.getNewMessages();
    }

    @Override
    public BlockchainBlock getBlockById(long id) throws IOException {
        return manager.getBlockById(id);
    }

    @Override
    public BlockchainBlock getBlockByHash(String hash) throws IOException {
        return manager.getBlockByHash(hash);
    }

    @Override
    public BlockchainPeer[] getPeers() throws IOException {
        return manager.getPeers();
    }

    @Override
    public BlockchainChainInfo getChain() throws IOException {
        return manager.getChain();
    }

    @Override
    public void authorize(String user, String password) {
        manager.authorize(user, password);
    }
}
